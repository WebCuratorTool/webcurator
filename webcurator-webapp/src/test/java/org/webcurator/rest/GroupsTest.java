package org.webcurator.rest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.*;

import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.webcurator.domain.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.webcurator.domain.model.auth.Agency;
import org.webcurator.domain.model.auth.User;
import org.webcurator.domain.model.core.Profile;
import org.webcurator.domain.model.core.TargetGroup;
import org.webcurator.domain.model.core.Target;
import org.webcurator.domain.model.core.GroupMember;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


@WebMvcTest(controllers = Groups.class)
@AutoConfigureMockMvc(addFilters = false)
// Force the controller to be loaded as a bean
@Import(Groups.class)
public class GroupsTest extends AbstractControllerTest {
    private TargetGroup mockGroup;
    private Target mockTarget;


    @BeforeEach
    void setUp() {
        User mockUser = new User();
        mockUser.setUsername("testUser");
        Agency agency = new Agency();
        agency.setName("TestAgency");
        mockUser.setAgency(agency);
        mockUser.setFirstname("Fairy");
        mockUser.setLastname("Tom");

        Profile mockProfile = new Profile();
        mockProfile.setHarvesterType("H3");

        mockGroup = new TargetGroup();
        mockGroup.setOid(123L);
        mockGroup.setName("Test Group");
        mockGroup.setOwner(mockUser);
        mockGroup.setProfile(mockProfile);

        mockTarget = new Target();
        mockTarget.setOwner(mockUser);
    }

    @Test
    void testGetGroupById_Success() throws Exception {
        when(targetDAO.loadGroup(123L)).thenReturn(mockGroup);
        when(annotationDAO.loadAnnotations(anyString(), anyLong())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/v1/groups/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.general.id").value(123))
                .andExpect(jsonPath("$.general.name").value("Test Group"));
    }

    @Test
    void testGetGroupById_NotFound() throws Exception {
        when(targetDAO.loadGroup(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/groups/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteGroup_Success() throws Exception {
        when(targetDAO.loadGroup(123L)).thenReturn(mockGroup);

        mockMvc.perform(delete("/api/v1/groups/123"))
                .andExpect(status().isOk());

        Mockito.verify(targetDAO).deleteGroup(any(TargetGroup.class));
    }

    @Test
    void testGetStates() throws Exception {
        mockMvc.perform(get("/api/v1/groups/states"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(String.format("$.%d", TargetGroup.STATE_PENDING)).value("Pending"))
                .andExpect(jsonPath(String.format("$.%d", TargetGroup.STATE_INACTIVE)).value("Inactive"))
                .andExpect(jsonPath(String.format("$.%d", TargetGroup.STATE_ACTIVE)).value("Active"));
    }

    @Test
    void testGetTypes() throws Exception {
        mockMvc.perform(get("/api/v1/groups/types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("collection"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testAddMember_Success() throws Exception {
        when(targetDAO.loadGroup(123L)).thenReturn(mockGroup);
        when(targetDAO.load(456L)).thenReturn(mockTarget);

        mockMvc.perform(post("/api/v1/groups/123/members/456"))
                .andExpect(status().isOk());
    }

    @Test
    void testAddMember_GroupNotFound() throws Exception {
        when(targetDAO.loadGroup(123L)).thenReturn(null);

        mockMvc.perform(post("/api/v1/groups/123/members/456"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testSearchGroups_EmptyParams() throws Exception {
        // Mocking the Pagination object returned by TargetDAO
        Pagination mockPagination = Mockito.mock(Pagination.class);
        when(mockPagination.getList()).thenReturn(Collections.singletonList(mockGroup));
        when(mockPagination.getTotal()).thenReturn(1);

        when(targetDAO.searchGroups(anyInt(), anyInt(), any(), any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(mockPagination);

        mockMvc.perform(get("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(1))
                .andExpect(jsonPath("$.groups[0].name").value("Test Group"));
    }

    @Test
    void testUpdateGroup_Failure_NonExistent() throws Exception {
        when(targetDAO.loadGroup(anyLong(), anyBoolean())).thenReturn(null);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("name", "Updated Name");

        mockMvc.perform(put("/api/v1/groups/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateMap)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testRemoveMember_Success() throws Exception {
        // Setup group with a child
        GroupMember member = new GroupMember();
        Target child = new Target();
        child.setOid(456L);
        member.setChild(child);

        mockGroup.getChildren().add(member);

        when(targetDAO.loadGroup(123L)).thenReturn(mockGroup);
        when(targetDAO.load(456L)).thenReturn(child);

        mockMvc.perform(delete("/api/v1/groups/123/members/456"))
                .andExpect(status().isOk());
    }
}
