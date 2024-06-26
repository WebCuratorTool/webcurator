/**
 * nz.govt.natlib.ndha.wctdpsdepositor - Software License
 * <p>
 * Copyright 2007/2009 National Library of New Zealand.
 * All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * or the file "LICENSE.txt" included with the software.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package nz.govt.natlib.ndha.wctdpsdepositor;

import com.exlibris.dps.DepositWebServices;
import com.exlibris.dps.sdk.pds.PdsClient;
import com.google.inject.Inject;
import nz.govt.natlib.ndha.wctdpsdepositor.dpsdeposit.DepositWebServicesFactory;
import nz.govt.natlib.ndha.wctdpsdepositor.dpsdeposit.dspresult.DepositResultConverter;
import nz.govt.natlib.ndha.wctdpsdepositor.extractor.ArchiveFile;
import nz.govt.natlib.ndha.wctdpsdepositor.extractor.WctDataExtractor;
import nz.govt.natlib.ndha.wctdpsdepositor.extractor.XPathWctMetsExtractor;
import nz.govt.natlib.ndha.wctdpsdepositor.extractor.filefinder.CollectionFileArchiveBuilder;
import nz.govt.natlib.ndha.wctdpsdepositor.extractor.filefinder.FileArchiveBuilder;
import nz.govt.natlib.ndha.wctdpsdepositor.filemover.FileMover;
import nz.govt.natlib.ndha.wctdpsdepositor.mets.DnxMapper;
import nz.govt.natlib.ndha.wctdpsdepositor.mets.MetsDocument;
import nz.govt.natlib.ndha.wctdpsdepositor.pds.PdsClientFactory;
import nz.govt.natlib.ndha.wctdpsdepositor.preprocessor.PreDepositProcessor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webcurator.core.archive.dps.DpsDepositFacade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DpsDepositFacadeImpl implements DpsDepositFacade {
    private static final Log log = LogFactory.getLog(DpsDepositFacadeImpl.class);

    private final DepositWebServicesFactory dwsFactory;
    private final FileMover fileMover;
    private final PdsClientFactory pdsClientFactory;
    private final DnxMapper dnxMapper;
    private final PreDepositProcessor preDepositProcessor;
    private static PdsClient pdsClient;

    private CustomDepositFormMapping customDepositFormMapping;

    private DepositResultConverter resultConverter = new DepositResultConverter();

    @Inject
    public DpsDepositFacadeImpl(DepositWebServicesFactory dwsFactory, PdsClientFactory pdsClientFactory, FileMover fileMover, DnxMapper dnxMapper, PreDepositProcessor preDepositProcessor) {
        this.dwsFactory = dwsFactory;
        this.pdsClientFactory = pdsClientFactory;
        this.fileMover = fileMover;
        this.dnxMapper = dnxMapper;
        this.preDepositProcessor = preDepositProcessor;
    }


    public DepositResult deposit(Map<String, String> parameters, List<File> fileList) throws WctDepositParameterValidationException {
        DepositResult depositResultAdapter = null;
        try {
            String targetInstanceOID = parameters.get(DpsDepositFacade.TARGET_INSTANCE_ID);
            String finalSIP = parameters.get(DpsDepositFacade.WCT_METS_XML_DOCUMENT);
            String ilsReference = parameters.get(DpsDepositFacade.ILS_REFERENCE);
            String accessRestriction = parameters.get(DpsDepositFacade.ACCESS_RESTRICTION);

            FileArchiveBuilder archiveBuilder = populateFileArchiveBuilderFrom(fileList);
            XPathWctMetsExtractor wctDataExtractor = new XPathWctMetsExtractor();
            String metsFileName = "METS-" + targetInstanceOID + ".xml";
            log.debug("finalSIP:\n" + finalSIP);
            try {
                String defaultCharset = Charset.defaultCharset().name();
                log.info("Charset.defaultCharset()" + defaultCharset);
                File metFileDefault = File.createTempFile("METS-" + targetInstanceOID + "-", "-default.xml");
                IOUtils.write(finalSIP.getBytes(), new FileOutputStream(metFileDefault));
                File metFileUTF8 = File.createTempFile("METS-" + targetInstanceOID + "-", "-utf8.xml");
                IOUtils.write(finalSIP.getBytes(StandardCharsets.UTF_8), new FileOutputStream(metFileUTF8));
            } catch (IOException e) {
                log.error(e);
            }

            wctDataExtractor.parseFile(finalSIP.getBytes(), metsFileName, archiveBuilder);
            setHarvestType(wctDataExtractor, parameters.get(HARVEST_TYPE));
            setAdditionalDublinCoreElements(wctDataExtractor, parameters);
            wctDataExtractor.setIeEntityType(parameters.get(IE_ENTITY_TYPE));
            wctDataExtractor.setWctTargetInstanceID(targetInstanceOID);
            wctDataExtractor.setILSReference(ilsReference);
            wctDataExtractor.setAccessRestriction(accessRestriction);
            wctDataExtractor.setCmsSection(parameters.get(CMS_SECTION));
            wctDataExtractor.setCmsSystem(parameters.get(CMS_SYSTEM));
            wctDataExtractor.setDCTitleSource(parameters.get(TITLE_SOURCE));

            WctDepositParameter depositParameter = populateDepositParameterFromMap(parameters);

            depositResultAdapter = deposit(wctDataExtractor, depositParameter);
        } finally {
        }
        return depositResultAdapter;
    }

    public String loginToPDS(Map<String, String> parameters) throws RuntimeException {
        WctDepositParameter depositParameter = populateDepositParameterFromMap(parameters);
        return authenticate(depositParameter);
    }

    private DepositResult deposit(WctDataExtractor wctData, WctDepositParameter depositParameter) throws WctDepositParameterValidationException {
        log.debug("Deposit started");
        try {
            depositParameter.isValid();
            preDepositProcessor.process(wctData);
            dnxMapper.populateAccessRightsCodes(depositParameter);
            MetsDocument dpsMetsDocument = dnxMapper.generateDnxFrom(wctData);
            moveFilesToServer(dpsMetsDocument, wctData.getAllFiles(), depositParameter);
            String pdsSessionId = authenticate(depositParameter);
            DepositResult depositResultAdapter = callDepositService(pdsSessionId, dpsMetsDocument, depositParameter);
            if (log.isDebugEnabled())
                log.debug("Deposit finished, SipId: " + depositResultAdapter.getSipId());
            return depositResultAdapter;
        } catch (Exception e) {
            log.error("Failed to deposit: ", e);
            throw e;
        } finally {
            wctData.cleanUpCdxFile();
        }
    }

    private void moveFilesToServer(MetsDocument metsDocument, List<ArchiveFile> archiveFiles, WctDepositParameter depositParameter) {
        fileMover.move(metsDocument, archiveFiles, depositParameter);
    }

    private String authenticate(WctDepositParameter depositParameter) {
        initPdsClient(depositParameter.getPdsUrl());

        String pdsHandle = authenticateWithPDS(depositParameter);
        return pdsHandle;
    }

    private void initPdsClient(String pdsUrl) {
        if (pdsClient == null) {
            synchronized (DpsDepositFacadeImpl.class) {
                pdsClient = pdsClientFactory.createInstance();
                pdsClient.init(pdsUrl, false);
            }
        }
    }

    private String authenticateWithPDS(WctDepositParameter depositParameter) {
        try {
            return pdsClient.login(depositParameter.getDpsInstitution(), depositParameter.getDpsUserName(), depositParameter.getDpsPassword());
        } catch (Exception e) {
            throw new RuntimeException("An exception occurred while authenticating with the PDS service.", e);
        }
    }

    private DepositResult callDepositService(String pdsSessionId, MetsDocument metsDocument, WctDepositParameter depositParameter) {
        String info = String.format("pdsSessionId=%s, materialId=%s, depositDirectoryName=%s, producerId=%s, depositSetId=%s", pdsSessionId, depositParameter.getMaterialFlowId(), metsDocument.getDepositDirectoryName(), depositParameter.getProducerId(), metsDocument.getDepositSetId());
        log.debug(info);
        DepositWebServices dws = dwsFactory.createInstance(depositParameter);
        String xmlFragmentResult = dws.submitDepositActivity(pdsSessionId, depositParameter.getMaterialFlowId(), metsDocument.getDepositDirectoryName(), depositParameter.getProducerId(), metsDocument.getDepositSetId());
        return resultConverter.unmarshalFrom(xmlFragmentResult);
    }

    private FileArchiveBuilder populateFileArchiveBuilderFrom(List<File> fileList) {
        Map<String, File> archiveFileMap = new HashMap<String, File>();

        for (File archiveFile : fileList)
            archiveFileMap.put(archiveFile.getName(), archiveFile);

        return new CollectionFileArchiveBuilder(archiveFileMap);
    }

    private WctDepositParameter populateDepositParameterFromMap(Map<String, String> parameters) {
        WctDepositParameter depositParameter = new WctDepositParameter();

        depositParameter.setDpsInstitution(parameters.get(DpsDepositFacade.DPS_INSTITUTION));
        depositParameter.setDpsUserName(parameters.get(DpsDepositFacade.DPS_USER_NAME));
        depositParameter.setDpsPassword(parameters.get(DpsDepositFacade.DPS_PASSWORD));
        depositParameter.setFtpHost(parameters.get(DpsDepositFacade.FTP_HOST));
        depositParameter.setFtpPassword(parameters.get(DpsDepositFacade.FTP_PASSWORD));
        depositParameter.setFtpUserName(parameters.get(DpsDepositFacade.FTP_USER_NAME));
        depositParameter.setFtpDirectory(parameters.get(DpsDepositFacade.FTP_DIRECTORY));
        depositParameter.setMaterialFlowId(parameters.get(DpsDepositFacade.MATERIAL_FLOW_ID));
        depositParameter.setPdsUrl(parameters.get(DpsDepositFacade.PDS_URL));
        depositParameter.setProducerId(parameters.get(DpsDepositFacade.PRODUCER_ID));
        depositParameter.setDpsWsdlUrl(parameters.get(DpsDepositFacade.DPS_WSDL_URL));
        depositParameter.setOmsOpenAccess(parameters.get(DpsDepositFacade.OMS_OPEN_ACCESS));
        depositParameter.setOmsPublishedRestricted(parameters.get(DpsDepositFacade.OMS_PUBLISHED_RESTRICTED));
        depositParameter.setOmsUnpublishedRestrictedByLocation(parameters.get(DpsDepositFacade.OMS_UNPUBLISHED_RESTRICTED_BY_LOCATION));
        depositParameter.setOmsUnpublishedRestrictedByPersion(parameters.get(DpsDepositFacade.OMS_UNPUBLISHED_RESTRICTED_BY_PERSON));

        return depositParameter;
    }

    private void setHarvestType(XPathWctMetsExtractor wctDataExtractor, String harvestTypeString) {
        HarvestType type = HarvestType.TraditionalWebHarvest;
        try {
            type = HarvestType.valueOf(harvestTypeString);
        } catch (Exception e) {
        }
        wctDataExtractor.setHarvestType(type);
    }

    public void setCustomDepositFormMapping(CustomDepositFormMapping customDepositFormMapping) {
        this.customDepositFormMapping = customDepositFormMapping;
    }

    private void setAdditionalDublinCoreElements(XPathWctMetsExtractor wctDataExtractor, Map<String, String> parameters) {
        if (HarvestType.HtmlSerialHarvest.equals(wctDataExtractor.getHarvestType()) == false) return;
        String customDepositFormURL = parameters.get(DpsDepositFacade.CUSTOM_DEPOSIT_FORM_URL);
        List<CustomDepositField> customDepositFormFieldMapping = customDepositFormMapping.getFormMapping(customDepositFormURL);

        for (CustomDepositField field : customDepositFormFieldMapping) {
            // Check whether field is dc or dcterms
            if (field.getDcFieldType().equals("dc")) {
                wctDataExtractor.setAdditionalDCElement(field.getDcFieldLabel(), parameters.get(field.getFieldReference()));
            } else if (field.getDcFieldType().equals("dcterms")) {
                wctDataExtractor.setAdditionalDCTermElement(field.getDcFieldLabel(), parameters.get(field.getFieldReference()));
            }
        }
        if (!customDepositFormFieldMapping.isEmpty()) {
            wctDataExtractor.setDcFieldsAdditional(customDepositFormFieldMapping);
        }
    }

}
