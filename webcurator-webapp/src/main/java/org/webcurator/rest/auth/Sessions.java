package org.webcurator.rest.auth;

import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Just a look-up table containing existing session IDs and last-seen timestamps
 */
@Component
public class Sessions {

    private ConcurrentHashMap<String, Date> sessionMap = new ConcurrentHashMap<>();

    public void addSession(String id) {
        if (sessionMap.containsKey(id)) {
            // Can't happen
            throw new RuntimeException(String.format("Session id %s already exists", id));
        }
        sessionMap.put(id, new Date());
    }

    public void removeSession(String id) {
        sessionMap.remove(id);
    }

    public void touchSession(String id, long expireInterval) throws InvalidSessionException {
        if (!sessionMap.containsKey(id) || System.currentTimeMillis() - sessionMap.get(id).getTime() > expireInterval) {
            sessionMap.remove(id);
            throw new InvalidSessionException(id);
        }
        sessionMap.put(id, new Date());
    }

    public class InvalidSessionException extends Exception {
        public InvalidSessionException(String id) {
            super(String.format("Session with %d has expired", id));
        }
    }

}
