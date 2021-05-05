package org.webcurator.core.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.visualization.VisualizationAbstractApplyCommand;
import org.webcurator.core.visualization.modification.metadata.ModifyApplyCommand;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapApplyCommand;
import org.webcurator.domain.model.core.HarvestResult;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PatchUtil {
    private static final Logger log = LoggerFactory.getLogger(PatchUtil.class);
    private static final String DIR_JOBS = "jobs";
    private static final String DIR_HISTORY = "history";

    public static final PatchStageProcessor modifier = new PatchStageProcessor(HarvestResult.PATCH_STAGE_TYPE_MODIFYING);
    public static final PatchStageProcessor indexer = new PatchStageProcessor(HarvestResult.PATCH_STAGE_TYPE_INDEXING);

    public static String getPatchJobName(long targetInstanceId, int harvestResultNumber) {
        if (harvestResultNumber == 1) {
            return Long.toString(targetInstanceId);
        } else {
            return String.format("mod_%d_%d", targetInstanceId, harvestResultNumber);
        }
    }

    public static List<File> listWarcFiles(String sPath) {
        return listWarcFiles(new File(sPath));
    }

    public static List<File> listWarcFiles(File directory) {
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

    public static class PatchStageProcessor {
        private final String stage;

        public PatchStageProcessor(String stage) {
            this.stage = stage;
        }

        public String getPatchJobName(long targetInstanceId, int harvestResultNumber) {
            return String.format("%s_%d_%d", stage, targetInstanceId, harvestResultNumber);
        }

        public void savePatchJob(String baseDir, VisualizationAbstractApplyCommand cmd) throws IOException {
            //Save cmd to local directory
            savePatchJob(baseDir, cmd, cmd.getTargetInstanceId(), cmd.getNewHarvestResultNumber());
        }

        public void savePatchJob(String baseDir, VisualizationAbstractApplyCommand cmd, long targetInstanceId, int harvestResultNumber) throws IOException {
            //Save cmd to local directory
            File jobDirectory = new File(baseDir, DIR_JOBS);
            if (!jobDirectory.exists()) {
                boolean mkdirResult = jobDirectory.mkdir();
                if (!mkdirResult) {
                    throw new IOException("Failed to make dir: " + jobDirectory.getAbsolutePath());
                }
            }

            File cmdFile = new File(jobDirectory, getPatchJobName(targetInstanceId, harvestResultNumber) + ".json");
            if (cmdFile.exists() && cmdFile.delete()) {
                log.warn("Existing file is deleted: {}", cmdFile.getAbsolutePath());
            }

            ObjectMapper objectMapper = new ObjectMapper();

            byte[] cmdJsonContent = objectMapper.writeValueAsBytes(cmd);
            Files.write(cmdFile.toPath(), cmdJsonContent);
        }

        public VisualizationAbstractApplyCommand readPatchJob(String baseDir, long targetInstanceId, int harvestResultNumber) {
            File jobDirectory = new File(baseDir, DIR_JOBS);
            File cmdFile = new File(jobDirectory, getPatchJobName(targetInstanceId, harvestResultNumber) + ".json");


            byte[] cmdJsonContent = new byte[0];
            try {
                cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                if (this.stage.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
                    return objectMapper.readValue(cmdJsonContent, NetworkMapApplyCommand.class);
                } else {
                    return objectMapper.readValue(cmdJsonContent, ModifyApplyCommand.class);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }

        public VisualizationAbstractApplyCommand readHistoryPatchJob(String baseDir, long targetInstanceId, int harvestResultNumber) {
            File historyDirectory = new File(baseDir, DIR_HISTORY);
            File cmdFile = new File(historyDirectory, getPatchJobName(targetInstanceId, harvestResultNumber) + ".json");

            byte[] cmdJsonContent = new byte[0];
            try {
                cmdJsonContent = Files.readAllBytes(cmdFile.toPath());
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                if (this.stage.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
                    return objectMapper.readValue(cmdJsonContent, NetworkMapApplyCommand.class);
                } else {
                    return objectMapper.readValue(cmdJsonContent, ModifyApplyCommand.class);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                return null;
            }
        }

        public List<VisualizationAbstractApplyCommand> listPatchJob(String baseDir) throws IOException {
            List<VisualizationAbstractApplyCommand> list = new ArrayList<>();
            File jobDirectory = new File(baseDir, DIR_JOBS);
            if (!jobDirectory.exists()) {
                log.info("Job directory does not exist: {}", jobDirectory.getAbsolutePath());
                return list;
            }

            File[] jobFiles = jobDirectory.listFiles();
            if (jobFiles == null) {
                log.info("There is no jobs: {}", jobDirectory.getAbsolutePath());
                return list;
            }

            for (File jobFile : jobFiles) {
                if (!jobFile.isFile() || !jobFile.getName().startsWith(this.stage) || !jobFile.getName().endsWith(".json")) {
                    continue;
                }

                byte[] cmdJsonContent = Files.readAllBytes(jobFile.toPath());
                ObjectMapper objectMapper = new ObjectMapper();

                VisualizationAbstractApplyCommand cmd = null;
                if (this.stage.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_INDEXING)) {
                    cmd = objectMapper.readValue(cmdJsonContent, NetworkMapApplyCommand.class);
                } else {
                    cmd = objectMapper.readValue(cmdJsonContent, ModifyApplyCommand.class);
                }
                list.add(cmd);
            }

            return list;
        }

        public boolean moveJob2History(String baseDir, long targetInstanceId, int harvestResultNumber) {
            File jobDirectory = new File(baseDir, DIR_JOBS);
            File historyDirectory = new File(baseDir, DIR_HISTORY);
            if (!historyDirectory.exists()) {
                boolean mkdirResult = historyDirectory.mkdir();
                if (!mkdirResult) {
                    log.error("Failed to make dir: {}", historyDirectory.getAbsolutePath());
                    return false;
                }
            }

            String jobFileName = getPatchJobName(targetInstanceId, harvestResultNumber) + ".json";

            File jobFile = new File(jobDirectory, jobFileName);
            if (!jobFile.exists()) {
                log.error("File does not exist: {}", jobFile.getAbsolutePath());
                return false;
            }

            File historyFile = new File(historyDirectory, jobFileName);
            if (historyFile.exists() && historyFile.delete()) {
                log.warn("Existing file is deleted: {}", historyFile.getAbsolutePath());
            }

            return jobFile.renameTo(historyFile);
        }

        public boolean deleteJob(String baseDir, long targetInstanceId, int harvestResultNumber) {
            File historyDirectory = new File(baseDir, DIR_JOBS);
            String jobFileName = getPatchJobName(targetInstanceId, harvestResultNumber) + ".json";
            File historyFile = new File(historyDirectory, jobFileName);
            return historyFile.delete();
        }

        public boolean deleteHistoryJob(String baseDir, long targetInstanceId, int harvestResultNumber) {
            File historyDirectory = new File(baseDir, DIR_HISTORY);
            String jobFileName = getPatchJobName(targetInstanceId, harvestResultNumber) + ".json";
            File historyFile = new File(historyDirectory, jobFileName);
            return historyFile.delete();
        }
    }
}
