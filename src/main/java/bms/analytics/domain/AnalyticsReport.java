package bms.analytics.domain;

import java.time.Instant;
import java.util.List;

/** An immutable snapshot of analytics displayed on the admin dashboard. */
public record AnalyticsReport(
        Instant generatedAt,
        TicketCounts tickets,
        List<ServiceStats> services,
        String limitation) {

    public AnalyticsReport {
        services = List.copyOf(services);
    }

    public record TicketCounts(long total, long open, long resolved, long closed) {}

    public record ServiceStats(
            String name,
            String status,
            long outstandingTasks,
            List<FeatureStats> features,
            String source) {
        public ServiceStats {
            features = List.copyOf(features);
        }
    }

    public record FeatureStats(String name, long usageCount, long outstandingTasks) {}
}
