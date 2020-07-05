package org.webcurator.core.visualization;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public abstract class VisualizationAbstractProcessor implements Callable<Boolean> {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationAbstractProcessor.class);
    protected WctCoordinatorClient wctCoordinatorClient;
    protected final long targetInstanceId;
    protected final int harvestResultNumber;
    protected int state = HarvestResult.STATE_UNASSESSED;
    protected int status = HarvestResult.STATUS_SCHEDULED;
    protected String baseDir; //Harvest WARC files dir
    protected String fileDir; //Upload files
    protected String logsDir; //log dir
    protected String reportsDir; //report dir
    protected VisualizationProgressBar progressBar;
    protected VisualizationProcessorManager visualizationProcessorManager;
//    private boolean running = true;
//    private final Semaphore running_blocker = new Semaphore(1);

    public VisualizationAbstractProcessor(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        this.targetInstanceId = targetInstanceId;
        this.harvestResultNumber = harvestResultNumber;
    }

    public void init(VisualizationProcessorManager visualizationProcessorManager, VisualizationDirectoryManager visualizationDirectoryManager, WctCoordinatorClient wctCoordinatorClient) {
        this.progressBar = new VisualizationProgressBar(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.visualizationProcessorManager = visualizationProcessorManager;
        this.baseDir = visualizationDirectoryManager.getBaseDir();
        this.fileDir = visualizationDirectoryManager.getUploadDir(targetInstanceId);
        this.logsDir = visualizationDirectoryManager.getPatchLogDir(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.reportsDir = visualizationDirectoryManager.getPatchReportDir(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.wctCoordinatorClient = wctCoordinatorClient;
        this.initInternal();
    }

    abstract protected void initInternal();

    abstract protected String getProcessorStage();

    public String getKey() {
        return VisualizationProcessorManager.getKey(targetInstanceId, harvestResultNumber);
    }

    public boolean process() {
        try {
            this.status = HarvestResult.STATUS_RUNNING;
            updateHarvestResultStatus();
            processInternal();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        } finally {
            this.progressBar.clear();
            this.status = HarvestResult.STATUS_FINISHED;
            visualizationProcessorManager.finalise(this);
        }
    }

    abstract public void processInternal() throws Exception;

    public void pauseTask() {
        this.status = HarvestResult.STATUS_PAUSED;
//        synchronized (this.running_blocker) {
//            if (this.running) {
//                try {
//                    this.running_blocker.acquire();
//                } catch (InterruptedException e) {
//                    log.error(e.getMessage());
//                }
//                this.running = false;
//            }
//        }
        pauseInternal();
        updateHarvestResultStatus();
    }

    abstract protected void pauseInternal();

    public void resumeTask() {
        this.status = HarvestResult.STATUS_RUNNING;
//        synchronized (this.running_blocker) {
//            if (!this.running) {
//                this.running = true;
//                this.running_blocker.release();
//            }
//        }
        resumeInternal();
        updateHarvestResultStatus();
    }

    abstract protected void resumeInternal();

    protected void tryBlock() {
//        if (!this.running) {
//            try {
//                this.running_blocker.acquire();
//            } catch (InterruptedException e) {
//                log.error(e.getMessage());
//            }
//        }
    }

    public void terminateTask() {
        this.status = HarvestResult.STATUS_TERMINATED;
        terminateInternal();
        updateHarvestResultStatus();
    }

    abstract protected void terminateInternal();

    public void deleteTask() {
        //delete logs, reports
        delete(this.logsDir);
        delete(this.reportsDir);
        deleteInternal();
    }

    abstract public void deleteInternal();

    protected void delete(String rootDir, String dir) {
        File toPurge = new File(rootDir, dir);
        delete(toPurge);
    }

    protected void delete(String toPurge) {
        delete(new File(toPurge));
    }

    protected void delete(File toPurge) {
        log.debug("About to purge dir " + toPurge.toString());
        try {
            FileUtils.deleteDirectory(toPurge);
        } catch (IOException e) {
            log.warn("Unable to purge target instance folder: " + toPurge.getAbsolutePath());
        }
    }

    public VisualizationProgressBar getProgress() {
        return this.progressBar;
    }

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void updateHarvestResultStatus() {
        HarvestResultDTO hrDTO = new HarvestResultDTO();
        hrDTO.setTargetInstanceOid(this.targetInstanceId);
        hrDTO.setHarvestNumber(this.harvestResultNumber);
        hrDTO.setState(this.state);
        hrDTO.setStatus(this.status);

        wctCoordinatorClient.dasUpdateHarvestResultStatus(hrDTO);
    }

    @Override
    public Boolean call() {
        return process();
    }
}
