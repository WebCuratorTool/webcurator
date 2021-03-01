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
package org.webcurator.ui.target.validator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.profiles.*;
import org.webcurator.domain.model.core.AbstractTarget;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.ui.common.validation.AbstractBaseValidator;
import org.webcurator.ui.common.validation.ValidatorUtil;
import org.webcurator.ui.target.command.ProfileCommand;
import org.webcurator.ui.util.HarvestAgentUtil;
import org.webcurator.ui.util.ProfileUtil;

import java.math.BigDecimal;

/**
 * Validate the profile overrides tab.
 *
 * @author nwaight
 */
@Component
public class ProfilesOverridesValidator extends AbstractBaseValidator implements ApplicationContextAware {
    private Logger log = LoggerFactory.getLogger(ProfilesOverridesValidator.class);

    ApplicationContext applicationContext;

    public boolean supports(Class clazz) {
        return ProfileCommand.class.equals(clazz);
    }

    public void validate(Object comm, Errors errors) {
        ProfileCommand command = (ProfileCommand) comm;

        // If we are overriding an imported H3RawProfile, then we only validate it and nothing else
        // We validate the profile whether or not it's being overridden
        if (command.isImported()) {
            String h3RawProfile = command.getH3RawProfile();
            if (command.isOverrideH3RawProfile()) {
                HarvestAgent harvestAgent = HarvestAgentUtil.getHarvestAgent(getApplicationContext());
                if (!harvestAgent.isValidProfile(h3RawProfile)) {
                    log.info("isImported, validating overridden imported profile: validation failed.");
                    Object[] vals = new Object[]{"'unnamed overridden imported profile'"};
                    errors.reject("profile.invalid", vals, "The profile is invalid.");
                } else {
                    log.info("isImported, validating overridden imported profile: validation succeeded.");
                }
            } else {
                log.info("Skipping imported profile validation as there is no profile override.");
            }
        }
        // Only validate Heritrix 1 fields if a Heritrix 1 profile is selected.
        // (prevents hidden H1 fields failing validation when using an H3 profile)
        else if ("HERITRIX1".equals(command.getHarvesterType())) {
            log.info("Validating Heritrix 1.x profile: " + command.getProfileOid());

            if (command.isOverrideExcludedMimeTypes()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "excludedMimeTypes", "required", getObjectArrayForLabel("excludedMimeTypes"), "excludedMimeTypes is a required field");
            }

            if (command.isOverrideExcludeFilters()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "excludeFilters", "required", getObjectArrayForLabel("excludeFilters"), "excludeFilters is a required field");
            }

            if (command.isOverrideForceAcceptFilters()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "forceAcceptFilters", "required", getObjectArrayForLabel("forceAcceptFilters"), "forceAcceptFilters is a required field");
            }

            if (command.isOverrideMaxBytesDownload()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "maxBytesDownload", "required", getObjectArrayForLabel("maxBytesDownload"), "maxBytesDownload is a required field");
            }

            if (command.isOverrideMaxDocuments()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "maxDocuments", "required", getObjectArrayForLabel("maxDocuments"), "maxDocuments is a required field");
            }

            if (command.isOverrideMaxHops()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "maxHops", "required", getObjectArrayForLabel("maxHops"), "maxHops is a required field");
            }

            if (command.isOverrideMaxHours()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "maxHours", "required", getObjectArrayForLabel("maxHours"), "maxHours is a required field");
            }

            if (command.isOverrideMaxPathDepth()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "maxPathDepth", "required", getObjectArrayForLabel("maxPathDepth"), "maxPathDepth is a required field");
            }

            if (command.isOverrideRobots()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "robots", "required", getObjectArrayForLabel("robots"), "robots is a required field");
            }
        } else if ("HERITRIX3".equals(command.getHarvesterType())) {
            log.info("Validating non-imported Heritrix 3.x profile: " + command.getProfileOid());

            if (command.isOverrideH3DataLimit()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "h3DataLimit", "required", getObjectArrayForLabel("h3DataLimit"), "h3DataLimit is a required field");
            }
            if (command.isOverrideH3TimeLimit()) {
                ValidationUtils.rejectIfEmptyOrWhitespace(errors, "h3TimeLimit", "required", getObjectArrayForLabel("h3TimeLimit"), "h3TimeLimit is a required field");
            }

            boolean isValidBlockUrls = true;
            if (command.isOverrideH3BlockedUrls()) {
                isValidBlockUrls = ProfileUtil.rejectInvalidURLs(errors, "h3BlockedUrls", command.getH3BlockedUrls(), "Block Urls: the * at the start and end of the url must be transferred to .*");
            }
            boolean isValidIncludeUrls = true;
            if (command.isOverrideH3IncludedUrls()) {
                isValidIncludeUrls = ProfileUtil.rejectInvalidURLs(errors, "h3IncludedUrls", command.getH3IncludedUrls(), "Include Urls: the * at the start and end of the url must be transferred to .*");
            }
            if (!isValidBlockUrls || !isValidIncludeUrls) {
                log.debug("Invalid urls");
                return;
            }

            if (applicationContext == null) {
                log.error("applicationContext is not initialed");
                return;
            }

            ProfileManager profileManager = applicationContext.getBean(ProfileManager.class);
            if (profileManager == null) {
                log.error("Could not find ProfileManager instance");
                return;
            }

            Profile profile = profileManager.load(command.getProfileOid());
            if (profile == null || StringUtils.isEmpty(StringUtils.trimToEmpty(profile.getProfile()))) {
                log.error("Could not find the profile: {}", command.getProfileOid());
                return;
            }

            Heritrix3Profile heritrix3Profile = new Heritrix3Profile(profile.getProfile());
            Heritrix3ProfileOptions h3ProfileOptions = heritrix3Profile.getHeritrix3ProfileOptions();

            if (command.isOverrideH3DocumentLimit()) {
                h3ProfileOptions.setDocumentLimit(command.getH3DocumentLimit());
            }
            if (command.isOverrideH3DataLimit()) {
                h3ProfileOptions.setDataLimit(BigDecimal.valueOf(command.getH3DataLimit()));
                h3ProfileOptions.setDataLimitUnit(ProfileDataUnit.valueOf(command.getH3DataLimitUnit()));
            }
            if (command.isOverrideH3TimeLimit()) {
                h3ProfileOptions.setTimeLimit(BigDecimal.valueOf(command.getH3TimeLimit()));
                h3ProfileOptions.setTimeLimitUnit(ProfileTimeUnit.valueOf(command.getH3TimeLimitUnit()));
            }
            if (command.isOverrideH3MaxPathDepth()) {
                h3ProfileOptions.setMaxPathDepth(command.getH3MaxPathDepth());
            }
            if (command.isOverrideH3MaxHops()) {
                h3ProfileOptions.setMaxHops(command.getH3MaxHops());
            }
            if (command.isOverrideH3MaxTransitiveHops()) {
                h3ProfileOptions.setMaxTransitiveHops(command.getH3MaxTransitiveHops());
            }
            if (command.isOverrideH3IgnoreRobots()) {
                h3ProfileOptions.setIgnoreRobotsTxt(command.isH3IgnoreRobots());
            }
            if (command.isOverrideH3IgnoreCookies()) {
                h3ProfileOptions.setIgnoreRobotsTxt(command.isH3IgnoreCookies());
            }
            if (command.isOverrideH3ExtractJs()) {
                h3ProfileOptions.setIgnoreRobotsTxt(command.isH3ExtractJs());
            }
            if (command.isOverrideH3BlockedUrls()) {
                h3ProfileOptions.setBlockURLs(command.getH3BlockedUrls());
            }
            if (command.isOverrideH3IncludedUrls()) {
                h3ProfileOptions.setIncludeURLs(command.getH3IncludedUrls());
            }

            heritrix3Profile.setHeritrix3ProfileOptions(h3ProfileOptions);

            ProfileUtil.rejectInvalidProfile(errors, heritrix3Profile.toProfileXml());
        }


        // All profiles have profile notes
        ValidatorUtil.validateStringMaxLength(errors, command.getProfileNote(), AbstractTarget.MAX_PROFILE_NOTE_LENGTH, "string.maxlength", getObjectArrayForLabelAndInt(ProfileCommand.PARAM_PROFILE_NOTE, AbstractTarget.MAX_PROFILE_NOTE_LENGTH), "Evaluation Note is too long");
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
