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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.webcurator.common.ui.CommandConstants;
import org.webcurator.ui.common.validation.AbstractBaseValidator;
import org.webcurator.ui.common.validation.ValidatorUtil;
import org.webcurator.ui.target.command.TargetInstanceCommand;

/**
 * Validate a request to harvest a target instance now.
 * @author nwaight
 */
@Component
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
public class HarvestNowValidator extends AbstractBaseValidator {

    /** Logger to use with this class. */
    private final Log log;

    /** default constructor. */
    public HarvestNowValidator() {
        super();
        log = LogFactory.getLog(HarvestNowValidator.class);
    }

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#supports(java.lang.Class)
     */
    public boolean supports(Class aClass) {
        return aClass.equals(TargetInstanceCommand.class);
    }

    /* (non-Javadoc)
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    public void validate(Object aCommand, Errors aErrors) {
        TargetInstanceCommand cmd = (TargetInstanceCommand) aCommand;
        if (log.isDebugEnabled()) {
            log.debug("Validating harvest now target instance command.");
        }

        ValidationUtils.rejectIfEmptyOrWhitespace(aErrors, TargetInstanceCommand.PARAM_CMD, "required", getObjectArrayForLabel(TargetInstanceCommand.PARAM_CMD), "Action command is a required field.");

        if (TargetInstanceCommand.ACTION_HARVEST.equals(cmd.getCmd())) {
            ValidationUtils.rejectIfEmptyOrWhitespace(aErrors, TargetInstanceCommand.PARAM_AGENT, "required", getObjectArrayForLabel(TargetInstanceCommand.PARAM_AGENT), "Harvest agent is a required field.");
            ValidationUtils.rejectIfEmptyOrWhitespace(aErrors, CommandConstants.TARGET_INSTANCE_COMMAND_PARAM_OID, "required", getObjectArrayForLabel(CommandConstants.TARGET_INSTANCE_COMMAND_PARAM_OID), "Target Instance Id is a required field.");

            if (!aErrors.hasErrors()) {
            	ValidatorUtil.validateTargetApproved(aErrors, cmd.getTargetInstanceId(), "target.not.approved");
            }
        }
    }
}
