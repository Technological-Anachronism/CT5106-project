package bms.analytics;

import bms.analytics.presentation.AnalyticsAdminFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnalyticsAdminFilterTest {
    private static MockHttpServletResponse request(AnalyticsAdminFilter filter,
                                                    String path, String authorization) throws Exception {
        var req = new MockHttpServletRequest("GET", path);
        req.setServletPath(path);
        if (authorization != null) req.addHeader("Authorization", authorization);
        var res = new MockHttpServletResponse();
        filter.doFilter(req, res, new MockFilterChain());
        return res;
    }

    @Test
    void blocksMissingAndIncorrectCredentials() throws Exception {
        var filter = new AnalyticsAdminFilter("admin", "localPassword");
        assertEquals(401, request(filter, "/api/analytics/overview", null).getStatus());
        assertEquals(401, request(filter, "/api/analytics/overview", "Basic invalid").getStatus());
        var invalid = "Basic " + Base64.getEncoder().encodeToString(
                "admin:wrong".getBytes(StandardCharsets.UTF_8));
        assertEquals(401, request(filter, "/api/analytics/overview", invalid).getStatus());
    }

    @Test
    void allowsCorrectCredentialsWithoutAffectingTeammateRoutes() throws Exception {
        var filter = new AnalyticsAdminFilter("admin", "localPassword");
        var valid = "Basic " + Base64.getEncoder().encodeToString(
                "admin:localPassword".getBytes(StandardCharsets.UTF_8));
        assertEquals(200, request(filter, "/api/analytics/overview", valid).getStatus());
        assertEquals(200, request(filter, "/api/portal/tickets", null).getStatus());
    }

    @Test
    void failsClosedWhenCredentialsAreMissing() throws Exception {
        var filter = new AnalyticsAdminFilter("", "");
        assertEquals(503, request(filter, "/api/analytics/overview", null).getStatus());
    }
}
