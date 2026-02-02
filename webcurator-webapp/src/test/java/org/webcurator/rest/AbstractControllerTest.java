package org.webcurator.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.webcurator.core.targets.TargetManager2;
import org.webcurator.domain.*;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.BusinessObjectFactory;
import org.webcurator.domain.model.core.TargetGroup;

import java.util.Collections;

public class AbstractControllerTest {
    @SpringBootConfiguration // Provides the missing configuration context
    @EnableAutoConfiguration
    static class TestConfig {
    }


    @Autowired
    public MockMvc mockMvc;

    @MockBean
    public TargetDAO targetDAO;

    @MockBean
    public AnnotationDAO annotationDAO;

    @MockBean
    public UserRoleDAO userRoleDAO;

    @MockBean
    public ProfileDAO profileDAO;

    @MockBean
    public BusinessObjectFactory businessObjectFactory;

    @MockBean
    public TargetManager2 targetManager;

    public Pagination createMockTargetGroupPagination() {
        // 1. Create a dummy Owner (User)
        Agency agency = new Agency();
        agency.setOid(1L);
        agency.setName("agency1");

        User mockUser = new User();
        mockUser.setOid(99L);
        mockUser.setUsername("admin");
        mockUser.setAgency(agency);

        // 2. Create a dummy TargetGroup
        TargetGroup group = new TargetGroup();
        // AbstractTarget fields
        group.setOid(1L);
        group.setName("Mock Research Group");
        group.setDescription("A group for testing purposes");
        group.setState(TargetGroup.STATE_ACTIVE);
        group.setOwner(mockUser);

        // TargetGroup specific fields
        group.setType("Collection");

        // 3. Create the Pagination object
        // Using the constructor: Pagination(Collection items, int aPage, int aPageSize)
        return new Pagination(Collections.singletonList(group), 0, 10);
    }
}
