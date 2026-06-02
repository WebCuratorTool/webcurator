package org.webcurator.core.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessBuilderUtils {
    private static final Logger log = LoggerFactory.getLogger(ProcessBuilderUtils.class);
    private static final int UNKNOWN_ERROR = -9999;

    public static String getFullPathOfCommand(String commandName) {
        final List<String> ret = new ArrayList<>();
        String command = "which " + commandName;
        String[] commandList = command.split(" ");
        try {
            Process process = Runtime.getRuntime().exec(commandList);
            int processStatus = process.waitFor();

            if (processStatus != 0) {
                log.error("Unable to process the command in a new thread, processStatus={}.", processStatus);
            }
            List<String> results = IOUtils.readLines(process.getInputStream(), Charset.defaultCharset());
            ret.addAll(results);
        } catch (Exception e) {
            log.error("Unable to process the command in a new thread.", e);
        }

        if (!ret.isEmpty() && ret.get(0).endsWith(commandName)) {
            return ret.get(0);
        }
        return null;
    }

    public static int processCommand(File targetDirectory, String[] commands) {
        List<String> commandList = Arrays.asList(commands);
        ProcessBuilder processBuilder = new ProcessBuilder(commandList);
        processBuilder.directory(targetDirectory);
        try {
            Process process = processBuilder.inheritIO().start();
            int processStatus = process.waitFor();

            if (processStatus != 0) {
                log.error("Process ended with a failed status: {}, commands: {}", processStatus, String.join(",", commands));
            }
            return processStatus;
        } catch (Exception e) {
            log.error("Unable to process the command in a new thread.", e);
            return UNKNOWN_ERROR;
        }
    }

    public static int wbManagerInitCollection(File targetDirectory, String collName) {
        String wb_manager = ProcessBuilderUtils.getFullPathOfCommand("wb-manager");
        if (wb_manager == null) {
            log.error("The command tool [wb-manager] is not installed or not in the env PATH.");
            return UNKNOWN_ERROR;
        }
        String[] commands = {wb_manager, "init", collName};
        return processCommand(targetDirectory, commands);
    }

    public static int wbManagerAddWarcFile(File targetDirectory, String collName, String warcFilePath) {
        String wb_manager = ProcessBuilderUtils.getFullPathOfCommand("wb-manager");
        if (wb_manager == null) {
            log.error("The command tool [wb-manager] is not installed or not in the env PATH.");
            return UNKNOWN_ERROR;
        }
        String[] commands = {wb_manager, "add", collName, warcFilePath};
        return processCommand(targetDirectory, commands);
    }

    public static int wbManagerReindexCollection(File targetDirectory, String collName) {
        String wb_manager = ProcessBuilderUtils.getFullPathOfCommand("wb-manager");
        if (wb_manager == null) {
            log.error("The command tool [wb-manager] is not installed or not in the env PATH.");
            return UNKNOWN_ERROR;
        }
        String[] commands = {wb_manager, "reindex", collName};
        return processCommand(targetDirectory, commands);
    }

    public static boolean forceDeleteDirectory(File toBeDeletedDirectory) {
        if (toBeDeletedDirectory == null || !toBeDeletedDirectory.exists()) {
            return true;
        }
        String os = SystemUtils.OS_NAME.toLowerCase();
        if (SystemUtils.IS_OS_UNIX || os.contains("bsd")) {
//        if (os.contains("linux") || os.contains("unix")) {
            String cmdRm = getFullPathOfCommand("rm");
            String[] commands = {cmdRm, "-rf", toBeDeletedDirectory.getAbsolutePath()};
            int ret = processCommand(new File("/tmp"), commands);
            return ret == 0;
        } else {
            //TODO: not supported
            return false;
        }
    }

    public static boolean createSymLink(File baseDirectory, File targetDirectory, String symLinkName) {
        if (targetDirectory == null || !targetDirectory.exists()) {
            return false;
        }
        String os = SystemUtils.OS_NAME.toLowerCase();
        if (SystemUtils.IS_OS_UNIX || os.contains("bsd")) {
            String cmdRm = getFullPathOfCommand("ln");
            String[] commands = {cmdRm, "-s", targetDirectory.getAbsolutePath(), symLinkName};
            int ret = processCommand(baseDirectory, commands);
            return ret == 0;
        } else {
            //TODO: not supported
            return false;
        }
    }
}