package org.webcurator.core.visualization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockVisualizationDirectoryManager {
    private static final String baseDir = "/usr/local/wct/store";
    private static final String baseLogDir = "logs";
    private static final String baseReportDir = "reports";

    public static VisualizationDirectoryManager getDirectoryManagerInstance() {
        return new VisualizationDirectoryManager(baseDir, baseLogDir, baseReportDir);
    }
}
