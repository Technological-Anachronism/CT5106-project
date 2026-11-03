package bms.analytics.application;

import bms.analytics.domain.AnalyticsReport;
import bms.analytics.domain.AnalyticsRepository;
import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

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
        // Read each mutable ticket's state once so all counts use the same captured values.
        var statuses = tickets.findAll().stream().map(RepairTicket::getStatus).toList();
        var report = AnalyticsReport.fromTicketStatuses(Instant.now(), statuses);
        // Keep the latest result for the prototype dashboard.
        analytics.save(report);
        return report;
    }
}
