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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.webcurator.core.check.CheckNotifier;
import org.webcurator.core.exceptions.WCTRuntimeException;
import org.webcurator.core.harvester.agent.HarvestAgent;
import org.webcurator.domain.model.core.HarvestResultDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;
import org.webcurator.domain.model.core.harvester.agent.HarvesterStatusDTO;

/**
 * The HarvestCoordinatorNotifier uses SOAP to send messages back to the core.
 * These include notifications, heartbeats and job completions messages.
 * @author nwaight
 */
public class HarvestCoordinatorNotifier implements HarvestAgentListener, CheckNotifier {
	/** The harvest agent that the this notifier is running on. */
	HarvestAgent agent;
    /** the protocol for the wct host name or ip address. */
    private String protocol = "http";
    /** the host name or ip-address for the wct. */
    private String host = "localhost";
    /** the port number for the wct. */
    private int port = 8080;
    /** Flag used to control a harvest recovery attempt on startup. */
    public String attemptRecovery = "false";
    /** the logger. */
    private static Log log = LogFactory.getLog(HarvestCoordinatorNotifier.class);

    @Autowired
    protected RestTemplateBuilder restTemplateBuilder;

    public String baseUrl() {
        return protocol + "://" + host + ":" + port;
    }

    public String getUrl(String appendUrl) {
        return baseUrl() + appendUrl;
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#heartbeat(org.webcurator.core.harvester.agent.HarvestAgentStatus)
     */
    public void heartbeat(HarvestAgentStatusDTO aStatus) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("WCT: Start of heartbeat");
            }
            log.info("WCT: Start of heartbeat");

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.HEARTBEAT))
                    .queryParam("harvest-agent-status", aStatus);

            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                    null, Void.class);

            if (log.isDebugEnabled()) {
                log.debug("WCT: End of heartbeat");
            }
            log.info("WCT: End of heartbeat");
        }
        catch(Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Heartbeat Notification failed : " + ex.getMessage(), ex);
            }
        }
    }

    public void requestRecovery(String haHost, int haPort, String haService) {
        try {

            if(attemptRecovery()){
                log.info("Harvest Agent attempting a recovery request");

                if (log.isDebugEnabled()) {
                    log.debug("WCT: Start of requestRecovery");
                }

                RestTemplate restTemplate = restTemplateBuilder.build();
                UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.RECOVERY))
                        .queryParam("host", haHost)
                        .queryParam("port", haPort)
                        .queryParam("service", haService);

                restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                        null, Void.class);

                if (log.isDebugEnabled()) {
                    log.debug("WCT: End of requestRecovery");
                }
                setAttemptRecovery("false");
            }
        }
        catch(Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Recovery Request failed : " + ex.getMessage(), ex);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#harvestComplete(org.webcurator.core.model.HarvestResult)
     */
    public void harvestComplete(HarvestResultDTO aResult) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("WCT: Start of harvestComplete");
            }

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.HARVEST_COMPLETE))
                    .queryParam("harvest-result", aResult);

            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                    null, Void.class);

            if (log.isDebugEnabled()) {
                log.debug("WCT: End of HarvestComplete");
            }
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
            if (log.isDebugEnabled()) {
                log.debug("WCT: Start of notification");
            }

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.NOTIFICATION_BY_OID))
                    .queryParam("target-instance-oid", aTargetInstanceOid)
                    .queryParam("notification-category", notificationCategory)
                    .queryParam("message-type", aMessageType);

            restTemplate.postForObject(uriComponentsBuilder.buildAndExpand().toUri(),
                    null, Void.class);

            if (log.isDebugEnabled()) {
                log.debug("WCT: End of notification");
            }
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Notification failed : " + ex.getMessage(), ex);
            }  
        }        
    }
    
    /* (non-Javadoc)
     * @see org.webcurator.core.harvester.coordinator.HarvestAgentListener#notification(String, String)
     */
    public void notification(String aSubject, int notificationCategory, String aMessage) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("WCT: Start of notification");
            }

            RestTemplate restTemplate = restTemplateBuilder.build();
            UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(getUrl(HarvestCoordinatorPaths.NOTIFICATION_BY_SUBJECT))
                    .queryParam("subject", agent.getName() + " " + aSubject)
                    .queryParam("notification-category", notificationCategory)
                    .queryParam("message", aMessage);

            if (log.isDebugEnabled()) {
                log.debug("WCT: End of notification");
            }
        }
        catch (Exception ex) {
            if (log.isErrorEnabled()) {
                log.error("Notification failed : " + ex.getMessage(), ex);
            }  
        }        
    }

    /**
     * @param aProtocol The host to set.
     */
    public void setProtocol(String aProtocol) {
        this.protocol = aProtocol;
    }

    /**
     * @param aHost The host to set.
     */
    public void setHost(String aHost) {
        this.host = aHost;
    }

    /**
     * @param aPort The port to set.
     */
    public void setPort(int aPort) {
        this.port = aPort;
    }

	/**
	 * @param agent the agent to set
	 */
	public void setAgent(HarvestAgent agent) {
		this.agent = agent;
	}

    /**
     * Return boolean value of attemptRecovery String
     * @return boolean
     */
    public boolean attemptRecovery() {
        if(attemptRecovery.equals("true")){
            return true;
        }
        return false;
    }

    /**
     * @param attemptRecovery boolean flag
     */
    public void setAttemptRecovery(String attemptRecovery) {
        this.attemptRecovery = attemptRecovery;
    }
}
