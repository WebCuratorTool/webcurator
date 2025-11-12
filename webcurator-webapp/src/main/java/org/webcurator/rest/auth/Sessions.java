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
    public void addSession(String id, HashMap<String, Integer> privileges, String user, String agency, long expireInterval) {
        if (sessionMap.containsKey(id)) {
            // Can't happen
            throw new RuntimeException(String.format("Session id %s already exists", id));
        }
        for (String key : sessionMap.keySet()) {
            if (sessionMap.get(key).expired()) {
                removeSession(key);
            }
        }
        sessionMap.put(id, new SessionInfo(privileges, user, agency, expireInterval));
    }

    public boolean exists(String id) {
        return sessionMap.containsKey(id);
    }

    public void removeSession(String id) {
        sessionMap.remove(id);
    }


    /**
     * Get the privileges for this session if it's still valid and, if so, update the timestamp of last access
     */
    public HashMap<String, Integer> getPrivileges(String id) throws InvalidSessionException {
        if (!sessionMap.containsKey(id) || sessionMap.get(id).expired()) {
            sessionMap.remove(id);
            throw new InvalidSessionException(id);
        }
        sessionMap.get(id).touch();
        return sessionMap.get(id).privileges;
    }

    public String getAgency(String id) {
        return sessionMap.get(id).agency;
    }

    public String getUser(String id) {
        return sessionMap.get(id).user;
    }


    // Struct used as the value in the kv pair representing a session
    class SessionInfo {
        Date modified;
        HashMap<String, Integer> privileges;
        long expireInterval;
        String user;
        String agency;

        public SessionInfo(HashMap<String, Integer> privileges, String user, String agency, long expireInterval) {
            modified = new Date();
            this.privileges = privileges;
            this.user = user;
            this.agency = agency;
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
