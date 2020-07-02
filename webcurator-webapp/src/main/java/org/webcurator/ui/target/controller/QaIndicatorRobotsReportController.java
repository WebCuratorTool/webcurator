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
package org.webcurator.ui.target.controller;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.webcurator.auth.AuthorityManager;
import org.webcurator.core.agency.AgencyUserManager;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.core.store.DigitalAssetStore;
import org.webcurator.core.util.AuthUtil;
import org.webcurator.core.visualization.networkmap.metadata.NetworkMapResult;
import org.webcurator.core.visualization.networkmap.service.NetworkMapClient;
import org.webcurator.domain.IndicatorDAO;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.Privilege;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.HarvestResult;
import org.webcurator.domain.model.core.Indicator;
import org.webcurator.domain.model.core.IndicatorCriteria;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.ui.admin.command.QaIndicatorCommand;
import org.webcurator.ui.target.command.TargetInstanceCommand;

/**
 * Manages the QA Indicator Administration view and the actions associated with
 * a IndicatorCriteria
 *
 * @author twoods
 */
@Controller
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Lazy(false)
@RequestMapping(path = "/curator/target/qa-indicator-robots-report.html")
public class QaIndicatorRobotsReportController {
    /**
     * the logger.
     */
    private Log log = null;
    /**
     * The manager to use to access the target instance.
     */
    @Autowired
    private TargetInstanceManager targetInstanceManager;
    /**
     * The Data access object for indicators.
     */
    @Autowired
    private IndicatorDAO indicatorDAO;
    /**
     * the agency user manager.
     */
    @Autowired
    private AgencyUserManager agencyUserManager;
    /**
     * the authority manager.
     */
    @Autowired
    private AuthorityManager authorityManager;

    private Map<String, String> excludedIndicators = null;
    /**
     * interface for retrieving data for excluded indicators
     **/
    @Autowired
    private DigitalAssetStore digitalAssetStore;

    @Autowired
    private NetworkMapClient networkMapClient;

    /**
     * message displayed if the robots.txt file is not found
     **/
    private String fileNotFoundMessage = "robots.txt file not found";

    /**
     * Default Constructor.
     */
    public QaIndicatorRobotsReportController() {
        log = LogFactory.getLog(QaIndicatorRobotsReportController.class);
    }

    @InitBinder
    protected void initBinder(HttpServletRequest request,
                              ServletRequestDataBinder binder) {
        // enable null values for long and float fields
        NumberFormat nf = NumberFormat.getInstance(request.getLocale());
        NumberFormat floatFormat = new DecimalFormat("##############.##");
        binder.registerCustomEditor(java.lang.Long.class,
                new CustomNumberEditor(java.lang.Long.class, nf, true));
        binder.registerCustomEditor(java.lang.Float.class,
                new CustomNumberEditor(java.lang.Float.class, nf, true));
    }

    private final void ShowRobotsDotTxtFile(ModelAndView mav, Indicator indicator, TargetInstance ti) throws IOException {
        List<HarvestResult> results = ti.getHarvestResults();
        // get the latest HarvestResult for the ti (may have applied auto-prune)
        HarvestResult hr = results.get(results.size() - 1);

        //Get the "robots.txt" resource urls
        NetworkMapResult networkMapResult=networkMapClient.searchUrlNames(ti.getOid(), hr.getHarvestNumber(), "robots.txt");
        if (networkMapResult.getRspCode()!=NetworkMapResult.RSP_CODE_SUCCESS){
            log.warn(networkMapResult.getRspMsg());
            return;
        }
        List<String> robotUrls = networkMapClient.getArrayListOfNetworkMapNode((String)networkMapResult.getPayload());;
        List<String> lines = new ArrayList<String>();
        robotUrls.forEach(resourceUrl -> {
            try {
                Path path = digitalAssetStore.getResource(ti.getOid(), hr.getHarvestNumber(), resourceUrl);
                // read the file for reporting
                Files.readAllLines(path).stream().filter(line -> {
                    return line != null && line.trim().length() > 0;
                }).forEach(lines::add);
            } catch (DigitalAssetStoreException | IOException e) {
                e.printStackTrace();
            }
        });

        if (lines.size() == 0) {
            lines.add(fileNotFoundMessage);
        }
        // add the lines to the ModelAndView
        mav.addObject("lines", lines);
    }

    @GetMapping
    protected ModelAndView showForm(HttpServletRequest request)
            throws Exception {

        ModelAndView mav = new ModelAndView();

        // fetch the indicator oid from the request (hyper-linked from QA Summary Page)
        String iOid = request.getParameter("indicatorOid");

        if (iOid != null) {

            // prepare the indicator oid
            Long indicatorOid = Long.parseLong(iOid);

            // get the indicator
            Indicator indicator = indicatorDAO.getIndicatorByOid(indicatorOid);

            // add it to the ModelAndView so that we can access it within the jsp
            mav.addObject("indicator", indicator);

            // add the target instance
            TargetInstance instance = targetInstanceManager.getTargetInstance(indicator.getTargetInstanceOid());
            mav.addObject(TargetInstanceCommand.MDL_INSTANCE, instance);

            ShowRobotsDotTxtFile(mav, indicator, instance);

            // ensure that the user belongs to the agency that created the indicator
            if (agencyUserManager.getAgenciesForLoggedInUser().contains(indicator.getAgency())) {
                // otherwise redirect to the configured view
                mav.setViewName("QaIndicatorRobotsReport");
            }

        }
        return mav;

    }

    @PostMapping
    protected ModelAndView processFormSubmission(HttpServletRequest request) throws Exception {
        return showForm(request);
    }

    /**
     * Populate the Indicator Criteria list model object in the model and view
     * provided.
     *
     * @param mav the model and view to add the user list to.
     */
    private void populateIndicatorCriteriaList(ModelAndView mav) {
        List<IndicatorCriteria> indicators = agencyUserManager.getIndicatorCriteriaForLoggedInUser();
        List<Agency> agencies = null;
        if (authorityManager.hasPrivilege(Privilege.MANAGE_INDICATORS, Privilege.SCOPE_ALL)) {
            agencies = agencyUserManager.getAgencies();
        } else {
            User loggedInUser = AuthUtil.getRemoteUserObject();
            Agency usersAgency = loggedInUser.getAgency();
            agencies = new ArrayList<Agency>();
            agencies.add(usersAgency);
        }

        mav.addObject(QaIndicatorCommand.MDL_QA_INDICATORS, indicators);
        mav.addObject(QaIndicatorCommand.MDL_LOGGED_IN_USER,
                AuthUtil.getRemoteUserObject());
        mav.addObject(QaIndicatorCommand.MDL_AGENCIES, agencies);
        mav.setViewName("viewIndicators");
    }

    class DescendingValueComparator implements Comparator {
        Map base;

        public DescendingValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if ((Integer) base.get(a) <= (Integer) base.get(b)) {
                return 1;
            } else if ((Integer) base.get(a) == (Integer) base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    class AscendingValueComparator implements Comparator {
        Map base;

        public AscendingValueComparator(Map base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {
            if ((Integer) base.get(a) >= (Integer) base.get(b)) {
                return 1;
            } else if ((Integer) base.get(a) == (Integer) base.get(b)) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    /**
     * Spring setter method for the <code>IndicatorDAO</code>.
     *
     * @param indicatorDAO The indicatorDAO to set.
     */
    public void setIndicatorDAO(IndicatorDAO indicatorDAO) {
        this.indicatorDAO = indicatorDAO;
    }

    /**
     * @param agencyUserManager the agency user manager.
     */
    public void setAgencyUserManager(AgencyUserManager agencyUserManager) {
        this.agencyUserManager = agencyUserManager;
    }

    /**
     * Spring setter method for the Authority Manager.
     *
     * @param authorityManager The authorityManager to set.
     */
    public void setAuthorityManager(AuthorityManager authorityManager) {
        this.authorityManager = authorityManager;
    }

    /**
     * @param aTargetInstanceManager The targetInstanceManager to set.
     */
    public void setTargetInstanceManager(TargetInstanceManager aTargetInstanceManager) {
        targetInstanceManager = aTargetInstanceManager;
    }

    /**
     * @param excludedIndicators the excludedIndicators to set
     */
    public void setExcludedIndicators(Map<String, String> excludedIndicators) {
        this.excludedIndicators = excludedIndicators;
    }

    /**
     * @param fileNotFoundMessage the fileNotFoundMessage to set
     */
    public void setFileNotFoundMessage(String fileNotFoundMessage) {
        this.fileNotFoundMessage = fileNotFoundMessage;
    }
}
