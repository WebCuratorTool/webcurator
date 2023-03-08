package org.webcurator.ui.util;

//import org.apache.commons.io.IOUtils;

import org.apache.commons.lang.StringUtils;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;


public class ProfileUtil {

    /**
     * Validate the URL regexes supplied
     */
    public static boolean rejectInvalidRegexes(Errors errors, String fieldName, List<String> urlRegexes) {
        if (urlRegexes == null) {
            errors.rejectValue(fieldName, "Error", "List of URL regexes unexpectedly null");
            return false;
        }
        for (String item : urlRegexes) {
            if (item == null || StringUtils.isEmpty(item)) {
                continue;
            }
            try {
                Pattern.compile(item);
            } catch (PatternSyntaxException e) {

                errors.rejectValue(fieldName, "Error", "Bad URL regex: " + e.getMessage());
                return false;
            }
        }
        return true;
    }

    public static boolean rejectInvalidRegexes(Errors errors, String fieldName, String urlRegexes) {
        List<String> list = null;
        if (urlRegexes != null) {
            list = Arrays.asList(urlRegexes.split(System.lineSeparator())).stream().map(StringUtils::trimToEmpty).collect(Collectors.toList());
        }
        return rejectInvalidRegexes(errors, fieldName, list);
    }

    @Deprecated
    public static boolean rejectInvalidProfile(Errors errors, String profileXml) {
        return true;
    }
}
