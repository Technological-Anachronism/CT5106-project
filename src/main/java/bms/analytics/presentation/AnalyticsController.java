package bms.analytics.presentation;

import bms.analytics.application.AnalyticsService;
import bms.analytics.domain.AnalyticsReport;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The filter in this package restricts this read-only endpoint. */
@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) {
        this.service = service;
    }

    /** Returns a fresh administrator report with cache prevention.
     * @return HTTP 200 with live statistics, after the authentication filter succeeds
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsReport> overview() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(service.overview());
    }
}
