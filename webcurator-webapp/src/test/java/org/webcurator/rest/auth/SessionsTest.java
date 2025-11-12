package org.webcurator.rest.auth;

import org.junit.Assert;
import org.junit.Test;

public class SessionsTest {

    /**
     * Test the implicit clean-up of stale sessions when adding a new one
     */
    @Test
    public void testSessionCleanup() {

        Sessions sessions = new Sessions();
        sessions.addSession("token1", null, "user", "agency", 1);
        try { Thread.sleep(5); } catch (InterruptedException e) { throw new RuntimeException(e); }
        sessions.addSession("token2", null, "user", "agency", 1);

        // The session pointed to by token1 should have been removed by the second call to addSession
        Assert.assertFalse(sessions.exists("token1"));

    }

    /**
     * Test whether sessions correctly expire
     */
    @Test
    public void testSessionInvalidation() {
        Sessions sessions = new Sessions();
        sessions.addSession("token1", null, "user", "agency", 1);
        try { Thread.sleep(5); } catch (InterruptedException e) { throw new RuntimeException(e); }
        try {
            sessions.getPrivileges("token1");
        } catch (InvalidSessionException e) {
            return;
        }
        Assert.fail("Expected exception " + InvalidSessionException.class.getName());
    }
}