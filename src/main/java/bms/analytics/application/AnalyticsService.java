package bms.analytics.application;

import bms.analytics.domain.AnalyticsReport;
import bms.analytics.domain.AnalyticsRepository;
import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/** Reads the team's existing tickets and stores the calculated analytics snapshot. */
@Service
public class AnalyticsService {
    private final TicketRepository tickets;
    private final AnalyticsRepository analytics;

    public AnalyticsService(TicketRepository tickets, AnalyticsRepository analytics) {
        this.tickets = tickets;
        this.analytics = analytics;
    }

    public AnalyticsReport overview() {
        // Work with the tickets already stored by the portal, rather than a second demo list.
        List<RepairTicket> all = tickets.findAll();
        long open = all.stream().filter(t -> t.getStatus() == RepairTicket.Status.OPEN).count();
        long resolved = all.stream().filter(t -> t.getStatus() == RepairTicket.Status.RESOLVED).count();
        long closed = all.stream().filter(t -> t.getStatus() == RepairTicket.Status.CLOSED).count();
        long total = all.size();

        // Proxies derived from current ticket states, not a historical action log.
        // 'Created' = all tickets; 'Resolved' = resolved or subsequently closed;
        // 'Closed' = currently closed. A database/event log can replace these later.
        var features = List.of(
                new AnalyticsReport.FeatureStats("Open a ticket", total, 0),
                new AnalyticsReport.FeatureStats("Resolve a ticket", resolved + closed, open),
                new AnalyticsReport.FeatureStats("Close a ticket", closed, resolved)
        );
        var repair = new AnalyticsReport.ServiceStats(
                "Repair tickets", "PORTAL_AVAILABLE", open + resolved, features, "LIVE_TICKETS_DERIVED");
        var report = new AnalyticsReport(
                Instant.now(),
                new AnalyticsReport.TicketCounts(total, open, resolved, closed),
                List.of(repair),
                "Only Repair tickets is currently integrated; status indicates portal existence, not uptime. Feature usage is estimated "
                    + "from ticket states, not recorded user events. Analytics snapshots "
                    + "are held in memory and reset when the server restarts.");
        // Keep the latest result for the prototype dashboard.
        analytics.save(report);
        return report;
    }
}
