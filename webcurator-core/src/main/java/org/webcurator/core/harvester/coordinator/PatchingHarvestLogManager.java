package org.webcurator.core.harvester.coordinator;

import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;

import java.io.File;
import java.util.List;

public interface PatchingHarvestLogManager {
    File getLogfile(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFilename);

    List<String> getLogLinesByRegex(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines, String aRegex, boolean prependLineNumbers);

    Integer getFirstLogLineAfterTimeStamp(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, Long timestamp);

    Integer getFirstLogLineContaining(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, String match);

    Integer getFirstLogLineBeginning(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, String match);

    List<String> getLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aStartLine, int aNoOfLines);

    Integer countLogLines(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName);

    List<String> headLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines);

    List<LogFilePropertiesDTO> listLogFileAttributes(long targetInstanceId, int harvestResultNumber, int harvestResultState);

    List<String> listLogFiles(long targetInstanceId, int harvestResultNumber, int harvestResultState);

    List<String> tailLog(long targetInstanceId, int harvestResultNumber, int harvestResultState, String aFileName, int aNoOfLines);
}
