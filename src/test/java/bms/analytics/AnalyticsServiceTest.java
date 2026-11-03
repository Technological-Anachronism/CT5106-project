package bms.analytics;

import bms.analytics.application.AnalyticsService;
import bms.analytics.domain.AnalyticsReport;
import bms.analytics.infrastructure.InMemoryAnalyticsRepository;
import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsServiceTest {
    @Test
    void emptyServiceHasNoOutstandingTasksOrUsage() {
        var report = AnalyticsReport.fromTicketStatuses(Instant.EPOCH, List.of());
        assertEquals(new AnalyticsReport.TicketCounts(0, 0, 0, 0), report.tickets());
        assertEquals(0, report.services().get(0).outstandingTasks());
        assertTrue(report.services().get(0).features().stream()
                .allMatch(feature -> feature.usageCount() == 0 && feature.outstandingTasks() == 0));
    }

    @Test
    void closedTicketsCountAsPreviouslyResolvedButNotOutstanding() {
        var report = AnalyticsReport.fromTicketStatuses(Instant.EPOCH,
                List.of(RepairTicket.Status.OPEN, RepairTicket.Status.RESOLVED,
                        RepairTicket.Status.CLOSED, RepairTicket.Status.CLOSED));
        assertEquals(new AnalyticsReport.TicketCounts(4, 1, 1, 2), report.tickets());
        var repair = report.services().get(0);
        assertEquals(2, repair.outstandingTasks());
        assertEquals(3, repair.features().get(1).usageCount());
        assertEquals(1, repair.features().get(1).outstandingTasks());
        assertEquals(2, repair.features().get(2).usageCount());
        assertEquals(1, repair.features().get(2).outstandingTasks());
    }

    @Test
    void readsLiveTicketsAndStoresLatestSnapshot() {
       var ticketRepo = new TestTicketRepository();
        var analyticsRepo = new InMemoryAnalyticsRepository();
        var service = new AnalyticsService(ticketRepo, analyticsRepo);

        var initial = service.overview();
        assertEquals(2, initial.tickets().total());
        assertEquals(1, initial.tickets().open());
        assertEquals(1, initial.tickets().resolved());
        assertEquals(2, initial.services().get(0).outstandingTasks());
        assertEquals("LIVE_TICKETS_DERIVED", initial.services().get(0).source());
        assertTrue(analyticsRepo.findLatest().isPresent());

        var ticket = RepairTicket.open(ticketRepo.nextId(), "Broken printer");
        ticketRepo.save(ticket);
        assertEquals(2, service.overview().tickets().open());
        ticket.resolve();
        ticket.close();
        var changed = service.overview();
        assertEquals(1, changed.tickets().closed());
        assertEquals(1, changed.services().get(0).features().get(2).usageCount());
        assertEquals(changed, analyticsRepo.findLatest().orElseThrow());
    }
    private static final class TestTicketRepository implements TicketRepository {
    private final List<RepairTicket> tickets = new ArrayList<>();

    TestTicketRepository() {
        var first = RepairTicket.open("ticket-1", "Broken monitor");
        var second = RepairTicket.open("ticket-2", "Network issue");
        second.resolve();

        tickets.add(first);
        tickets.add(second);
    }

    @Override
    public List<RepairTicket> findAll() {
        return List.copyOf(tickets);
    }

    @Override
    public RepairTicket findById(String id) {
        return tickets.stream()
                .filter(ticket -> ticket.getId().equals(id))
                .findFirst()
                .orElseThrow(() ->
                        new RepairTicket.TicketProblem("Ticket not found"));
    }

    @Override
    public void save(RepairTicket ticket) {
        tickets.add(ticket);
    }

    @Override
    public String nextId() {
        return "ticket-" + (tickets.size() + 1);
    }
}
}
