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
import org.webcurator.core.reader.LogReaderClient;

/**
 * Factory to create HarvestAgent instances that use SOAP to communicate with a remote HarvestAgent.
 */
public class HarvestAgentFactoryImpl implements HarvestAgentFactory {
    public HarvestAgent getHarvestAgent(String aHost, int aPort) {
        HarvestAgentClient ha = new HarvestAgentClient(aHost, aPort);
        return ha;
    }

    public LogReader getLogReader(String aHost, int aPort) {
        return new LogReaderClient(aHost, aPort);
    }
}
