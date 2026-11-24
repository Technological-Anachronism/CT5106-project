package bms.analytics.presentation;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/** Protects Analytics routes with configured Basic Auth and disables response caching.
 * Missing configuration fails closed with HTTP 503; invalid credentials return 401.
 * This adapter is independent of team user roles and requires HTTPS outside local use.
 */
@Component
public class AnalyticsAdminFilter extends OncePerRequestFilter {
    private final String username;
    private final String password;

    public AnalyticsAdminFilter(
            @Value("${bms.analytics.admin.username:}") String username,
            @Value("${bms.analytics.admin.password:}") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return !(path.equals("/api/analytics") || path.startsWith("/api/analytics/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        if (username.isBlank() || password.isBlank()) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Analytics admin credentials are not configured");
            return;
        }
        if (!isValid(request.getHeader("Authorization"))) {
            response.setHeader("WWW-Authenticate", "Basic realm=\"Analytics prototype\"");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        chain.doFilter(request, response);
    }

    private boolean isValid(String header) {
        if (header == null || !header.startsWith("Basic ")) return false;
        try {
            byte[] encoded = Base64.getDecoder().decode(header.substring(6));
            if (encoded.length > 8192) return false;
            String decoded = new String(encoded, StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator < 0) return false;
            String name = decoded.substring(0, separator);
            String secret = decoded.substring(separator + 1);
            return constantTimeEqual(name, username) && constantTimeEqual(secret, password);
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean constantTimeEqual(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                b.getBytes(StandardCharsets.UTF_8));
    }
}
