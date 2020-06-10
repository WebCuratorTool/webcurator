package org.webcurator.core.harvester.coordinator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.store.DigitalAssetStoreFactory;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;

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
    public File getLogfile(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFilename) {
        if (aFilename == null || aFilename.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Tail Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }

        return logReader.retrieveLogfile(jobName, aFilename);
    }

    @Override
    public List<String> getLogLinesByRegex(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines, String aRegex, boolean prependLineNumbers) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Get log lines by regex failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }

        return logReader.getByRegularExpression(jobName, aFileName, aRegex, "zzzzzzzzz", prependLineNumbers, 0,
                aNoOfLines);
    }

    @Override
    public Integer getFirstLogLineAfterTimeStamp(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, Long timestamp) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Get First Log Line After Timestamp failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineAfterTimeStamp(jobName, aFileName, timestamp);
    }

    @Override
    public Integer getFirstLogLineContaining(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, String match) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Get First Log Line Containing failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineContaining(jobName, aFileName, match);
    }

    @Override
    public Integer getFirstLogLineBeginning(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, String match) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Get First Log Line Beginning failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.findFirstLineBeginning(jobName, aFileName, match);
    }

    @Override
    public List<String> getLog(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aStartLine, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Get Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return null;
        }
        return logReader.get(jobName, aFileName, aStartLine, aNoOfLines);
    }

    @Override
    public Integer countLogLines(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Count Log Lines Failed. Failed to find the log Reader for the Job {}.", jobName);
            return 0;
        }
        return logReader.countLines(jobName, aFileName);
    }

    @Override
    public List<String> headLog(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Head Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return Collections.singletonList("");
        }
        return logReader.get(jobName, aFileName, 1, aNoOfLines);
    }

    @Override
    public List<LogFilePropertiesDTO> listLogFileAttributes(TargetInstance aTargetInstance, HarvestResult aHarvestResult) {
        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("listLogFileAttributes Failed. Failed to find the Log Reader for the Job {}.", jobName);
            return new ArrayList<>();
        }
        return logReader.listLogFileAttributes(jobName);
    }

    @Override
    public List<String> listLogFiles(TargetInstance aTargetInstance, HarvestResult aHarvestResult) {
        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("list Log Files Failed. Failed to find the Log Reader for the Job {}.", jobName);
            return new ArrayList<>();
        }
        return logReader.listLogFiles(jobName);
    }

    @Override
    public List<String> tailLog(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines) {
        if (aFileName == null || aFileName.trim().length() == 0) {
            throw new WCTRuntimeException("File name must not be null");
        }

        String jobName = getJobName(aTargetInstance, aHarvestResult);
        LogReader logReader = getLogReader(aTargetInstance, aHarvestResult);
        if (logReader == null) {
            log.warn("Tail Log Files Failed. Failed to find the log Reader for the Job {}.", jobName);
            return Collections.singletonList("");
        }
        return logReader.tail(jobName, aFileName, aNoOfLines);
    }

    private LogReader getLogReader(TargetInstance ti, HarvestResult hr) {
        if (ti == null) {
            throw new WCTRuntimeException("Target instance must not be null");
        }
        if (hr == null) {
            throw new WCTRuntimeException("Harvest result must not be null");
        }

        if (!ti.getState().equalsIgnoreCase(TargetInstance.STATE_PATCHING)) {
            throw new WCTRuntimeException("Target instance is not patching, state: " + ti.getState());
        }

        LogReader logReader = null;
        if (hr.getState() == HarvestResult.STATE_PATCH_HARVEST_RUNNING || hr.getState() == HarvestResult.STATE_PATCH_HARVEST_PAUSED || hr.getState() == HarvestResult.STATE_PATCH_HARVEST_STOPPED) {
            logReader = harvestAgentManager.getLogReader(getJobName(ti, hr));
            if (logReader == null) {
                log.error("Could not get log reader instance from Harvest Agent");
            }
        } else {
            logReader = digitalAssetStoreFactory.getLogReader();
        }
        return logReader;
    }

    private String getJobName(TargetInstance ti, HarvestResult hr) {
        if (ti == null) {
            throw new WCTRuntimeException("Target instance must not be null");
        }
        if (hr == null) {
            throw new WCTRuntimeException("Harvest result must not be null");
        }

        if (this.type == null || this.type.trim().length() == 0 || this.type.equalsIgnoreCase(HarvestResult.PATCH_STAGE_TYPE_NORMAL)) {
            return String.format("mod_%d_%d", ti.getOid(), hr.getHarvestNumber());
        } else {
            return String.format("%s@mod_%d_%d", this.type, ti.getOid(), hr.getHarvestNumber());
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
