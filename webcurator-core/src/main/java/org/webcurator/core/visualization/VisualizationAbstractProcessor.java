package org.webcurator.core.visualization;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
import org.webcurator.core.util.WctUtils;
import org.webcurator.core.visualization.networkmap.service.NetworkMapService;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.HarvestResultDTO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;

public abstract class VisualizationAbstractProcessor implements Callable<Boolean> {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationAbstractProcessor.class);
    protected WctCoordinatorClient wctClient;
    protected NetworkMapService networkMapClient;
    protected final long targetInstanceId;
    protected final int harvestResultNumber;
    protected int state = HarvestResult.STATE_UNASSESSED;
    protected int status = HarvestResult.STATUS_SCHEDULED;
    protected String baseDir; //Harvest WARC files dir
    protected String fileDir; //Upload files
    protected String logsDir; //log dir
    protected String reportsDir; //report dir
    protected VisualizationProgressBar progressBar;
    protected VisualizationProcessorManager processorManager;
    protected String flag; //MOD or IDX
    protected String reportTitle;
    protected FileWriter logWriter;
    protected FileWriter reportWriter;
    protected List<VisualizationStatisticItem> statisticItems = new ArrayList<>();

    private boolean running = true;
    private final Semaphore running_blocker = new Semaphore(1);

    private boolean finished = false;

    public VisualizationAbstractProcessor(long targetInstanceId, int harvestResultNumber) {
        this.targetInstanceId = targetInstanceId;
        this.harvestResultNumber = harvestResultNumber;
    }

    public void init(VisualizationProcessorManager processorManager, VisualizationDirectoryManager directoryManager, WctCoordinatorClient wctClient, NetworkMapService networkMapClient) throws IOException {
        this.progressBar = new VisualizationProgressBar(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.processorManager = processorManager;
        this.baseDir = directoryManager.getBaseDir();
        this.fileDir = directoryManager.getUploadDir(targetInstanceId);
        this.logsDir = directoryManager.getBaseLogDir(targetInstanceId);
        this.reportsDir = directoryManager.getBaseReportDir(targetInstanceId);
        this.wctClient = wctClient;
        this.networkMapClient = networkMapClient;

        File fLogsDir = new File(logsDir);
        if (!fLogsDir.exists() && !fLogsDir.mkdirs()) {
            String err = String.format("Make directory failed: %s", fLogsDir.getAbsolutePath());
            log.error(err);
            throw new IOException(err);
        }
        this.logWriter = new FileWriter(new File(logsDir, directoryManager.getPatchLogFileName(getProcessorStage(), harvestResultNumber)), false);

        File fReportsDir = new File(reportsDir);
        if (!fReportsDir.exists() && !fReportsDir.mkdirs()) {
            String err = String.format("Make directory failed: %s", fReportsDir.getAbsolutePath());
            log.error(err);
            throw new IOException(err);
        }
        this.reportWriter = new FileWriter(new File(reportsDir, directoryManager.getPatchReportFileName(getProcessorStage(), harvestResultNumber)), false);

        this.statisticItems.clear();

        this.initInternal();
    }

    abstract protected void initInternal() throws IOException;

    abstract protected String getProcessorStage();

    public String getKey() {
        return VisualizationProcessorManager.getKey(targetInstanceId, harvestResultNumber);
    }

    public boolean process() {
        try {
            this.status = HarvestResult.STATUS_RUNNING;
            updateHarvestResultStatus();
            processInternal();
            this.close();
            return true;
        } catch (Exception e) {
            log.error("Failed to process", e);
            return false;
        } finally {
            if (this.progressBar != null) {
                this.progressBar.clear();
            }
            if (this.status == HarvestResult.STATUS_RUNNING) {
                this.status = HarvestResult.STATUS_FINISHED;
            }
            if (processorManager != null) {
                processorManager.finalise(this);
            }

            this.finished = true;
        }
    }

    abstract public void processInternal() throws Exception;

    public void pauseTask() {
        this.status = HarvestResult.STATUS_PAUSED;
        if (!this.running) {
            return;
        }
        try {
            this.running_blocker.acquire();
        } catch (InterruptedException e) {
            log.error("Failed to pause", e);
        }
        this.running = false;
        log.info("Paused");
    }

    public void resumeTask() {
        this.status = HarvestResult.STATUS_RUNNING;
        if (this.running) {
            return;
        }
        this.running = true;
        this.running_blocker.release(2);
        log.info("Resumed");
    }

    protected void tryBlock() {
        if (!running) {
            try {
                log.info("Going to wait");
                this.running_blocker.acquire();
                log.debug("Awake");
            } catch (InterruptedException e) {
                log.info("Failed to block", e);
            }
        }
    }

    public void writeLog(String content) {
        if (logWriter == null) {
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String time = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        try {
            logWriter.write(String.format("%s %s %s%s", this.flag, time, content, System.lineSeparator()));
        } catch (IOException e) {
            log.error("Failed to write log", e);
        }
    }

    public void writeReport() {
        if (reportWriter == null) {
            return;
        }

        try {
            reportWriter.write(this.reportTitle + System.lineSeparator());

            for (VisualizationStatisticItem item : statisticItems) {
                reportWriter.write(item.getPrintContent() + System.lineSeparator());
            }
        } catch (IOException e) {
            log.error("Failed to write report", e);
        }
    }

    public void terminateTask() {
        this.state = HarvestResult.STATE_ABORTED;
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
        if (!StringUtils.isEmpty(rootDir) && !StringUtils.isEmpty(dir)) {
            File toPurge = new File(rootDir, dir);
            delete(toPurge);
        }
    }

    protected void delete(String toPurge) {
        if (!StringUtils.isEmpty(toPurge)) {
            delete(new File(toPurge));
        }
    }

    protected void delete(File toPurge) {
        log.debug("About to purge dir " + toPurge.toString());
        WctUtils.cleanDirectory(toPurge);
    }

    public VisualizationProgressBar getProgress() {
        this.progressBar.setState(this.state);
        this.progressBar.setStatus(this.status);
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
        if (wctClient != null) {
            wctClient.dasUpdateHarvestResultStatus(getHarvestResultDTO());
        }
    }

    public HarvestResultDTO getHarvestResultDTO() {
        HarvestResultDTO hrDTO = new HarvestResultDTO();
        hrDTO.setTargetInstanceOid(this.targetInstanceId);
        hrDTO.setHarvestNumber(this.harvestResultNumber);
        hrDTO.setState(this.state);
        hrDTO.setStatus(this.status);
        return hrDTO;
    }

    @Override
    public Boolean call() {
        return process();
    }

    public void close() {
        try {
            if (statisticItems != null) {
                statisticItems.clear();
            }
            if (progressBar != null) {
                progressBar.clear();
            }
            if (logWriter != null) {
                logWriter.close();
            }
            if (reportWriter != null) {
                reportWriter.close();
            }
        } catch (IOException e) {
            log.error("Failed to close processor", e);
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }
}
