package bms.analytics.domain;

import bms.domain.RepairTicket;
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

    /** Business rules for the currently integrated repair-ticket service. */
    public static AnalyticsReport fromTicketStatuses(Instant generatedAt, List<RepairTicket.Status> statuses) {
        long open = statuses.stream().filter(s -> s == RepairTicket.Status.OPEN).count();
        long resolved = statuses.stream().filter(s -> s == RepairTicket.Status.RESOLVED).count();
        long closed = statuses.stream().filter(s -> s == RepairTicket.Status.CLOSED).count();
        long total = statuses.size();
        // The current lifecycle has no deletion or reopening: CLOSED has previously been RESOLVED.
        var features = List.of(
                new FeatureStats("Open a ticket", total, 0),
                new FeatureStats("Resolve a ticket", resolved + closed, open),
                new FeatureStats("Close a ticket", closed, resolved));
        var repair = new ServiceStats("Repair tickets", "PORTAL_AVAILABLE", open + resolved,
                features, "LIVE_TICKETS_DERIVED");
        return new AnalyticsReport(generatedAt, new TicketCounts(total, open, resolved, closed),
                List.of(repair),
                "Only Repair tickets is currently integrated; status indicates portal existence, not uptime. "
                        + "Feature usage is estimated from ticket states, not recorded user events. "
                        + "Analytics snapshots are held in memory and reset when the server restarts.");
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
