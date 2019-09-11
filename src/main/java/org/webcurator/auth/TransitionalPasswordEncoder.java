package org.webcurator.auth;

/**
 * Copyright (c) 2019, Koninklijke Bibliotheek - Nationale bibliotheek van Nederland
 * <p>
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * * Neither the name of the Koninklijke Bibliotheek nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class that allows us to support both bcrypt and legacy salted SHA-1 passwords,
 * to allow our users to smoothly transtion to bcrypt
 *
 * @author Hanna Koppelaar
 *
 * $
 */
public class TransitionalPasswordEncoder implements PasswordEncoder {

    private static final PasswordEncoder BCRYPT_ENCODER = new BCryptPasswordEncoder();

    // The old salt that used to be in wct-core-security.xml (which can reasonably be considered to be fixed)
    private static final String SALT = "Rand0mS4lt";

    private static Log log = LogFactory.getLog(TransitionalPasswordEncoder.class);

    @Override
    public String encode(CharSequence rawPassword) {
        return BCRYPT_ENCODER.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword.startsWith("$")) {
            return BCRYPT_ENCODER.matches(rawPassword, encodedPassword);
        }
        log.warn("Found legacy SHA-1 password hash. Please advise your users to change their WCT password");
        return sha1Match(rawPassword, encodedPassword);
    }

    private boolean sha1Match(CharSequence rawPassword, String encodedPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            String saltedInput = rawPassword + "{" + SALT + "}";
            byte[] hashedPassword = md.digest(saltedInput.toString().getBytes(StandardCharsets.UTF_8));
            String hexHashedPassword = toHex(hashedPassword);
            return encodedPassword.equalsIgnoreCase(hexHashedPassword);
        } catch (NoSuchAlgorithmException e) {
            // "Can't" happen
            throw new RuntimeException(e);
        }
    }

    private String toHex(byte[] input) {
        char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[input.length * 2];
        for (int j = 0; j < input.length; j++) {
            int v = input[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

}
