/*
 *  Copyright 2006 The National Library of New Zealand
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.webcurator.ui.profiles.validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.webcurator.core.profiles.*;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.ui.common.validation.AbstractBaseValidator;
import org.webcurator.ui.common.validation.ValidatorUtil;
import org.webcurator.ui.profiles.command.Heritrix3ProfileCommand;
import org.webcurator.ui.util.ProfileUtil;

import java.math.BigDecimal;

/**
 * Validate the profile general tab.
 *
 * @author nwaight
 */
public class Heritrix3ProfileValidator extends AbstractBaseValidator {
    private static final Logger log = LoggerFactory.getLogger(Heritrix3ProfileValidator.class);

    public boolean supports(Class clazz) {
        return Heritrix3ProfileCommand.class.equals(clazz);
    }

    public void validate(Object comm, Errors errors) {
        Heritrix3ProfileCommand command = (Heritrix3ProfileCommand) comm;

        // Contact URL is required.
        if (command.getContactURL() != null) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "contactURL", "required", getObjectArrayForLabel("contactURL"), "Contact URL is a required field");
            ValidatorUtil.validateURL(errors, command.getContactURL(), "invalid.url", new Object[]{command.getContactURL()}, "Invalid Contact URL");
        }

        // User agent prefix is required.
        if (command.getUserAgent() != null) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "userAgent", "required", getObjectArrayForLabel("userAgent"), "User Agent Prefix is a required field");
        }

        boolean isValidBlockUrls = ProfileUtil.rejectInvalidURLs(errors, "blockUrls", command.getBlockUrls(), "Block Urls: each star(*) in the url pattern must start with a dot(.)");
        boolean isValidIncludeUrls = ProfileUtil.rejectInvalidURLs(errors, "includeUrls", command.getIncludeUrls(), "Include Urls: each star(*) in the url pattern must start with a dot(.)");

        if ((req != null) && isValidBlockUrls && isValidIncludeUrls) {
            Profile sessionObj = (Profile) req.getSession().getAttribute("profile");
            if (sessionObj.isHeritrix3Profile()) {
                Heritrix3Profile heritrix3Profile = (Heritrix3Profile) req.getSession().getAttribute("heritrixProfile");

                Heritrix3ProfileOptions h3ProfileOptions = heritrix3Profile.getHeritrix3ProfileOptions();
                h3ProfileOptions.setContactURL(command.getContactURL());
                h3ProfileOptions.setDocumentLimit(command.getDocumentLimit());
                h3ProfileOptions.setDataLimit(BigDecimal.valueOf(command.getDataLimit()));
                h3ProfileOptions.setDataLimitUnit(ProfileDataUnit.valueOf(command.getDataLimitUnit()));
                h3ProfileOptions.setTimeLimit(BigDecimal.valueOf(command.getTimeLimit()));
                h3ProfileOptions.setTimeLimitUnit(ProfileTimeUnit.valueOf(command.getTimeLimitUnit()));
                h3ProfileOptions.setMaxPathDepth(command.getMaxPathDepth());
                h3ProfileOptions.setMaxHops(command.getMaxHops());
                h3ProfileOptions.setMaxTransitiveHops(command.getMaxTransitiveHops());
                h3ProfileOptions.setIgnoreRobotsTxt(command.isIgnoreRobotsTxt());
                h3ProfileOptions.setExtractJs(command.isExtractJs());
                h3ProfileOptions.setIgnoreCookies(command.isIgnoreCookies());
                h3ProfileOptions.setDefaultEncoding(command.getDefaultEncoding());
                h3ProfileOptions.setBlockURLs(command.getBlockUrls());
                h3ProfileOptions.setIncludeURLs(command.getIncludeUrls());
                h3ProfileOptions.setMaxFileSize(BigDecimal.valueOf(command.getMaxFileSize()));
                h3ProfileOptions.setMaxFileSizeUnit(ProfileDataUnit.valueOf(command.getMaxFileSizeUnit()));
                h3ProfileOptions.setCompress(command.isCompress());
                h3ProfileOptions.setPrefix(command.getPrefix());
                h3ProfileOptions.setPolitenessOptions(new PolitenessOptions(command.getPoliteness(), command.getDelayFactor(), command.getMinDelayMs(), command.getMaxDelayMs(), command.getRespectCrawlDelayUpToSeconds(), command.getMaxPerHostBandwidthUsageKbSec()));

                heritrix3Profile.setHeritrix3ProfileOptions(h3ProfileOptions);
                ProfileUtil.rejectInvalidProfile(errors, heritrix3Profile.toProfileXml());
            }
        }
    }
}
