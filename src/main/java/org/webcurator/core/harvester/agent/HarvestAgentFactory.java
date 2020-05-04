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

import org.webcurator.core.reader.LogReader;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

/**
 * Interface for a factory to create instances of a HarvestAgent.
 *
 * @author nwaight
 */
public interface HarvestAgentFactory {
    /**
     * Return an instance of the harvest agent running on the
     * specified host, port and service name.
     *
     * @return the Harvest Agent
     */
    HarvestAgent getHarvestAgent(HarvestAgentStatusDTO harvestAgentStatusDTO);

    /**
     * Return an instance of the log reader running on the specified host and port
     *
     * @return the log reader
     */
    LogReader getLogReader(HarvestAgentStatusDTO harvestAgentStatusDTO);
}
