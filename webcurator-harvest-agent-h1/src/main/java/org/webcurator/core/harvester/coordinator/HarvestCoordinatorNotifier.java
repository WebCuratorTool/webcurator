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
package org.webcurator.core.harvester.coordinator;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.check.CheckNotifier;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.core.rest.AbstractRestClient;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

import java.net.URI;

/**
 * The HarvestCoordinatorNotifier uses SOAP to send messages back to the core.
 * These include notifications, heartbeats and job completions messages.
 * @author nwaight
 */
public class HarvestCoordinatorNotifier extends AbstractRestClient implements HarvestAgentListener, CheckNotifier {
	/** The harvest agent that the this notifier is running on. */
	HarvestAgent agent;

    public HarvestCoordinatorNotifier(String scheme, String host, int port, RestTemplateBuilder restTemplateBuilder) {
        super(scheme, host, port, restTemplateBuilder);
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#heartbeat(org.webcurator.core.harvester.agent.HarvestAgentStatus)
     */
    public void heartbeat(HarvestAgentStatusDTO aStatus){
        try{
            log.info("WCT: Start of heartbeat");

            RestTemplate restTemplate = restTemplateBuilder.build();

            String uri = getUrl(HarvestCoordinatorPaths.HEARTBEAT);

            HttpEntity<String> request = this.createHttpRequestEntity(aStatus);

            restTemplate.postForObject(uri, request, String.class);
            log.info("WCT: End of heartbeat");
        } catch (Exception ex) {
            log.error("Heartbeat Notification failed : " + ex.getMessage(), ex);
        }
    }

    @Override
    public void requestRecovery(HarvestAgentStatusDTO aStatus) {
        // Placeholder - not used with Heritrix 1x
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(org.webcurator.core.model.HarvestResult)
     */
    public void harvestComplete(HarvestResultDTO aResult) {
        try {
            log.info("WCT: Start of harvestComplete");

            HttpEntity<String> request = this.createHttpRequestEntity(aResult);

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.HARVEST_COMPLETE));
            URI uri = uriComponentsBuilder.buildAndExpand().toUri();

            restTemplate.postForObject(uri, request, String.class);

            log.info("WCT: End of HarvestComplete");
        }
        catch(Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Harvest Complete Notification failed : " + ex.getMessage(), ex);
            } 
            throw new WCTRuntimeException(ex);
        }
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(Long, String)
     */
    public void notification(Long aTargetInstanceOid, int notificationCategory, String aMessageType) {
        try {
            log.debug("WCT: Start of notification");


            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.NOTIFICATION_BY_OID))
                    .queryParam("target-instance-oid", aTargetInstanceOid)
                    .queryParam("notification-category", notificationCategory)
                    .queryParam("message-type", aMessageType);

            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(), null, String.class);

            log.debug("WCT: End of notification");
        }
        catch (Exception ex) {
            log.error("Notification failed : " + ex.getMessage(), ex);
        }        
    }
    
    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(String, String)
     */
    public void notification(String aSubject, int notificationCategory, String aMessage) {
        try {
            log.debug("WCT: Start of notification");

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.NOTIFICATION_BY_SUBJECT))
                    .queryParam("subject", agent.getName() + " " + aSubject)
                    .queryParam("notification-category", notificationCategory)
                    .queryParam("message", aMessage);

            log.debug("WCT: End of notification");
        }
        catch (Exception ex) {
            log.error("Notification failed : " + ex.getMessage(), ex);
        }        
    }

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(HarvestAgent agent) {
		this.agent = agent;
	}   
}
