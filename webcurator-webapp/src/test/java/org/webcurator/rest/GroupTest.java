package org.webcurator.rest;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.webcurator.domain.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


@WebMvcTest(controllers = Groups.class)
@AutoConfigureMockMvc(addFilters = false)
// Force the controller to be loaded as a bean
@Import(Groups.class)
public class GroupTest extends AbstractControllerTest {
    @Test
    public void testGet() throws Exception {
        Pagination mockResult = createMockTargetGroupPagination();

        // 2. Mock the TargetManager call
        // Ensure you match the method signature (10 arguments)
        given(targetDAO.searchGroups(
                anyInt(),    // pageNumber
                anyInt(),    // pageSize
                any(),   // searchOid
                any(), // name
                any(),       // states (Set)
                any(), // owner
                any(), // agency
                any(), // memberOf
                any(), // groupType
                anyBoolean() // nondisplayonly
        )).willReturn(mockResult);

        mockMvc.perform(get("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
