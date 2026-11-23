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

    /** Applies the repair-ticket lifecycle counting rules to captured states.
     * OPEN and RESOLVED remain outstanding until CLOSED. A closed ticket counts as
     * previously resolved because the domain only permits OPEN to RESOLVED to CLOSED.
     * @param generatedAt report generation time
     * @param statuses captured states of the currently stored tickets
     * @return immutable workload and lifecycle-usage statistics
     */
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
        var repair = new ServiceStats("Repair tickets", open + resolved == 0 ? "NO_OUTSTANDING_TASKS" : "TASKS_OUTSTANDING", open + resolved,
                features, "LIVE_TICKETS_DERIVED");
        return new AnalyticsReport(generatedAt, new TicketCounts(total, open, resolved, closed),
                List.of(repair),
                "Repair tickets is the integrated service; status describes outstanding work, not uptime. "
                        + "Feature usage is estimated from ticket states, not recorded user events. "
                        + "Analytics snapshots are held in memory and reset when the server restarts.");
    }

    /** Counts mutually exclusive current states; total is their sum. */
    public record TicketCounts(long total, long open, long resolved, long closed) {}

    /** Workload status and outstanding actions for one integrated service. */
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

    /** Estimated successful lifecycle operations and pending actions for a feature. */
    public record FeatureStats(String name, long usageCount, long outstandingTasks) {}
}
