package org.webcurator.core.harvester.agent;

import org.webcurator.core.reader.*;
import org.webcurator.domain.model.core.harvester.agent.HarvestAgentStatusDTO;

public class MockHarvestAgentFactory implements HarvestAgentFactory {
	static MockHarvestAgent agent = null;

	public MockHarvestAgent getMockHarvestAgent() {
		if(agent == null)
		{
			agent = new MockHarvestAgent(); 
		}
		return agent;
	}

	@Override
	public HarvestAgent getHarvestAgent(HarvestAgentStatusDTO harvestAgentStatusDTO) {
		return getMockHarvestAgent();
	}

	@Override
	public LogReader getLogReader(HarvestAgentStatusDTO harvestAgentStatusDTO) {
		return new MockLogReader();
	}
}
