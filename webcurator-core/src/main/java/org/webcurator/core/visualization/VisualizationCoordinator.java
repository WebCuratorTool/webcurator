package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VisualizationCoordinator {
    protected static final Logger log = LoggerFactory.getLogger(VisualizationCoordinator.class);
    protected String flag; //MOD or IDX
    protected String reportTitle;
    protected FileWriter log_modification;
    protected FileWriter report_modification;
    protected List<VisualizationStatisticItem> statisticItems = new ArrayList<>();

    public void init(String logsDir, String reportsDir) throws IOException {
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
    }

    public void close() throws IOException {
        this.log_modification.close();
        this.report_modification.close();
        this.statisticItems.clear();
    }

    public void writeLog(String content) {
        if (log_modification==null){
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
        if (report_modification==null){
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
}
