package org.webcurator.core.screenshot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.webcurator.core.util.ProcessBuilderUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ProcessBuilderUtilsTest {
    private static final Log log = LogFactory.getLog(ProcessBuilderUtilsTest.class);

    @Test
    public void testWbManagerInit() {
        String collName = "x-899333";
        File pywbManagerStoreDir = new File("/usr/local/wct/pywb");
        String wb_manager = ProcessBuilderUtils.getFullPathOfCommand("wb-manager");
        String[] commands = {wb_manager, "init", collName};



        List<String> commandList = Arrays.asList(commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(pywbManagerStoreDir);
        try {
            Process process = processBuilder.inheritIO().start();
            int processStatus = process.waitFor();

            if (processStatus != 0) {
                throw new Exception("Process ended with a failed status: " + processStatus);
            }
        } catch (Exception e) {
            log.error("Unable to process the command in a new thread.", e);
        }
    }
}
