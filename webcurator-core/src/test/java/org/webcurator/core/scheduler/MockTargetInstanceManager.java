package org.webcurator.core.scheduler;

import org.webcurator.core.notification.MockInTrayManager;
import org.webcurator.core.util.TestAuditor;
import org.webcurator.domain.*;

public class MockTargetInstanceManager extends TargetInstanceManagerImpl {

	private MockTargetInstanceDAO mTargetInstanceDao = null;
	private MockIndicatorDAO mIndicatorDAO = null;
	private MockIndicatorCriteriaDAO mIndicatorCriteriaDAO = null;
	
	public MockTargetInstanceManager(String filename) 
	{
		super();
		this.setAnnotationDAO(new MockAnnotationDAO(filename));
		this.setProfileDAO(new MockProfileDAO(filename));
		this.setAuditor(new TestAuditor());
		this.setInTrayManager(new MockInTrayManager(filename));
		
		mIndicatorCriteriaDAO = new MockIndicatorCriteriaDAO(filename);
		this.setIndicatorCriteriaDAO(mIndicatorCriteriaDAO);
		mIndicatorDAO = new MockIndicatorDAO(filename);
		this.setIndicatorDAO(mIndicatorDAO);

		mTargetInstanceDao = new MockTargetInstanceDAO(filename);
		this.setTargetInstanceDao(mTargetInstanceDao);
	}
	
	public TargetInstanceDAO getTargetInstanceDAO()
	{
		return mTargetInstanceDao;
	}
	
	public IndicatorDAO getIndicatorDAO() {
		return mIndicatorDAO;
	}

	public IndicatorCriteriaDAO getIndicatorCriteriaDAO() {
		return mIndicatorCriteriaDAO;
	}
}
