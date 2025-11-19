package org.webcurator.rest.auth;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basically a look-up table, containing a map of session IDs to session info structs
 */
@Component
public class Sessions {

    private ConcurrentHashMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    /**
     * Add session and remove any expired sessions while we're at it
     */
    public void addSession(String id, String user, long expireInterval) {
        if (sessionMap.containsKey(id)) {
            // Can't happen
            throw new RuntimeException(String.format("Session id %s already exists", id));
        }
        for (String key : sessionMap.keySet()) {
            if (sessionMap.get(key).expired()) {
                removeSession(key);
            }
        }
        sessionMap.put(id, new SessionInfo(user, expireInterval));
    }

    public boolean exists(String id) {
        return sessionMap.containsKey(id);
    }

    public void removeSession(String id) {
        sessionMap.remove(id);
    }


    public String getUser(String id) {
        return sessionMap.get(id).user;
    }


    // Struct used as the value in the kv pair representing a session
    class SessionInfo {
        Date modified;
        long expireInterval;
        String user;

        public SessionInfo(String user, long expireInterval) {
            modified = new Date();
            this.user = user;
            this.expireInterval = expireInterval;
        }

        boolean expired() {
            return System.currentTimeMillis() - modified.getTime() > expireInterval;
        }

        void touch() {
            modified = new Date();
        }
    }

}
