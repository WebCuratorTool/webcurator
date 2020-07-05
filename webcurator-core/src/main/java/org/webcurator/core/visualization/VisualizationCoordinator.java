package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class VisualizationCoordinator {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationCoordinator.class);
    protected String flag; //MOD or IDX
    protected String reportTitle;
    protected FileWriter logWriter;
    protected FileWriter reportWriter;
    protected List<VisualizationStatisticItem> statisticItems = new ArrayList<>();
    protected VisualizationProgressBar progressBar;

    private boolean running = true;
    private final Semaphore running_blocker = new Semaphore(1);

    public void init(String logsDir, String reportsDir, VisualizationProgressBar progressBar) throws IOException {
        File fLogsDir = new File(logsDir);
        if (!fLogsDir.exists()) {
            fLogsDir.mkdirs();
        }
        this.logWriter = new FileWriter(new File(logsDir, "running.log"), false);

        File fReportsDir = new File(reportsDir);
        if (!fReportsDir.exists()) {
            fReportsDir.mkdirs();
        }
        this.reportWriter = new FileWriter(new File(reportsDir, "report.txt"), false);

        this.statisticItems.clear();

        this.progressBar = progressBar;
    }

    public void close() throws IOException {
        this.logWriter.close();
        this.reportWriter.close();
        this.statisticItems.clear();
    }

    public void pause() {
        try {
            this.running_blocker.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.running = false;
        System.out.println("Paused");
    }

    public void resume() {
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

    public static List<File> grepWarcFiles(String sPath) {
        return grepWarcFiles(new File(sPath));
    }

    public static List<File> grepWarcFiles(File directory) {
        //Checking validation
        if (!directory.exists() || !directory.isDirectory()) {
            return new ArrayList<File>();
        }

        File[] fileAry = directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".warc") || name.toLowerCase().endsWith(".warc.gz");
            }
        });

        if (fileAry == null) {
            return new ArrayList<File>();
        }
        return Arrays.asList(fileAry);
    }
}
