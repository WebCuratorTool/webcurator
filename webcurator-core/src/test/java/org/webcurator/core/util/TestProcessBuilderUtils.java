package org.webcurator.core.util;

import org.junit.Test;

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
}
