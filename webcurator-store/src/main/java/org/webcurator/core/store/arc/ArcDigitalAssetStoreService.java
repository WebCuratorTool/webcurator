/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.core.store.arc;

import static org.webcurator.core.archive.Constants.ARC_FILE;
import static org.webcurator.core.archive.Constants.LOG_FILE;
import static org.webcurator.core.archive.Constants.REPORT_FILE;
import static org.webcurator.core.archive.Constants.ROOT_FILE;

import com.google.common.collect.ImmutableMap;
import it.unipi.di.util.ExternalSort;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpParser;
import org.apache.commons.io.FileUtils;
import org.archive.format.warc.WARCConstants;
import org.archive.io.*;
import org.archive.io.arc.ARCRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.archive.Archive;
import org.webcurator.core.archive.ArchiveFile;
import org.webcurator.core.archive.file.FileArchive;
import org.webcurator.core.harvester.coordinator.HarvestCoordinatorPaths;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.core.store.Constants;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.reader.LogProvider;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.store.Indexer;
import org.webcurator.core.util.WebServiceEndPoint;
import org.webcurator.core.visualization.VisualizationConstants;
import org.webcurator.core.visualization.VisualizationManager;
import org.webcurator.core.visualization.modification.PruneAndImportProcessor;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandApply;
import org.webcurator.core.visualization.modification.metadata.PruneAndImportCommandResult;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapNode;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.model.core.*;

/**
 * The ArcDigitalAssetStoreService is used for storing and accessing the
 * completed harvest data from the digital asset store.
 *
 * @author bbeaumont
 */
@SuppressWarnings("all")
public class ArcDigitalAssetStoreService extends AbstractRestClient implements DigitalAssetStore, LogProvider {
    /**
     * The logger.
     */
    private static Logger log = LoggerFactory.getLogger(ArcDigitalAssetStoreService.class);
    /**
     * the base directory for the digital asset stores harvest files.
     */
    private File baseDir = null;
    /**
     * Constant for the size of a buffer.
     */
    private final int BYTE_BUFF_SIZE = 1024;

    /**
     * Arc files meta data date format.
     */
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private static final SimpleDateFormat writerDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    /**
     * The Indexer
     */
    private Indexer indexer = null;
    /**
     * The DAS File Mover
     */
    private DasFileMover dasFileMover = null;

    private FileArchive fileArchive = null;
    private WebServiceEndPoint wsEndPoint;
    @Autowired
    private Archive arcDasArchive;

    private String pageImagePrefix = "PageImage";
    private String aqaReportPrefix = "aqa-report";

    private VisualizationManager visualizationManager;
    private NetworkMapClient networkMapClient;

    static {
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        writerDF.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public ArcDigitalAssetStoreService(WebServiceEndPoint wsEndPoint, RestTemplateBuilder restTemplateBuilder) {
        this(wsEndPoint.getSchema(), wsEndPoint.getHost(), wsEndPoint.getPort(), restTemplateBuilder);
        this.wsEndPoint = wsEndPoint;
    }

    public ArcDigitalAssetStoreService(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    public void save(String targetInstanceName, String directory, Path path)
            throws DigitalAssetStoreException {
        save(targetInstanceName, directory, Collections.singletonList(path));
    }

    /**
     * @see DigitalAssetStore#save(String, String, List<java.nio.file.Path>).
     */
    public void save(String targetInstanceName, String directory, List<Path> paths)
            throws DigitalAssetStoreException {
        // Target destination is always baseDir plus targetInstanceName.
        File targetDir = new File(baseDir, targetInstanceName);
        String dir = directory + "/";

        // Create the target dir if is doesn't exist. This will also
        // create the parent if necessary.
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

        // Move the ARC files into the /1 directory.
        new File(targetDir, dir).mkdirs();
        boolean success = true;
        Exception failureException = null;

        // Loop through all the files, but stop if any of them fail.
        for (Path path : paths) {
            File destination = new File(targetDir, "/" + dir
                    + path.getFileName());
            log.debug("Moving File to Store: " + path.toString()
                    + " -> " + destination.getAbsolutePath());

            try {
                // FileUtils.copyFile(files[i], destination);
                // DasFileMover fileMover = new InputStreamDasFileMover();
                dasFileMover.moveFile(path.toFile(), destination);
            } catch (IOException ex) {
                log.error("Failed to move file " + path.toString()
                        + " to " + destination.getAbsolutePath(), ex);
                failureException = ex;
                success = false;
            }
        }

        // If the copy failed, throw an exception.
        if (!success) {
            throw new DigitalAssetStoreException(
                    "Failed to move Archive files to " + targetDir + "/" + dir,
                    failureException);
        }
    }


    /**
     * @see DigitalAssetStore#save(String, List<Path>).
     */
    public void save(String targetInstanceName, List<Path> paths)
            throws DigitalAssetStoreException {
        save(targetInstanceName, "1", paths);
    }

    public void save(String targetInstanceName, Path path)
            throws DigitalAssetStoreException {
        save(targetInstanceName, "1", Collections.singletonList(path));
    }

    /**
     * @see DigitalAssetStore#getResource(String, int, HarvestResourceDTO).
     */
    @SuppressWarnings("finally")
    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        NetworkMapNode resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        FileOutputStream fos = null;
        ArchiveReader reader = null;
        ArchiveRecord record = null;
        File source = null;
        File dest = null;
        try {
            source = new File(this.baseDir, "/" + targetInstanceId + "/"
                    + harvestResultNumber + "/" + resourceNode.getFileName());

            try {
                reader = ArchiveReaderFactory.get(source);
            } catch (IOException ex) {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to get resource : " + ex.getMessage());
                }
                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                if (log.isWarnEnabled()) {
                    log.info("trying filestore " + source.getAbsolutePath());
                }
                try {
                    reader = ArchiveReaderFactory.get(source);
                } catch (IOException e) {
                    throw new DigitalAssetStoreException(
                            "Failed to get resource : " + e.getMessage());
                }
            }

            record = reader.get(resourceNode.getOffset());

            dest = File.createTempFile("wct", "tmp");
            if (log.isDebugEnabled()) {
                log.debug("== Temp file: " + dest.getAbsolutePath());
            }

            fos = new FileOutputStream(dest);

            if (record instanceof ARCRecord) {
                ((ARCRecord) record).skipHttpHeader();
            } else {
                skipStatusLine(record);
                skipHeaders(record);
            }

            int bytesRead = 0;
            byte[] byteBuffer = new byte[BYTE_BUFF_SIZE];
            while ((bytesRead = record.read(byteBuffer, 0, BYTE_BUFF_SIZE)) != -1) {
                fos.write(byteBuffer, 0, bytesRead);
            }
            if (fos != null)
                fos.close();

        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + e.getMessage(), e);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + e.getMessage());
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + ex.getMessage(), ex);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + ex.getMessage());
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close fos failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
        }

        return dest.toPath();
    }

    /**
     * @see DigitalAssetStore#getSmallResource(String, int, HarvestResourceDTO).
     */
    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        NetworkMapNode resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        ArchiveRecord record = null;
        ArchiveReader reader = null;
        File source = null;
        try {
            source = new File(this.baseDir, "/" + targetInstanceId + "/"
                    + harvestResultNumber + "/" + resourceNode.getFileName());
            try {
                reader = ArchiveReaderFactory.get(source);

            } catch (IOException e) {
                if (log.isWarnEnabled()) {

                    log.error("Failed to get resource from "
                            + source.getAbsolutePath() + " from local store");
                }
                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                if (log.isWarnEnabled()) {
                    log.info("trying filestore " + source.getAbsolutePath());
                }
                reader = ArchiveReaderFactory.get(source);
            }
            record = reader.get(resourceNode.getOffset());

            if (record instanceof ARCRecord) {
                ((ARCRecord) record).skipHttpHeader();
            } else {
                skipStatusLine(record);
                skipHeaders(record);
            }

            ByteArrayOutputStream fos = new ByteArrayOutputStream(1024 * 1024);

            int bytesRead = 0;
            byte[] byteBuffer = new byte[BYTE_BUFF_SIZE];
            while ((bytesRead = record.read(byteBuffer, 0, BYTE_BUFF_SIZE)) != -1) {
                fos.write(byteBuffer, 0, bytesRead);
            }
            fos.close();

            // 5. Return the result.
            return fos.toByteArray();
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to get resource : " + ex.getMessage());
            }
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get resource : " + ex.getMessage(), ex);
            }
            throw new DigitalAssetStoreException("Failed to get resource : "
                    + ex.getMessage(), ex);
        } finally {
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (record != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
        }
        return new byte[0];
    }

    /**
     * @see DigitalAssetStore#getHeaders(String, int, HarvestResourceDTO).
     */
    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        log.debug("Start of getHeaders()");
        log.debug("Casting the DTO to HarvestResult");


        NetworkMapNode resourceNode = this.queryUrlNode(targetInstanceId, harvestResultNumber, resourceUrl);

        List<Header> headers = new ArrayList<>();
        ArchiveRecord record = null;
        ArchiveReader reader = null;

        log.debug("Determining the filename");

        File source = new File(this.baseDir, "/" + targetInstanceId + "/"
                + harvestResultNumber + "/" + resourceNode.getFileName());

        try {
            log.debug("Create the Archive File Reader");

            try {
                reader = ArchiveReaderFactory.get(source);
            } catch (IOException e) {
                log.warn("Could not read headers for ArchiveRecord from " + source.getAbsolutePath() + " from local store");

                source = new File(fileArchive.getArchiveRepository() + "/"
                        + targetInstanceId + "/"
                        + fileArchive.getArchiveArcDirectory() + "/"
                        + resourceNode.getFileName());
                log.info("trying filestore " + source.getAbsolutePath());

                reader = ArchiveReaderFactory.get(source);
            }

            log.debug("Skipping to the appropriate record at offset: " + resourceNode.getOffset());

            record = reader.get(resourceNode.getOffset());

            if (record instanceof ARCRecord) {
                log.debug("Reading the headers");
                ((ARCRecord) record).skipHttpHeader();
                Header[] headersArray = ((ARCRecord) record).getHttpHeaders();
                headers.addAll(Arrays.asList(headersArray));
            } else {
                log.debug("Reading the headers");
                skipStatusLine(record);
                Header[] headersArray = HttpParser.parseHeaders(record,
                        WARCConstants.DEFAULT_ENCODING);
                headers.addAll(Arrays.asList(headersArray));
            }

            Header httpResponseStatusCode = new Header("HTTP-RESPONSE-STATUS-CODE", Integer.toString(resourceNode.getStatusCode()));
            headers.add(httpResponseStatusCode);

            Header httpResponseContentLength = new Header("HTTP-RESPONSE-CONTENT_LENGTH", Long.toString(resourceNode.getContentLength()));
            headers.add(httpResponseContentLength);

            return headers;
        } catch (IOException ex) {
            if (log.isWarnEnabled()) {
                log.warn("Error reading headers from ArchiveRecord: "
                        + ex.getMessage());
            }
        } finally {
            try {
                if (record != null)
                    record.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close record failed " + ex.getMessage(), ex);
                }
            }
            try {
                if (record != null)
                    reader.close();
            } catch (Exception ex) {
                if (log.isErrorEnabled()) {
                    log.error("close reader failed " + ex.getMessage(), ex);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("End of method");
            }
        }
        return null;
    }

    private NetworkMapNode queryUrlNode(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        NetworkMapResult result = networkMapClient.getUrlByName(targetInstanceId, harvestResultNumber, resourceUrl);

        String err = String.format("Could not find NetworkMapNode with targetInstanceId=%d, harvestResultNumber=%d, resourceUrl=%s", targetInstanceId, harvestResultNumber, resourceUrl);
        if (result == null || result.getRspCode() != 0) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        String json = result.getPayload();
        NetworkMapNode node = networkMapClient.getNodeEntity(json);
        if (node == null) {
            log.warn(err);
            throw new DigitalAssetStoreException(err);
        }

        return node;
    }

    /**
     * Search the passed in list for an item matching the passed in Url.
     *
     * @param hrs - A list of HarvestResourceDTO objects
     * @param Url - The Url to check for.
     * @return true if a list item's name is equal to the Url, or else false
     */
    private boolean listContainsURL(List<HarvestResourceDTO> hrs, String Url) {
        for (Iterator<HarvestResourceDTO> it = hrs.iterator(); it.hasNext(); ) {
            HarvestResourceDTO hr = it.next();
            if (hr.getName().equals(Url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFile(java.lang.String,
     * java.lang.String)
     */
    public File getLogFile(String aJob, String aFileName) {
        File file = null;
        if (aJob.indexOf('@') < 0) {
            File targetDir = new File(baseDir, aJob);
            File logsDir = new File(targetDir, Constants.DIR_LOGS);
            file = new File(logsDir, aFileName);
            if (!file.exists() && aFileName.equalsIgnoreCase(Constants.SORTED_CRAWL_LOG_FILE)) {
                // we need to create sorted crawl.log from crawl.log.
                createSortedCrawlLogFile(logsDir);
                file = new File(logsDir, aFileName);
            }
            if (!file.exists()) {
                logsDir = new File(targetDir, Constants.DIR_REPORTS);
                file = new File(logsDir, aFileName);
            }
            if (!file.exists()) {
                logsDir = new File(targetDir, Constants.DIR_CONTENT);
                file = new File(logsDir, aFileName);
            }
        } else { //For patching logs
            File targetDir = parseAttachedLogDir(aJob);
            if (targetDir != null && targetDir.exists()) {
                file = new File(targetDir, aFileName);
            }

            if (file == null || !file.exists()) {
                targetDir = parseAttachedReportDir(aJob);
                if (targetDir != null && targetDir.exists()) {
                    file = new File(targetDir, aFileName);
                }
            }
        }
        if (file == null || !file.exists()) {
            file = null;
        }
        return file;
    }

    private void createSortedCrawlLogFile(File logsDir) {

        // sort the crawl.log file to create a sorted crawl.log file in the same
        // directory.

        // write new 'stripped' crawl.log, replacing multiple spaces with a
        // single space in each record..
        try {

            BufferedReader inputStream = new BufferedReader(new FileReader(
                    logsDir.getAbsolutePath() + File.separator
                            + Constants.CRAWL_LOG_FILE));
            PrintWriter outputStream = new PrintWriter(new FileWriter(
                    logsDir.getAbsolutePath() + File.separator
                            + Constants.STRIPPED_CRAWL_LOG_FILE));

            String inLine = null;

            while ((inLine = inputStream.readLine()) != null) {
                outputStream.println(inLine.replaceAll(" +", " "));
            }

            outputStream.close();
            inputStream.close();

        } catch (IOException e) {
            return;
        }

        // sort the 'stripped' crawl.log file to create a 'sorted' crawl.log
        // file...
        ExternalSort sort = new ExternalSort();
        try {
            sort.setInFile(logsDir.getAbsolutePath() + File.separator
                    + Constants.STRIPPED_CRAWL_LOG_FILE);
        } catch (FileNotFoundException e1) {
            return;
        }
        try {
            sort.setOutFile(logsDir.getAbsolutePath() + File.separator
                    + Constants.SORTED_CRAWL_LOG_FILE);
        } catch (FileNotFoundException e1) {
            return;
        }
        // sort on fourth column (url) then first column (timestamp)..
        int[] cols = {3, 0};
        sort.setColumns(cols);
        sort.setSeparator(' '); // space

        try {
            sort.run();
        } catch (IOException e1) {
            return;
        }
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFileNames(java.lang.String)
     */
    public List<String> getLogFileNames(String aJob) {
        List<String> logFiles = new ArrayList<String>();
        File logsDir = null;
        if (aJob.indexOf('@') < 0) {
            File targetDir = new File(baseDir, aJob);

            logsDir = new File(targetDir, Constants.DIR_LOGS);
            this.appendLogFileNames(logFiles, logsDir);

            logsDir = new File(targetDir, Constants.DIR_REPORTS);
            this.appendLogFileNames(logFiles, logsDir);
        } else {
            logsDir = parseAttachedLogDir(aJob);
            this.appendLogFileNames(logFiles, logsDir);

            logsDir = parseAttachedReportDir(aJob);
            this.appendLogFileNames(logFiles, logsDir);
        }

        return logFiles;
    }

    /**
     * @see org.webcurator.core.reader.LogProvider#getLogFileAttributes(java.lang.String)
     */
    public List<LogFilePropertiesDTO> getLogFileAttributes(String aJob) {
        List<LogFilePropertiesDTO> logFiles = new ArrayList<LogFilePropertiesDTO>();
        File logsDir = null;
        if (aJob.indexOf('@') < 0) {
            File targetDir = new File(baseDir, aJob);

            logsDir = new File(targetDir, Constants.DIR_LOGS);
            this.appendLogFiles(logFiles, logsDir);

            logsDir = new File(targetDir, Constants.DIR_REPORTS);
            this.appendLogFiles(logFiles, logsDir);
        } else {
            logsDir = parseAttachedLogDir(aJob);
            this.appendLogFiles(logFiles, logsDir);

            logsDir = parseAttachedReportDir(aJob);
            this.appendLogFiles(logFiles, logsDir);
        }

        return logFiles;
    }


    public File parseAttachedLogDir(String aJob) {
        File logsDir = null;
        String[] prefixItems = aJob.split("@");
        String prefix = prefixItems[0];
        String[] jobItems = prefixItems[1].split("_");
        String sTargetInstanceId = jobItems[1];
        String sHarvestNumberId = jobItems[2];

        String extDir = String.format("%s%s%s%s%s%s%s", baseDir, File.separator, sTargetInstanceId, File.separator, Constants.DIR_LOGS, File.separator, Constants.DIR_LOGS_EXT);
        if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            logsDir = new File(extDir, Constants.DIR_LOGS_INDEX);
        } else if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            logsDir = new File(extDir, Constants.DIR_LOGS_MOD);
        } else {
            log.warn("Unsupported query type {}", aJob);
            return null;
        }

        logsDir = new File(logsDir, sHarvestNumberId);

        return logsDir;
    }

//    public File getAttachedLogDir(String prefix, long targetInstanceId, int harvestResultNumber) {
//        String sTargetInstanceId = Long.toString(targetInstanceId);
//        String sHarvestNumberId = Integer.toString(harvestResultNumber);
//
//        File logsDir = null;
//        String extDir = String.format("%s%s%s%s%s%s%s", baseDir, File.separator, sTargetInstanceId, File.separator, Constants.DIR_LOGS, File.separator, Constants.DIR_LOGS_EXT);
//        if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
//            logsDir = new File(extDir, Constants.DIR_LOGS_INDEX);
//        } else if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
//            logsDir = new File(extDir, Constants.DIR_LOGS_MOD);
//        } else {
//            log.warn("Unsupported query type {} {} {}", prefix, targetInstanceId, harvestResultNumber);
//            return null;
//        }
//
//        logsDir = new File(logsDir, sHarvestNumberId);
//
//        return logsDir;
//    }

    public File parseAttachedReportDir(String aJob) {
        File logsDir = null;
        String[] prefixItems = aJob.split("@");
        String prefix = prefixItems[0];
        String[] jobItems = prefixItems[1].split("_");
        String sTargetInstanceId = jobItems[1];
        String sHarvestNumberId = jobItems[2];

        String extDir = String.format("%s%s%s%s%s%s%s", baseDir, File.separator, sTargetInstanceId, File.separator, Constants.DIR_REPORTS, File.separator, Constants.DIR_LOGS_EXT);
        if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            logsDir = new File(extDir, Constants.DIR_LOGS_INDEX);
        } else if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            logsDir = new File(extDir, Constants.DIR_LOGS_MOD);
        } else {
            log.warn("Unsupported query type {}", aJob);
            logsDir = null;
        }

        logsDir = new File(logsDir, sHarvestNumberId);

        return logsDir;
    }

//    public File getAttachedReportDir(String prefix, long targetInstanceId, int harvestResultNumber) {
//        String sTargetInstanceId = Long.toString(targetInstanceId);
//        String sHarvestNumberId = Integer.toString(harvestResultNumber);
//
//        File logsDir = null;
//        String extDir = String.format("%s%s%s%s%s%s%s", baseDir, File.separator, sTargetInstanceId, File.separator, Constants.DIR_REPORTS, File.separator, Constants.DIR_LOGS_EXT);
//        if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
//            logsDir = new File(extDir, Constants.DIR_LOGS_INDEX);
//        } else if (prefix.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
//            logsDir = new File(extDir, Constants.DIR_LOGS_MOD);
//        } else {
//            log.warn("Unsupported query type {} {} {}", prefix, targetInstanceId, harvestResultNumber);
//            return null;
//        }
//
//        logsDir = new File(logsDir, sHarvestNumberId);
//
//        return logsDir;
//    }

    private void appendLogFiles(List<LogFilePropertiesDTO> logFiles, File logsDir) {
        if (logFiles == null || logsDir == null || !logsDir.exists() || !logsDir.isDirectory()) {
            log.warn("Invalid input parameter");
            return;
        }
        File[] fileList = logsDir.listFiles();
        for (File f : fileList) {
            if (!f.isFile()) {
                continue;
            }
            logFiles.add(new LogFilePropertiesDTO(f, pageImagePrefix, aqaReportPrefix));
        }
    }

    private void appendLogFileNames(List<String> logFiles, File logsDir) {
        if (logFiles == null || logsDir == null || !logsDir.exists() || !logsDir.isDirectory()) {
            log.warn("Invalid input parameter");
            return;
        }
        File[] fileList = logsDir.listFiles();
        for (File f : fileList) {
            if (!f.isFile()) {
                continue;
            }
            logFiles.add(f.getName());
        }
    }

    /**
     * @see DigitalAssetStore#purge(List<String>).
     */
    public void purge(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        if (null == targetInstanceNames || targetInstanceNames.size() == 0) {
            return;
        }

        try {
            for (String tiName : targetInstanceNames) {
                File toPurge = new File(baseDir, tiName);
                if (log.isDebugEnabled()) {
                    log.debug("About to purge dir " + toPurge.toString());
                }
                try {
                    FileUtils.deleteDirectory(toPurge);
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            throw new DigitalAssetStoreException("Failed to complete purge : "
                    + e.getMessage(), e);
        }
    }

    /**
     * @see DigitalAssetStore#purgeAbortedTargetInstances(List<String>).
     */
    public void purgeAbortedTargetInstances(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        if (null == targetInstanceNames || targetInstanceNames.size() == 0) {
            return;
        }

        try {
            for (String tiName : targetInstanceNames) {
                File toPurge = new File(baseDir, tiName);
                if (log.isDebugEnabled()) {
                    log.debug("About to purge dir " + toPurge.toString());
                }
                try {
                    FileUtils.deleteDirectory(toPurge);
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
                    }
                }
            }
        } catch (Exception e) {
            throw new DigitalAssetStoreException("Failed to complete purge : "
                    + e.getMessage(), e);
        }
    }

    /**
     * Return a list of all the ARC files for the specified target instance and
     * harvest result
     *
     * @param targetInstanceName  the name of the target instance
     * @param harvestResultNumber the harvest result number
     * @return the list of all the ARC files
     * @throws DigitalAssetStoreException throw if there is a problem
     */
    private List<File> getAllARCFiles(String targetInstanceName,
                                      int harvestResultNumber) throws DigitalAssetStoreException {
        ArrayList<File> arcFiles = new ArrayList<File>();
        try {
            File sourceDir = new File(this.baseDir, "/" + targetInstanceName
                    + "/" + harvestResultNumber);
            for (File file : sourceDir.listFiles()) {
                arcFiles.add(file);
            }
            return arcFiles;
        } catch (RuntimeException ex) {
            if (log.isErrorEnabled()) {
                log.error("Failed to get archive files : " + ex.getMessage(),
                        ex);
            }
            throw new DigitalAssetStoreException(
                    "Failed to get archive files : " + ex.getMessage(), ex);
        }
    }

    /**
     * Return a list of all the log files for the specified target instance.
     *
     * @param targetInstanceName the name of the target instance
     * @return the list of log files
     * @throws DigitalAssetStoreException thrown if there is an error
     */
    private List<File> getLogFiles(String targetInstanceName)
            throws DigitalAssetStoreException {
        List<File> logFiles = new ArrayList<File>();
        File targetDir = new File(baseDir, targetInstanceName);
        File logsDir = new File(targetDir, Constants.DIR_LOGS);
        File[] fileList = null;

        if (logsDir.exists()) {
            fileList = logsDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.endsWith(".log");
                }
            });
            for (File f : fileList) {
                logFiles.add(f);
            }
        }
        return logFiles;
    }

    /**
     * Return a list of report files for the specified target instance
     *
     * @param targetInstanceName the name of the target instance
     * @return the list of report files
     * @throws DigitalAssetStoreException thrown if there is an error
     */
    private List<File> getReportFiles(String targetInstanceName)
            throws DigitalAssetStoreException {
        List<File> reportFiles = new ArrayList<File>();
        File targetDir = new File(baseDir, targetInstanceName);
        File reportsDir = new File(targetDir, Constants.DIR_REPORTS);
        File[] fileList = null;

        if (reportsDir.exists()) {
            fileList = reportsDir.listFiles();
            for (File f : fileList) {
                reportFiles.add(f);
            }
        }
        return reportFiles;
    }

    /**
     * @see DigitalAssetStore#submitToArchive(String, String, Map, int).
     */
    public void submitToArchive(String targetInstanceOid, String SIP,
                                Map xAttributes, int harvestNumber)
            throws DigitalAssetStoreException {
        // Kick off the archiving in a separate thread.
        ArchivingThread thread = new ArchivingThread(targetInstanceOid, SIP,
                xAttributes, harvestNumber, wsEndPoint);
        new Thread(thread).start();
    }

    private class ArchivingThread implements Runnable {
        private String targetInstanceOid = null;
        private String SIP = null;
        private Map xAttributes = null;
        private int harvestNumber;
        private WebServiceEndPoint wsEndPoint = null;


        public ArchivingThread(String targetInstanceOid, String sip,
                               Map attributes, int harvestNumber, WebServiceEndPoint wsEndPoint) {
            super();
            this.targetInstanceOid = targetInstanceOid;
            SIP = sip;
            xAttributes = attributes;
            this.harvestNumber = harvestNumber;
            this.wsEndPoint = wsEndPoint;
        }

        public void run() {
            try {

                String targetID = targetInstanceOid + "";
                ArrayList<ArchiveFile> fileList = new ArrayList<ArchiveFile>();
                // Get log files
                for (File f : getLogFiles(targetID)) {
                    fileList.add(new ArchiveFile(f, LOG_FILE));
                }
                // Get report files
                for (File f : getReportFiles(targetID)) {
                    if (f.getName().endsWith("order.xml")) {
                        fileList.add(new ArchiveFile(f, ROOT_FILE));
                    } else {
                        fileList.add(new ArchiveFile(f, REPORT_FILE));
                    }
                }
                // Get arc files
                for (File f : getAllARCFiles(targetID, harvestNumber)) {
                    fileList.add(new ArchiveFile(f, ARC_FILE));
                }

                String archiveIID = arcDasArchive.submitToArchive(targetInstanceOid,
                        SIP, xAttributes, fileList);

                completeArchiving(Long.parseLong(targetInstanceOid), archiveIID);
            } catch (Throwable t) {
                log.error("Could not archive " + targetInstanceOid, t);

                try {
                    failedArchiving(Long.parseLong(targetInstanceOid), t.getMessage());
                } catch (Exception ex) {
                    log.error("Got error trying to send \"failedArchiving\" to server", ex);
                }
            }
        }
    }

    private void completeArchiving(Long targetInstanceOid, String archiveIID) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.COMPLETE_ARCHIVING))
                .queryParam("archive-id", archiveIID);

        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    private void failedArchiving(Long targetInstanceOid, String message) {
        RestTemplate restTemplate = restTemplateBuilder.build();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.FAILED_ARCHIVING))
                .queryParam("message", message);

        Map<String, Long> pathVariables = ImmutableMap.of("target-instance-oid", targetInstanceOid);
        restTemplate.postForObject(uriComponentsBuilder.buildAndExpand(pathVariables).toUri(),
                null, Void.class);
    }

    public CustomDepositFormResultDTO getCustomDepositFormDetails(
            CustomDepositFormCriteriaDTO criteria)
            throws DigitalAssetStoreException {
        return arcDasArchive.getCustomDepositFormDetails(criteria);
    }


    /**
     * @param baseDir the base directory for the digital asset stores harvest files.
     */
    public void setBaseDir(String baseDir) {
        this.baseDir = new File(baseDir);
    }

    public void initiateIndexing(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
        // Determine the source directory.
        File sourceDir = new File(this.baseDir, "/"
                + harvestResult.getTargetInstanceOid() + "/"
                + harvestResult.getHarvestNumber());

        // Kick of the indexer.
        indexer.runIndex(harvestResult, sourceDir);
    }

    public void initiateRemoveIndexes(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
        // Determine the source directory.
        File sourceDir = new File(this.baseDir, "/"
                + harvestResult.getTargetInstanceOid() + "/"
                + harvestResult.getHarvestNumber());

        // Kick of the indexer.
        indexer.removeIndex(harvestResult, sourceDir);
    }

    public Boolean checkIndexing(Long harvestResultOid)
            throws DigitalAssetStoreException {
        return indexer.checkIndexing(harvestResultOid);
    }

    /**
     * @param indexer the indexer to set
     */
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     * @param fileMover the fileMover to set
     */
    public void setDasFileMover(DasFileMover fileMover) {
        this.dasFileMover = fileMover;
    }

    // Moves the ArchiveRecord stream past the HTTP status line;
    // checks if it starts with HTTP and ends with CRLF
    private void skipStatusLine(ArchiveRecord record) throws IOException {
        if (record.available() > 0) {
            int i;
            char[] proto = new char[4];
            for (int c = 0; c < 4; c++) {
                if ((i = record.read()) == -1) {
                    throw new IOException("Malformed HTTP Status-Line");
                }
                proto[c] = (char) i;
            }
            if (!"HTTP".equals(new String(proto))) {
                throw new IOException("Malformed HTTP Status-Line");
            }
            char c0 = '0';
            char c1;
            while ((i = record.read()) != -1) {
                c1 = (char) i;
                if (c0 == '\r' && c1 == '\n') {
                    break;
                }
                c0 = c1;
            }
        }
    }

    private void skipHeaders(ArchiveRecord record) throws IOException {
        HttpParser.parseHeaders(record, WARCConstants.DEFAULT_ENCODING);
    }

    public void setPageImagePrefix(String pageImagePrefix) {
        this.pageImagePrefix = pageImagePrefix;
    }

    public String getPageImagePrefix() {
        return pageImagePrefix;
    }

    public void setAqaReportPrefix(String aqaReportPrefix) {
        this.aqaReportPrefix = aqaReportPrefix;
    }

    public String getAqaReportPrefix() {
        return aqaReportPrefix;
    }

    /**
     * @param fileArchive the fileArchive to set
     */
    public void setFileArchive(FileArchive fileArchive) {
        this.fileArchive = fileArchive;
    }

    public VisualizationManager getVisualizationManager() {
        return visualizationManager;
    }

    public void setVisualizationManager(VisualizationManager visualizationManager) {
        this.visualizationManager = visualizationManager;
    }

    public NetworkMapClient getNetworkMapClient() {
        return networkMapClient;
    }

    public void setNetworkMapClient(NetworkMapClient networkMapClient) {
        this.networkMapClient = networkMapClient;
    }

    @Override
    public PruneAndImportCommandResult pruneAndImport(PruneAndImportCommandApply cmd) {
        PruneAndImportProcessor p = null;
        try {
            p = new PruneAndImportProcessor(visualizationManager, cmd, this);
        } catch (DigitalAssetStoreException e) {
            e.printStackTrace();
        }
        new Thread(p).start();

        PruneAndImportCommandResult result = new PruneAndImportCommandResult();
        result.setRespCode(VisualizationConstants.RESP_CODE_SUCCESS);
        result.setRespMsg("Modification task is accepted");
        return result;
    }

    @Override
    public void operateHarvestResultModification(String stage, String command, long targetInstanceId, int harvestNumber) throws DigitalAssetStoreException {
        PruneAndImportProcessor p = PruneAndImportProcessor.getProcessor(targetInstanceId, harvestNumber);

        if (command.equalsIgnoreCase("delete")) {
            if (p == null) {
                PruneAndImportCommandApply cmd = new PruneAndImportCommandApply();
                cmd.setTargetInstanceId(targetInstanceId);
                cmd.setNewHarvestResultNumber(harvestNumber);
                p = new PruneAndImportProcessor(visualizationManager, cmd, this);
            }
            p.delete(targetInstanceId, harvestNumber);
            return;
        }

        if (p == null) {
            log.error("Not running modification task, unable to: {}, {}, {}", command, targetInstanceId, harvestNumber);
            return;
        }

        if (command.equalsIgnoreCase("pause")) {
            p.pauseModification();
        } else if (command.equalsIgnoreCase("resume")) {
            p.resumeModification();
        } else if (command.equalsIgnoreCase("stop")) {
            p.stopModification();
        }
    }
}
