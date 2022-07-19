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

package nz.govt.natlib.ndha.wctdpsdepositor.dpsdeposit;

//import com.exlibris.core.infra.svc.api.locator.WebServiceLocator;
import com.exlibris.dps.DepositWebServices;
import com.exlibris.dps.DepositWebServices_Service;
import nz.govt.natlib.ndha.wctdpsdepositor.WctDepositParameter;

import javax.xml.namespace.QName;
import java.net.URL;


public class DepositWebServicesFactoryImpl implements DepositWebServicesFactory {

    public DepositWebServices createInstance(WctDepositParameter depositParameter) {
        try {
//            return WebServiceLocator.getInstance().lookUp(DepositWebServices.class, depositParameter.getDpsWsdlUrl());
            DepositWebServices_Service depWS = new DepositWebServices_Service(new URL(depositParameter.getDpsWsdlUrl()), new QName("http://dps.exlibris.com/", "DepositWebServices"));
            return depWS.getDepositWebServicesPort();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
