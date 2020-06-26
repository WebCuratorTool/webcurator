package org.webcurator.ui.tools.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.harvester.coordinator.HarvestAgentManager;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.TargetInstance;

@Component("harvestModificationHandler")
public class HarvestModificationHandler {
    private static final Logger log = LoggerFactory.getLogger(HarvestModificationHandler.class);
    @Autowired
    private TargetInstanceDAO targetInstanceDAO;
    @Autowired
    private HarvestCoordinator harvestCoordinator;
    @Autowired
    private HarvestAgentManager harvestAgentManager;
    @Autowired
    private DigitalAssetStore digitalAssetStore;

    public void clickPause(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus()!=HarvestResult.STATUS_RUNNING){
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.pausePatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "pause", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "pause", ti.getOid(), hr.getHarvestNumber());
        }else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_PAUSED);
        targetInstanceDAO.save(hr);
    }

    public void clickResume(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus()!=HarvestResult.STATUS_PAUSED){
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.resumePatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "resume", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "resume", ti.getOid(), hr.getHarvestNumber());
        }else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_PAUSED);
        targetInstanceDAO.save(hr);
    }

    public void clickStop(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus()!=HarvestResult.STATUS_RUNNING && hr.getStatus()!=HarvestResult.STATUS_PAUSED){
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.stopPatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "stop", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "stop", ti.getOid(), hr.getHarvestNumber());
        }else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_TERMINATED);
        targetInstanceDAO.save(hr);
    }

    public void clickDelete(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getStatus()!=HarvestResult.STATUS_SCHEDULED && hr.getStatus()!=HarvestResult.STATUS_TERMINATED){
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        if (hr.getState() == HarvestResult.STATE_CRAWLING) {
            harvestAgentManager.abortPatching(String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber()));
        } else if (hr.getState() == HarvestResult.STATE_MODIFYING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "delete", ti.getOid(), hr.getHarvestNumber());
        } else if (hr.getState() == HarvestResult.STATE_INDEXING) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "delete", ti.getOid(), hr.getHarvestNumber());
        }else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Delete the selected Harvest Result
        targetInstanceDAO.delete(hr);

        //Change the state of Target Instance to 'Harvested'
        if (ti.getPatchingHarvestResult() == null) {
            ti.setState(TargetInstance.STATE_HARVESTED);
            targetInstanceDAO.save(ti);
        }
    }

    public void clickStart(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        TargetInstance ti = targetInstanceDAO.load(targetInstanceId);
        HarvestResult hr = ti.getHarvestResult(harvestResultNumber);

        if (hr.getState() == HarvestResult.STATE_CRAWLING && hr.getStatus() == HarvestResult.STATUS_SCHEDULED) {
            harvestCoordinator.pushPruneAndImport(ti);
        } else if ((hr.getState() == HarvestResult.STATE_MODIFYING && hr.getStatus() == HarvestResult.STATUS_SCHEDULED)
                || (hr.getState() == HarvestResult.STATE_CRAWLING && hr.getStatus() == HarvestResult.STATUS_FINISHED)) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_MODIFYING, "start", ti.getOid(), hr.getHarvestNumber());
            hr.setState(HarvestResult.STATE_MODIFYING);
        } else if ((hr.getState() == HarvestResult.STATE_INDEXING && hr.getStatus() == HarvestResult.STATUS_SCHEDULED)
                || (hr.getState() == HarvestResult.STATE_MODIFYING && hr.getStatus() == HarvestResult.STATUS_FINISHED)) {
            digitalAssetStore.operateHarvestResultModification(HarvestResult.PATCH_STAGE_TYPE_INDEXING, "start", ti.getOid(), hr.getHarvestNumber());
            hr.setState(HarvestResult.STATE_INDEXING);
        } else {
            throw new DigitalAssetStoreException(String.format("Incorrect state: %d, status: %d", hr.getState(), hr.getStatus()));
        }

        //Change the status of Harvest Result
        hr.setStatus(HarvestResult.STATUS_RUNNING);
        targetInstanceDAO.save(hr);
    }
}
