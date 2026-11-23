package bms.analytics;

import bms.analytics.domain.AnalyticsRepository;
import bms.application.UserRepository;
import bms.domain.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Verifies Analytics MVC/filter wiring and its integration with the existing JPA ticket workflows. */
@SpringBootTest(properties = {"bms.analytics.admin.username=integration", "bms.analytics.admin.password=integration-test-only"})
@AutoConfigureMockMvc
@ActiveProfiles("analytics-test")
@TestExecutionListeners(listeners = {DependencyInjectionTestExecutionListener.class, TransactionalTestExecutionListener.class})
@Transactional
class AnalyticsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired TicketRepository tickets;
    @Autowired UserRepository users;
    @Autowired AnalyticsRepository analytics;
    private static final String PATH = "/api/analytics/overview";
    private String authorization() {
        return "Basic " + Base64.getEncoder().encodeToString("integration:integration-test-only".getBytes(StandardCharsets.UTF_8));
    }
    @Test
    void statisticsRequireCredentialsAndMustNotBeCached() throws Exception {
        mvc.perform(get(PATH).servletPath(PATH)).andExpect(status().isUnauthorized());
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization", "Basic invalid"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization", authorization()))
                .andExpect(status().isOk()).andExpect(header().string("Cache-Control", "no-store"));
    }
    @Test
    void persistedWorkflowChangesAppearInAnalytics() throws Exception {
        long initialTotal = tickets.findAll().size();
        long initialClosed = tickets.findAll().stream().filter(t -> t.getStatus() == RepairTicket.Status.CLOSED).count();
        users.save(new User(991L, "Assigned staff", "assigned@test.example", Role.STAFF));
        users.save(new User(992L, "Other staff", "other@test.example", Role.STAFF));
        String result = mvc.perform(post("/api/portal/tickets").param("description", "Analytics integration workflow"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String id = new com.fasterxml.jackson.databind.ObjectMapper().readTree(result).get("id").asText();
        mvc.perform(post("/api/management/tickets/" + id + "/assign").param("assigneeId", "991"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/management/tickets/" + id + "/resolve").param("userId", "992"))
                .andExpect(status().isBadRequest());
        assertEquals(RepairTicket.Status.OPEN, tickets.findById(id).getStatus());
        mvc.perform(post("/api/management/tickets/" + id + "/resolve").param("userId", "991"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/portal/tickets/" + id + "/close")).andExpect(status().isOk());
        assertEquals(RepairTicket.Status.CLOSED, tickets.findById(id).getStatus());
        mvc.perform(get(PATH).servletPath(PATH).header("Authorization", authorization()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tickets.total").value(initialTotal + 1))
                .andExpect(jsonPath("$.tickets.closed").value(initialClosed + 1));
        assertTrue(analytics.findLatest().isPresent());
    }
}
