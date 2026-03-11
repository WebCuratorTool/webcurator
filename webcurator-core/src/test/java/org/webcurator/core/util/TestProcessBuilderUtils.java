package org.webcurator.core.util;

import org.apache.commons.io.FileUtils;
import org.junit.Assume;
import org.junit.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import java.io.*;
import java.nio.file.Files;

public class TestProcessBuilderUtils {
    @Test
    public void testValidCommand() {
        String command = "ls";
        String path = ProcessBuilderUtils.getFullPathOfCommand(command);
        assert path != null;
        assert path.endsWith(command);
    }

    @Test
    public void testInvalidCommand() {
        String command = "whoru";
        String path = ProcessBuilderUtils.getFullPathOfCommand(command);
        assert path == null;
    }

    @Test
    public void testForceDeleteDirectory() throws IOException {
        // Check if OS is Windows. If true, the test is skipped here.
        String osName = System.getProperty("os.name").toLowerCase();
        Assume.assumeFalse("Skipping test on Windows", osName.startsWith("win"));

        File tmpDir = new File("/tmp/a-collection");
        assert tmpDir.exists() || tmpDir.mkdirs();

        File tmpFile = new File(tmpDir, "x.warc");
        try {
            FileUtils.writeByteArrayToFile(tmpFile, "this is a test case".getBytes(), false);
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
        }

        InputStreamReader input = new InputStreamReader(Files.newInputStream(tmpFile.toPath()));
        int ch = input.read();
        assert ch > 0;

        boolean ret = ProcessBuilderUtils.forceDeleteDirectory(tmpDir);
        assert ret;

        input.close();

        assert !tmpDir.exists();
    }
}
