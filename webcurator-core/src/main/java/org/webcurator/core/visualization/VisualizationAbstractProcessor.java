package org.webcurator.core.visualization;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public abstract class VisualizationAbstractProcessor implements Callable<Boolean> {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationAbstractProcessor.class);
    protected WctCoordinatorClient wctCoordinatorClient;
    protected final String processorStage;
    protected final VisualizationProgressBar progressBar;
    protected final long targetInstanceId;
    protected final int harvestResultNumber;
    protected String fileDir; //Upload files
    protected String baseDir; //Harvest WARC files dir
    protected String logsDir; //log dir
    protected String reportsDir; //report dir
    protected boolean running = true;
    //    protected Semaphore stopped = new Semaphore(1);
    protected int status = HarvestResult.STATUS_SCHEDULED;
    protected VisualizationProcessorManager visualizationProcessorManager;

    public VisualizationAbstractProcessor(long targetInstanceId, int harvestResultNumber) throws DigitalAssetStoreException {
        this.processorStage = getProcessorStage();
        this.progressBar = new VisualizationProgressBar(processorStage, targetInstanceId, harvestResultNumber);
        this.targetInstanceId = targetInstanceId;
        this.harvestResultNumber = harvestResultNumber;
    }

    public void init(VisualizationProcessorManager visualizationProcessorManager, VisualizationDirectoryManager visualizationDirectoryManager, WctCoordinatorClient wctCoordinatorClient) {
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

    public void process() {

    }

    abstract public void processInternal() throws Exception;

    public void pauseTask() {
        this.status = HarvestResult.STATUS_PAUSED;
        try {
            this.wait();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            e.printStackTrace();
        }
    }

    public void resumeTask() {
        this.status = HarvestResult.STATUS_RUNNING;
        this.notifyAll();
    }

    public void terminateTask() {
        this.status = HarvestResult.STATUS_TERMINATED;

        this.running = false;
        terminateInternal();
//        try {
//            this.stopped.acquire();
//        } catch (InterruptedException e) {
//            log.error(e.getMessage());
//            e.printStackTrace();
//        }
    }

    abstract protected void terminateInternal();

    public void deleteTask() {
        this.terminateTask();
//        try {
//            this.stopped.acquire(); //wait until process ended
//        } catch (InterruptedException e) {
//            log.error("Acquire token failed when stop modification task, {}, {}", targetInstanceId, harvestResultNumber);
//            e.printStackTrace();
//            return;
//        }

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

    public int getState() {
        int state = HarvestResult.STATE_UNASSESSED;
        if (this.processorStage.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_MODIFYING)) {
            state = HarvestResult.STATE_MODIFYING;
        } else if (this.processorStage.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
            state = HarvestResult.STATE_INDEXING;
        }
        return state;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getTargetInstanceId() {
        return targetInstanceId;
    }

    public int getHarvestResultNumber() {
        return harvestResultNumber;
    }

    @Override
    public Boolean call() {
        try {
//            this.stopped.acquire();
            this.status = HarvestResult.STATUS_RUNNING;
            processInternal();
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
        } finally {
//            this.stopped.release();
            this.progressBar.clear();
            this.status = HarvestResult.STATUS_FINISHED;
            visualizationProcessorManager.finalise(this);
        }
        return false;
    }
}
