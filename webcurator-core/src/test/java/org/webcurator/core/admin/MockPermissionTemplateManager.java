package org.webcurator.core.admin;

import org.webcurator.auth.*;
import org.webcurator.domain.*;

public class MockPermissionTemplateManager extends PermissionTemplateManager {

	public MockPermissionTemplateManager(String filename) {
		super();
		this.setAuthorityManager(new AuthorityManager());
		this.setPermissionTemplateDAO(new MockPermissionTemplateDAO(filename));
	}

}
