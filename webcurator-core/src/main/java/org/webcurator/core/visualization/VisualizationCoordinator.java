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

public class VisualizationCoordinator {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationCoordinator.class);
    protected String flag; //MOD or IDX
    protected String reportTitle;
    protected FileWriter log_modification;
    protected FileWriter report_modification;
    protected List<VisualizationStatisticItem> statisticItems = new ArrayList<>();
    protected VisualizationProgressBar progressBar;

    public void init(String logsDir, String reportsDir, VisualizationProgressBar progressBar) throws IOException {
        File fLogsDir = new File(logsDir);
        if (!fLogsDir.exists()) {
            fLogsDir.mkdirs();
        }
        this.log_modification = new FileWriter(new File(logsDir, "running.log"), false);

        File fReportsDir = new File(reportsDir);
        if (!fReportsDir.exists()) {
            fReportsDir.mkdirs();
        }
        this.report_modification = new FileWriter(new File(reportsDir, "report.txt"), false);

        this.statisticItems.clear();

        this.progressBar = progressBar;
    }

    public void close() throws IOException {
        this.log_modification.close();
        this.report_modification.close();
        this.statisticItems.clear();
    }

    public void writeLog(String content) {
        if (log_modification == null) {
            return;
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        String time = localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        try {
            log_modification.write(String.format("%s %s %s%s", this.flag, time, content, System.lineSeparator()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void writeReport() {
        if (report_modification == null) {
            return;
        }

        try {
            report_modification.write(this.reportTitle + System.lineSeparator());

            for (VisualizationStatisticItem item : statisticItems) {
                report_modification.write(item.toString() + System.lineSeparator());
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
