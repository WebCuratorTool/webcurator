/**
 * nz.govt.natlib.ndha.wctdpsdepositor - Software License
 * <p>
 * Copyright 2007/2009 National Library of New Zealand.
 * All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * or the file "LICENSE.txt" included with the software.
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package nz.govt.natlib.ndha.wctdpsdepositor.dpsdeposit.dspresult;

import com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument;


import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webcurator.core.archive.dps.DpsDepositFacade.DepositResult;


public class DepositResultConverter {
    private static final Logger log = LoggerFactory.getLogger(DepositResultConverter.class);

    public DepositResult unmarshalFrom(String depositResult) {
        try {
            log.debug(depositResult);
            DepositResultDocument depositResultDocument = DepositResultDocument.Factory.parse(depositResult);
            com.exlibris.digitool.deposit.service.xmlbeans.DepositResultDocument.DepositResult xmlDocument = depositResultDocument.getDepositResult();

            return new DepositResultAdapterImpl(xmlDocument);
        } catch (XmlException xe) {
            throw new RuntimeException("An exception occurred while un-marshaling the DPS result from XML.", xe);
        }

    }

}
