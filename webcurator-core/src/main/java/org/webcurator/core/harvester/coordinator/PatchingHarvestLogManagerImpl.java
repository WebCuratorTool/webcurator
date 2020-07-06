package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.core.util.PatchUtil;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatchingHarvestLogManagerImpl implements PatchingHarvestLogManager {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private HarvestAgentManager harvestAgentManager;
    private DigitalAssetStoreFactory digitalAssetStoreFactory;
    private String type = "";

    @Override
    public File getLogfile(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFilename) {
        if (aFilename == null || aFilename.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Tail Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }

        return logReader.retrieveLogfile(jobName, aFilename);
    }

    @Override
    public List<String> getLogLinesByRegex(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines, String aRegex, boolean prependLineNumbers) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Get log lines by regex failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }

        return logReader.getByRegularExpression(jobName, aFileName, aRegex, "zzzzzzzzz", prependLineNumbers, 0,
                aNoOfLines);
    }

    @Override
    public Integer getFirstLogLineAfterTimeStamp(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, Long timestamp) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Get First Log Line After Timestamp failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineAfterTimeStamp(jobName, aFileName, timestamp);
    }

    @Override
    public Integer getFirstLogLineContaining(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, String match) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Get First Log Line Containing failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineContaining(jobName, aFileName, match);
    }

    @Override
    public Integer getFirstLogLineBeginning(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, String match) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Get First Log Line Beginning failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineBeginning(jobName, aFileName, match);
    }

    @Override
    public List<String> getLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aStartLine, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Get Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.get(jobName, aFileName, aStartLine, aNoOfLines);
    }

    @Override
    public Integer countLogLines(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Count Log Lines Failed. Failed to find the log Reader for the Job {}.", jobName);
            return 0;
        }
        return logReader.countLines(jobName, aFileName);
    }

    @Override
    public List<String> headLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Head Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return Collections.singletonList("");
        }
        return logReader.get(jobName, aFileName, 1, aNoOfLines);
    }

    @Override
    public List<LogFilePropertiesDTO> listLogFileAttributes(long targetInstanceId, int harvestResultNumber, int harvestResultState) {
        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("listLogFileAttributes Failed. Failed to find the Log Reader for the Job {}.", jobName);
            return new ArrayList<>();
        }
        return logReader.listLogFileAttributes(jobName);
    }

    @Override
    public List<String> listLogFiles(long targetInstanceId, int harvestResultNumber, int harvestResultState) {
        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("list Log Files Failed. Failed to find the Log Reader for the Job {}.", jobName);
            return new ArrayList<>();
        }
        return logReader.listLogFiles(jobName);
    }

    @Override
    public List<String> tailLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(targetInstanceId, harvestResultNumber, harvestResultState);
        LogReader logReader = getLogReader(targetInstanceId, harvestResultNumber, harvestResultState);
        if (logReader == null) {
            log.warn("Tail Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return Collections.singletonList("");
        }
        return logReader.tail(jobName, aFileName, aNoOfLines);
    }

    private LogReader getLogReader(long targetInstanceId, int harvestResultNumber, int harvestResultState) {
        LogReader logReader = null;
        if (harvestResultState == HarvestResult.STATE_MODIFYING ||
                harvestResultState == HarvestResult.STATE_INDEXING) {
            logReader = digitalAssetStoreFactory.getLogReader();
        } else {
            logReader = harvestAgentManager.getLogReader(getJobName(targetInstanceId, harvestResultNumber, harvestResultState));
            if (logReader == null) {
                log.error("Could not get log reader instance from Harvest Agent");
            }
        }

        return logReader;
    }

    private String getJobName(long targetInstanceId, int harvestResultNumber, int harvestResultState) {
        if (harvestResultState == HarvestResult.STATE_MODIFYING ||
                harvestResultState == HarvestResult.STATE_INDEXING) {
            return String.format("%s@%s", this.type, PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber));
        } else {
            return PatchUtil.getPatchJobName(targetInstanceId, harvestResultNumber);
        }
    }

    public HarvestAgentManager getHarvestAgentManager() {
        return harvestAgentManager;
    }

    public void setHarvestAgentManager(HarvestAgentManager harvestAgentManager) {
        this.harvestAgentManager = harvestAgentManager;
    }

    public DigitalAssetStoreFactory getDigitalAssetStoreFactory() {
        return digitalAssetStoreFactory;
    }

    public void setDigitalAssetStoreFactory(DigitalAssetStoreFactory digitalAssetStoreFactory) {
        this.digitalAssetStoreFactory = digitalAssetStoreFactory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
