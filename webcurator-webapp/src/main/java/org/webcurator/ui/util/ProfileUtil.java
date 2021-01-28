package org.webcurator.ui.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.archive.spring.PathSharingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProfileUtil {
    private static final Logger log = LoggerFactory.getLogger(ProfileUtil.class);

    public static boolean rejectInvalidURLs(Errors errors, String fieldName, List<String> urls, String errMsg) {
        if (urls == null) {
            errors.rejectValue(fieldName, "Error", errMsg);
            return false;
        }
        for (String item : urls) {
            if (item == null || StringUtils.isEmpty(item)) {
                continue;
            }

            if (StringUtils.trimToEmpty(item).length() <= 4 || !item.startsWith(".*") || !item.endsWith(".*")) {
                errors.rejectValue(fieldName, "Error", errMsg);
                return false;
            }
        }
        return true;
    }

    public static boolean rejectInvalidURLs(Errors errors, String fieldName, String urls, String errMsg) {
        List<String> list = null;
        if (urls != null) {
            list = Arrays.asList(urls.split(System.lineSeparator())).stream().map(StringUtils::trimToEmpty).collect(Collectors.toList());
        }
        return rejectInvalidURLs(errors, fieldName, list, errMsg);
    }

    public static boolean rejectInvalidProfile(Errors errors, String profileXml) {
        Path fXml = null;
        PathSharingContext ac = null;
        try {
            fXml = Files.createTempFile("profile", ".xml");
            IOUtils.write(profileXml.getBytes(), Files.newOutputStream(fXml));
            ac = new PathSharingContext(new String[]{fXml.toUri().toString()}, false, null);
            ac.refresh();
            ac.validate();
        } catch (Exception e) {
            log.error("Invalid profile, {}", profileXml, e);
            errors.reject("Profile", e.getMessage());

            if (ac != null && ac.getAllErrors() != null && ac.getAllErrors().size() > 0) {
                ac.getAllErrors().forEach((key, errorList) -> {
                    errorList.getAllErrors().stream().map(ObjectError::toString).forEach(errors::reject);
                });
            }
            return false;
        } finally {
            if (ac != null) {
                ac.clearResourceCaches();
            }

            if (fXml != null && fXml.toFile().exists()) {
                fXml.toFile().delete();
            }
        }
        return true;
    }
}
