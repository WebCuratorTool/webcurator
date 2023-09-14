package org.webcurator.rest.common;

import java.util.HashMap;

public class Utils {

    /**
     * Generate a map with an error message, to be used everywhere in the API
     */
    public static HashMap<String, Object> errorMessage(Object msg) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("Error", msg);
        return map;
    }
}
