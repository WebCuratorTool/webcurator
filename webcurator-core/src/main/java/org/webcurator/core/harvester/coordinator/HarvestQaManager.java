package org.webcurator.core.harvester.coordinator;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.webcurator.core.exceptions.DigitalAssetStoreException;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.rules.QaRecommendationService;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.TargetInstanceDAO;
import org.webcurator.domain.model.core.*;
import org.webcurator.domain.model.dto.HarvestHistoryDTO;

public class HarvestQaManager {

    private TargetInstanceManager targetInstanceManager;
    private TargetInstanceDAO targetInstanceDao;

    /**
     * enables the new Target Instance and Harvest Summary pages
     **/
    private boolean enableQaModule = false;

    /**
     * modification note applied when a harvest is auto-pruned
     **/
    private String autoPrunedNote = null;
    private String autoQAUrl = "";


    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The component that will provide an <code>Indicator</code> based QA
     * recommendation
     **/
    @Autowired
    private QaRecommendationService qaRecommendationService;

    public void autoPrune(TargetInstance ti) throws DigitalAssetStoreException {
        if (!ti.getTarget().isAutoPrune()) {
            return;
        }

        // fetch the ti for the previous crawl from the harvest histories
        Iterator<HarvestHistoryDTO> histories = targetInstanceManager.getHarvestHistory(ti.getTarget().getOid()).iterator();

        // find the last archived history
        boolean found = false;
        HarvestHistoryDTO historyDto = null;
        while (histories.hasNext() && !found) {
            historyDto = histories.next();
            if (!historyDto.getOid().equals(ti.getOid()) && historyDto.getState().equals("Archived")) {
                found = true;
            }
        }
        if (found) {
            // fetch the harvest results for the last harvested ti
            Iterator<HarvestResult> harvestResults = targetInstanceManager.getHarvestResults(historyDto.getOid()).iterator();

            boolean resultFound = false;
            HarvestResult harvestResult = null;

            // find the last endorsed result
            while (harvestResults.hasNext() && !resultFound) {
                harvestResult = harvestResults.next();

                if (harvestResult.getState() == HarvestResult.STATE_ENDORSED) {
                    resultFound = true;
                }
            }

            if (resultFound) {
                ArrayList<String> modificationNote = new ArrayList<String>();
                modificationNote.add(autoPrunedNote);

                // prune all the uris that have previously been pruned
//                qualityReviewFacade.copyAndPrune(targetInstanceManager.getHarvestResults(ti.getOid()).get(0).getOid(),
//                        new ArrayList<String>(urisToDelete), new ArrayList<HarvestResourceDTO>(), autoPrunedNote, modificationNote);
            }

        }

    }

    public void initialiseQaRecommentationService(Long harvestResultOid) {
        // if the QA service is enabled, then derive the QA Indicators for this
        // ti
        if (enableQaModule) {
            HarvestResult ahr = targetInstanceDao.getHarvestResult(harvestResultOid, false);
            TargetInstance ti = ahr.getTargetInstance();
            if (ti == null) {
                throw new WCTRuntimeException(
                        "Unknown TargetInstance recieved, failed to save initialise the QaRecommentationService.");
            }
            runQaRecommentationService(ti);
        }
    }

    public void runQaRecommentationService(TargetInstance ti) {
        //TODO: To fix the QARecommendation based on the indexes in BDB
        /**
         // fetch the reference crawl if it one exists
         TargetInstance referenceCrawl = null;
         Long referenceCrawlOid = ti.getTarget().getReferenceCrawlOid();
         if (referenceCrawlOid != null) {
         referenceCrawl = targetInstanceDao.load(referenceCrawlOid);
         }

         try {
         // initialise the knowledge session
         qaRecommendationService.buildKnowledgeSession();
         // fetch the indicator criterias for the ti's agency
         List<IndicatorCriteria> criteria = targetInstanceManager.getIndicatorCriteriasByAgencyOid(ti.getOwner().getAgency()
         .getOid());
         // process the indicators
         qaRecommendationService.applyRules(ti, referenceCrawl, criteria);
         // persist the results
         targetInstanceManager.save(ti);

         } catch (DroolsParserException e) {
         log.error("Rules Engine encountered errors when processing indicators", e);
         } catch (IOException e) {
         log.error("Error encountered when processing rules file", e);
         } catch (Exception e) {
         log.error("Unexpected error encountered during Rules Engine processing of ti " + ti.getOid(), e);
         }
         */
    }

    public void triggerAutoQA(HarvestResult ahr) {
        TargetInstance ti = ahr.getTargetInstance();

        if (autoQAUrl != null && autoQAUrl.length() > 0 && ti.isUseAQA()) {
            GetMethod getMethod = new GetMethod(autoQAUrl);

            String primarySeed = "";

            // Might be nice to use the SeedHistory here, but as this won't
            // necessarily be turned on, we can't use it reliably
            Set<Seed> seeds = ti.getTarget().getSeeds();
            Iterator<Seed> it = seeds.iterator();
            while (it.hasNext()) {
                Seed seed = it.next();
                if (seed.isPrimary()) {
                    primarySeed = seed.getSeed();
                    break;
                }
            }

            NameValuePair[] params = {new NameValuePair("targetInstanceId", ti.getOid().toString()),
                    new NameValuePair("harvestNumber", Integer.toString(ahr.getHarvestNumber())),
                    new NameValuePair("primarySeed", primarySeed)};

            getMethod.setQueryString(params);
            HttpClient client = new HttpClient();
            try {
                int result = client.executeMethod(getMethod);
                if (result != HttpURLConnection.HTTP_OK) {
                    log.error("Unable to initiate Auto QA. Response at " + autoQAUrl + " is " + result);
                }
            } catch (Exception e) {
                log.error("Unable to initiate Auto QA.", e);
            }
        }
    }

    public void setAutoQAUrl(String autoQAUrl) {
        this.autoQAUrl = autoQAUrl;
    }

    public String getAutoQAUrl() {
        return autoQAUrl;
    }

    public QaRecommendationService getQaRecommendationService() {
        return qaRecommendationService;
    }

    public void setQaRecommendationService(QaRecommendationService qaRecommendationService) {
        this.qaRecommendationService = qaRecommendationService;
    }

    /**
     * Enable/disable the new QA Module (disabled by default)
     */
    public void setEnableQaModule(Boolean enableQaModule) {
        this.enableQaModule = enableQaModule;
    }

    /**
     * modification note applied when a harvest is auto-pruned
     **/
    public void setAutoPrunedNote(String autoPrunedNote) {
        this.autoPrunedNote = autoPrunedNote;
    }

    public TargetInstanceManager getTargetInstanceManager() {
        return targetInstanceManager;
    }

    public void setTargetInstanceManager(TargetInstanceManager targetInstanceManager) {
        this.targetInstanceManager = targetInstanceManager;
    }

    public TargetInstanceDAO getTargetInstanceDao() {
        return targetInstanceDao;
    }

    public void setTargetInstanceDao(TargetInstanceDAO targetInstanceDao) {
        this.targetInstanceDao = targetInstanceDao;
    }


}
