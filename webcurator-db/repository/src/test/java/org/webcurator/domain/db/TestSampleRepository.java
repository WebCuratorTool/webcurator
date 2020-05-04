package org.webcurator.domain.db;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestSampleRepository {
    @Test
    public void simpleTest() {
        assertTrue("Sample test description", "this".length() == 4);
    }
}
