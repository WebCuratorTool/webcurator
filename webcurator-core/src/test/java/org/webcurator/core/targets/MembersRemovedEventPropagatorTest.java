package org.webcurator.core.targets;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.webcurator.core.scheduler.TargetInstanceManager;
import org.webcurator.domain.model.core.TargetGroup;
import org.webcurator.domain.model.core.TargetInstance;
import org.webcurator.test.BaseWCTTest;

public class MembersRemovedEventPropagatorTest extends BaseWCTTest<MembersRemovedEventPropagator>{

	private MockTargetManager targetManager;
	private TargetInstanceManager instanceManager;
	
	public MembersRemovedEventPropagatorTest()
	{
		super(MembersRemovedEventPropagator.class, 
				"/org/webcurator/core/targets/MembersRemovedEventPropagatorTest.xml",
				false);
	}
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		targetManager = new MockTargetManager(testFile);
		instanceManager = targetManager.getInstanceManager();
	}

	@Test
	public final void testRunEventChain() {
		TargetGroup parentGroup = targetManager.loadGroup(15001L);
		TargetGroup childGroup = targetManager.loadGroup(15000L);
		try
		{
			testInstance = new MembersRemovedEventPropagator(targetManager, instanceManager, parentGroup, childGroup);
			TargetInstance ti = instanceManager.getTargetInstance(5003L);
			assertTrue(ti != null);
			testInstance.runEventChain();
			ti = instanceManager.getTargetInstance(5003L);
			assertTrue(ti == null);
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}
	}

}
