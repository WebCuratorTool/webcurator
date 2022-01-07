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
package org.webcurator.core.harvester.agent;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.webcurator.core.reader.LogReader;
import org.webcurator.core.reader.LogReaderClient;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

/**
 * Factory to create HarvestAgent and LogReader instances that use Restful API to communicate with a remote HarvestAgent.
 */
public class HarvestAgentFactory {
    public HarvestAgent getHarvestAgent(HarvestAgentStatusDTO harvestAgentStatusDTO) {
        return new HarvestAgentClient(harvestAgentStatusDTO.getBaseUrl(), new RestTemplateBuilder());
    }

    public LogReader getLogReader(HarvestAgentStatusDTO harvestAgentStatusDTO) {
        return new LogReaderClient(harvestAgentStatusDTO.getBaseUrl(), new RestTemplateBuilder());
    }
}
