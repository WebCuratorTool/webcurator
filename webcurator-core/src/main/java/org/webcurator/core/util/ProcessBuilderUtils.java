package org.webcurator.core.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class ProcessBuilderUtils {
    private static final Logger log = LoggerFactory.getLogger(ProcessBuilderUtils.class);

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

        if (ret.size() > 0 && ret.get(0).endsWith(commandName)) {
            return ret.get(0);
        }
        return null;
    }
}
