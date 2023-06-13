package org.webcurator.rest.auth;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
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
    public void addSession(String id, List<String> roles, long expireInterval) {
        if (sessionMap.containsKey(id)) {
            // Can't happen
            throw new RuntimeException(String.format("Session id %s already exists", id));
        }
        for (String key : sessionMap.keySet()) {
            if (sessionMap.get(key).expired()) {
                removeSession(id);
            }
        }
        sessionMap.put(id, new SessionInfo(roles, expireInterval));
    }

    public void removeSession(String id) {
        sessionMap.remove(id);
    }

    /**
     * Get the roles for this session if it's still valid and, if so, update the timestamp of last access
     */
    public List<String> getRoles(String id) throws InvalidSessionException {
        if (!sessionMap.containsKey(id) || sessionMap.get(id).expired()) {
            sessionMap.remove(id);
            throw new InvalidSessionException(id);
        }
        sessionMap.get(id).touch();
        return sessionMap.get(id).roles;
    }

    public class InvalidSessionException extends Exception {
        public InvalidSessionException(String id) {
            super(String.format("Session with %s has expired", id));
        }
    }


    // Struct used as the value in the kv pair representing a session
    class SessionInfo {
        Date modified;
        List<String> roles;
        long expireInterval;

        public SessionInfo(List<String> roles, long expireInterval) {
            modified = new Date();
            this.roles = roles;
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
