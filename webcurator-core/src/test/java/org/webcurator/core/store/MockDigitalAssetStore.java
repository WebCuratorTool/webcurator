package org.webcurator.core.store;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.domain.model.core.CustomDepositFormCriteriaDTO;
import org.webcurator.domain.model.core.CustomDepositFormResultDTO;
import org.webcurator.domain.model.core.HarvestResultDTO;

public class MockDigitalAssetStore implements DigitalAssetStore {

    private static Log log = LogFactory.getLog(MockDigitalAssetStore.class);
    private Boolean checkIndexing = false;
    private List<HarvestResultDTO> removedIndexes = new ArrayList<HarvestResultDTO>();

    public MockDigitalAssetStore() {
        // TODO Auto-generated constructor stub
    }

    public List<Header> getHeaders(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub
        return new ArrayList<>();
    }

    public Path getResource(long targetInstanceId, int harvestResultNumber, String resourceUrl) throws DigitalAssetStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] getSmallResource(long targetInstanceId, int harvestResultNumber, String resourceUrl)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub
        return null;
    }

    public void initiateIndexing(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
    }

    public void initiateRemoveIndexes(HarvestResultDTO harvestResult)
            throws DigitalAssetStoreException {
        log.info("Removing indexes for TargetInstance " + harvestResult.getTargetInstanceOid() + " HarvestNumber " + harvestResult.getHarvestNumber());
        removedIndexes.add(harvestResult);
    }

    public void purge(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void purgeAbortedTargetInstances(List<String> targetInstanceNames)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void save(String targetInstanceName, List<Path> paths)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void save(String targetInstanceName, Path path)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void save(String targetInstanceName, String directory, List<Path> paths)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void save(String targetInstanceName, String directory, Path path)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public void submitToArchive(String targetInstanceOid, String sip,
                                Map attributes, int harvestNumber)
            throws DigitalAssetStoreException {
        // TODO Auto-generated method stub

    }

    public Boolean checkIndexing(Long harvestResultOid) throws DigitalAssetStoreException {
        return checkIndexing;
    }

    public void setCheckIndexingReturn(Boolean checkIndexing) {
        this.checkIndexing = checkIndexing;
    }

    public List<HarvestResultDTO> getRemovedIndexes() {
        return removedIndexes;
    }

    public CustomDepositFormResultDTO getCustomDepositFormDetails(CustomDepositFormCriteriaDTO criteria) throws DigitalAssetStoreException {
        return null;
    }

    @Override
    public ModifyResult initialPruneAndImport(ModifyApplyCommand cmd) {
        return new ModifyResult();
    }

    @Override
    public void operateHarvestResultModification(String stage, String command, long targetInstanceId, int harvestNumber) throws DigitalAssetStoreException {
        return;
    }

    /**
     * Create live or screenshots for each seed in the harvest
     *
     * @param identifiers
     * @throws DigitalAssetStoreException
     */
    public void createScreenshots(Map identifiers) throws DigitalAssetStoreException {
    }

    ;

}
