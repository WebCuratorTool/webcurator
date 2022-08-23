package org.webcurator.core.sites;

import org.webcurator.domain.*;
import org.webcurator.core.util.*;
import org.webcurator.core.agency.*;
import org.webcurator.core.notification.*;

public class MockSiteManager extends SiteManager {

	public MockSiteManager(String filename) {
		
		super();
		this.setSiteDao(new MockSiteDAO(filename));
		this.setAuditor(new TestAuditor());
		this.setAgencyUserManager(new MockAgencyUserManager(filename));
		this.setAnnotationDAO(new MockAnnotationDAO(filename));
		this.setIntrayManager(new MockInTrayManager(filename));
	}

}
