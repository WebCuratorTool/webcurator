package org.webcurator.core.coordinator;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.webcurator.core.harvester.coordinator.HarvestCoordinator;
import org.webcurator.core.store.coordinator.DigitalAssetStoreCoordinator;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyResult;
import org.webcurator.core.visualization.modification.metadata.ModifyRowFullData;
import org.webcurator.domain.*;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.dto.QueuedTargetInstanceDTO;
import org.webcurator.core.reader.*;
import org.webcurator.domain.model.dto.SeedHistorySetDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("all")
public class MockWctCoordinator extends WctCoordinator {
    private static Log log = LogFactory.getLog(MockWctCoordinator.class);
    private LogReader logReader = null;
    private boolean queuePaused = false;
    private Boolean reIndexHarvestResultReturnValue = false;
    private TargetInstanceDAO targetInstanceDao = null;
    private List<HarvestResult> removedIndexes = new ArrayList<HarvestResult>();

    public MockWctCoordinator() {
        logReader = new MockLogReader();
    }

    public void abort(TargetInstance targetInstance) {
        // TODO Auto-generated method stub

    }

    public void checkForBandwidthTransition() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean pushPruneAndImport(long targetInstanceId, int harvestResultNumber) {
        return false;
    }

    public void completeArchiving(Long targetInstanceOid, String archiveIID) {
        // TODO Auto-generated method stub

    }

    public Integer countLogLines(TargetInstance targetInstance, String fileName) {
        return logReader.countLines(targetInstance.getOid().toString(), fileName);
    }

    public void failedArchiving(Long targetInstanceOid, String message) {
        // TODO Auto-generated method stub

    }

    public long getCurrentGlobalMaxBandwidth() {
        return 500;
    }

    public Integer getFirstLogLineAfterTimeStamp(TargetInstance targetInstance,
                                                 String fileName, Long timestamp) {
        return logReader.findFirstLineAfterTimeStamp(targetInstance.getOid().toString(),
                fileName, timestamp);
    }

    public Integer getFirstLogLineBeginning(TargetInstance targetInstance,
                                            String fileName, String match) {
        return logReader.findFirstLineBeginning(targetInstance.getOid().toString(),
                fileName, match);
    }

    public Integer getFirstLogLineContaining(TargetInstance targetInstance,
                                             String fileName, String match) {
        return logReader.findFirstLineContaining(targetInstance.getOid().toString(),
                fileName, match);
    }

    public HashMap<String, HarvestAgentStatusDTO> getHarvestAgents() {
        return new HashMap<String, HarvestAgentStatusDTO>();
    }

    public List<String> getLog(TargetInstance targetInstance, String fileName,
                               int startLine, int noOfLines) {
        return logReader.get(targetInstance.getOid().toString(), fileName, startLine,
                noOfLines);
    }

    public List<String> getHopPath(TargetInstance targetInstance, String fileName,
                                   String url) {
        return logReader.getHopPath(targetInstance.getOid().toString(), "1234", fileName, url);
    }

    public List<String> getLogLinesByRegex(TargetInstance targetInstance,
                                           String fileName, int noOfLines, String regex,
                                           boolean prependLineNumbers) {
        return logReader.getByRegularExpression(targetInstance.getOid().toString(), fileName,
                regex, "zzzzzzzzz", prependLineNumbers, 0, noOfLines);
    }

    public File getLogfile(TargetInstance targetInstance, String filename) {
        return logReader.retrieveLogfile(targetInstance.getOid().toString(),
                filename);
    }

    public int getMaxBandwidthPercent() {
        return 50;
    }

    public void harvest(TargetInstance targetInstance,
                        HarvestAgentStatusDTO harvestAgent) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean patchHarvest(TargetInstance targetInstance, HarvestResult hr, HarvestAgentStatusDTO harvestAgentStatusDTO) {
        return false;
    }

    @Override
    public ModifyResult patchHarvest(ModifyApplyCommand cmd) {
        return null;
    }

    public void harvestOrQueue(QueuedTargetInstanceDTO targetInstance) {
        // TODO Auto-generated method stub

    }

    public List<String> headLog(TargetInstance targetInstance, String fileName,
                                int noOfLines) {
        return logReader.get(targetInstance.getOid().toString(), fileName, 1,
                noOfLines);
    }

    public boolean isMiniumBandwidthAvailable(TargetInstance targetInstance) {
        return (targetInstance.getAllocatedBandwidth() < this.getCurrentGlobalMaxBandwidth());
    }

    public boolean isQueuePaused() {
        return queuePaused;
    }

    public List<LogFilePropertiesDTO> listLogFileAttributes(
            TargetInstance targetInstance) {
        return logReader.listLogFileAttributes(targetInstance.getOid().toString());
    }

    public List<String> listLogFiles(TargetInstance targetInstance) {
        return logReader.listLogFiles(targetInstance.getOid().toString());
    }

    public void pause(TargetInstance targetInstance) {
        // TODO Auto-generated method stub
    }

    public void pauseAll() {
        // TODO Auto-generated method stub
    }

    public void pauseQueue() {
        queuePaused = true;
    }

    public void processSchedule() {
        // TODO Auto-generated method stub
    }

    public void purgeAbortedTargetInstances() {
        // TODO Auto-generated method stub
    }

    public void purgeDigitalAssets() {
        // TODO Auto-generated method stub
    }

    public void resume(TargetInstance targetInstance) {
        // TODO Auto-generated method stub
    }

    public void resumeAll() {
        // TODO Auto-generated method stub
    }

    public void resumeQueue() {
        queuePaused = false;
    }

    public void stop(TargetInstance targetInstance) {
        // TODO Auto-generated method stub
    }

    public List<String> tailLog(TargetInstance targetInstance, String fileName,
                                int noOfLines) {
        return logReader.tail(targetInstance.getOid().toString(), fileName, noOfLines);
    }

    public void updateProfileOverrides(TargetInstance targetInstance) {
        // TODO Auto-generated method stub
    }

    public void harvestComplete(HarvestResultDTO result) {
        // TODO Auto-generated method stub
    }

    public void heartbeat(HarvestAgentStatusDTO status) {
        // TODO Auto-generated method stub
    }

    @Override
    public void requestRecovery(HarvestAgentStatusDTO harvestAgentStatusDTO) {
    }

    @Override
    public void recoverHarvests(String baseUrl, String Service) {
        // TODO Auto-generated method stub
    }

    public void notification(Long targetInstanceOid, int notificationCategory,
                             String messageType) {
        // TODO Auto-generated method stub
    }

    public void delete(BandwidthRestriction bandwidthRestriction) {
        // TODO Auto-generated method stub
    }

    public BandwidthRestriction getBandwidthRestriction(Long oid) {
        // TODO Auto-generated method stub
        return null;
    }

    public BandwidthRestriction getBandwidthRestriction(String day, Date time) {
        // TODO Auto-generated method stub
        return null;
    }

    public HashMap<String, List<BandwidthRestriction>> getBandwidthRestrictions() {
        // TODO Auto-generated method stub
        return null;
    }

    public void saveOrUpdate(BandwidthRestriction bandwidthRestriction) {
        // TODO Auto-generated method stub
    }

    public void notification(String subject, int notificationCategory,
                             String message) {
        // TODO Auto-generated method stub
    }

    public Long createHarvestResult(HarvestResultDTO harvestResultDTO) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void finaliseIndex(long targetInstanceId, int harvestNumber, boolean indexResult) {

    }

    @Override
    public void notifyAQAComplete(String aqaId) {

    }

    public void finaliseIndex(Long harvestResultOid) {
        // TODO Auto-generated method stub
    }

    public void finaliseAbortedIndex(Long harvestResultOid) {
        // TODO Auto-generated method stub
    }

    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }

    public void setReIndexHarvestResultReturnValue(Boolean reIndexHarvestResultReturnValue) {
        this.reIndexHarvestResultReturnValue = reIndexHarvestResultReturnValue;
    }

    public Boolean reIndexHarvestResult(HarvestResult origHarvestResult) {
        if (reIndexHarvestResultReturnValue) {
            TargetInstance ti = origHarvestResult.getTargetInstance();

            HarvestResultDTO hr = new HarvestResultDTO();
            hr.setCreationDate(new Date());
            hr.setTargetInstanceOid(ti.getOid());
            hr.setProvenanceNote(origHarvestResult.getProvenanceNote());
            hr.setHarvestNumber(origHarvestResult.getHarvestNumber());
            HarvestResult newHarvestResult = new HarvestResult(hr, ti);

            origHarvestResult.setState(HarvestResult.STATE_ABORTED);
            newHarvestResult.setState(HarvestResult.STATE_INDEXING);

            List<HarvestResult> hrs = ti.getHarvestResults();
            hrs.add(newHarvestResult);
            ti.setHarvestResults(hrs);

            ti.setState(TargetInstance.STATE_HARVESTED);

            targetInstanceDao.save(newHarvestResult);
            targetInstanceDao.save(ti);
        }
        return reIndexHarvestResultReturnValue;
    }

    public void removeIndexes(TargetInstance ti) {
        List<HarvestResult> results = ti.getHarvestResults();
        if (results != null) {
            Iterator<HarvestResult> it = results.iterator();
            while (it.hasNext()) {
                HarvestResult result = it.next();
                if (result.getState() != HarvestResult.STATE_REJECTED) {
                    removeIndexes(result);
                }
            }
        }

    }

    public void removeIndexes(HarvestResult hr) {
        log.info("Removing indexes for TargetInstance " + hr.getTargetInstance().getOid() + " HarvestNumber " + hr.getHarvestNumber());
        removedIndexes.add(hr);
    }

    @Override
    public void runQaRecommentationService(TargetInstance ti) {

    }

    public List<HarvestResult> getRemovedIndexes() {
        return removedIndexes;
    }


    @Override
    public void pauseAgent(String agentName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void resumeAgent(String agentName) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHarvestOptimizationEnabled(boolean optimizeEnabled) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isHarvestOptimizationEnabled() {
        return false;
    }

    @Override
    public int getHarvestOptimizationLookAheadHours() {
        return 24;
    }

    @Override
    public SeedHistorySetDTO dasQuerySeedHistory(long targetInstanceOid, int harvestNumber) {
        return null;
    }

    @Override
    public void dasModificationComplete(long targetInstanceOid, int harvestNumber) {

    }

    @Override
    public void dasDownloadFile(long targetInstanceOid, int harvestNumber, String fileName, HttpServletRequest req, HttpServletResponse rsp) throws IOException {

    }

    @Override
    public void dasHeartbeat(List<HarvestResultDTO> harvestResultDTOList) {

    }

    @Override
    public void dasUpdateHarvestResultStatus(HarvestResultDTO hrDTO) {

    }

    @Override
    public ModifyRowFullData uploadFile(long job, int harvestResultNumber, ModifyRowFullData cmd) {
        return null;
    }

    @Override
    public ModifyResult checkFiles(long job, int harvestResultNumber, List<ModifyRowFullData> items) {
        return null;
    }

    @Override
    public ModifyResult applyPruneAndImport(ModifyApplyCommand cmd) {
        return null;
    }
}
