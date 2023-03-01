package org.webcurator.rest.auth;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utils for creation of tokens
 */
public class TokenUtils {

    // These instances are thread-safe
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    // Create 64-char random token
    public static String createToken() {
        byte[] randomBytes = new byte[48];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }
}
