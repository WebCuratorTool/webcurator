package org.webcurator.core.harvester.coordinator;

import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.LogFilePropertiesDTO;
import org.webcurator.domain.model.core.TargetInstance;

import java.io.File;
import java.util.List;

public interface PatchingHarvestLogManager {
    String TYPE_NORMAL = "normal";
    String TYPE_MODIFYING = "modifying";
    String TYPE_INDEXING = "indexing";

    File getLogfile(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFilename);

    List<String> getLogLinesByRegex(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines, String aRegex, boolean prependLineNumbers);

    Integer getFirstLogLineAfterTimeStamp(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, Long timestamp);

    Integer getFirstLogLineContaining(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, String match);

    Integer getFirstLogLineBeginning(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, String match);

    List<String> getLog(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aStartLine, int aNoOfLines);

    Integer countLogLines(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName);

    List<String> headLog(TargetInstance aTargetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines);

    List<LogFilePropertiesDTO> listLogFileAttributes(TargetInstance aTargetInstance, HarvestResult aHarvestResult);

    List<String> listLogFiles(TargetInstance aTargetInstance, HarvestResult aHarvestResult);

    List<String> tailLog(TargetInstance targetInstance, HarvestResult aHarvestResult, String aFileName, int aNoOfLines);
}
