package org.webcurator.core.visualization;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.coordinator.WctCoordinatorClient;
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

    public VisualizationAbstractProcessor(long targetInstanceId, int harvestResultNumber) {
        this.targetInstanceId = targetInstanceId;
        this.harvestResultNumber = harvestResultNumber;
    }

    public void init(VisualizationProcessorManager processorManager, VisualizationDirectoryManager directoryManager, WctCoordinatorClient wctClient) throws IOException {
        this.progressBar = new VisualizationProgressBar(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.processorManager = processorManager;
        this.baseDir = directoryManager.getBaseDir();
        this.fileDir = directoryManager.getUploadDir(targetInstanceId);
        this.logsDir = directoryManager.getPatchLogDir(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.reportsDir = directoryManager.getPatchReportDir(getProcessorStage(), targetInstanceId, harvestResultNumber);
        this.wctClient = wctClient;

        File fLogsDir = new File(logsDir);
        if (!fLogsDir.exists() && !fLogsDir.mkdirs()) {
            String err = String.format("Make directory failed: %s", fLogsDir.getAbsolutePath());
            log.error(err);
            throw new IOException(err);
        }
        this.logWriter = new FileWriter(new File(logsDir, "running.log"), false);

        File fReportsDir = new File(reportsDir);
        if (!fReportsDir.exists() && !fReportsDir.mkdirs()) {
            String err = String.format("Make directory failed: %s", fReportsDir.getAbsolutePath());
            log.error(err);
            throw new IOException(err);
        }
        this.reportWriter = new FileWriter(new File(reportsDir, "report.txt"), false);

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
            log.error(e.getMessage());
            return false;
        } finally {
            this.progressBar.clear();
            this.status = HarvestResult.STATUS_FINISHED;
            processorManager.finalise(this);
        }
    }

    abstract public void processInternal() throws Exception;

    public void pauseTask() {
        try {
            this.running_blocker.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.running = false;
        System.out.println("Paused");
    }

    public void resumeTask() {
        this.running = true;
        this.running_blocker.release(2);
        System.out.println("Resumed");
    }

    protected void tryBlock() {
        if (!running) {
            try {
                System.out.println("Going to wait");
                this.running_blocker.acquire();
                System.out.println("Awake");
            } catch (InterruptedException e) {
                e.printStackTrace();
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
            log.error(e.getMessage());
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
            e.printStackTrace();
        }
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
        wctClient.dasUpdateHarvestResultStatus(getHarvestResultDTO());
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
            log.error(e.getMessage());
        }
    }
}
