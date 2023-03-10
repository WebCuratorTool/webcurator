package org.webcurator.ui.util;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.List;

public class TestProfileUtil {
    @Test
    public void testRejectInvalidURLs() {
        Errors errors = Mockito.mock(Errors.class);
        {
            String[] urlsInvalid = {
                    "*abc.nz*",
                    "*abc.nz",
                    "abc.nz*",
                    "http://rnz.*org*nz",
            };

            for (String item : urlsInvalid) {
                List<String> list = new ArrayList<>();
                list.add(item);
                boolean isValid = ProfileUtil.rejectInvalidRegexes(errors, "testField", list, "invalid");
                assert !isValid;
            }
        }

        {
            String[] urlsValid = {
                    ".*abc.nz.*",
                    ".*abc.nz",
                    "http://abc.nz.*",
                    "http://rnz.*org..*nz",
                    "https:\\/\\/www\\.rnz\\.org\\/img\\/8934502245\\.jpg",
            };
            for (String item : urlsValid) {
                List<String> list = new ArrayList<>();
                list.add(item);
                boolean isValid = ProfileUtil.rejectInvalidRegexes(errors, "testField", list, "invalid");
                assert isValid;
            }
        }
    }
}
