package bms.analytics;

import bms.analytics.application.AnalyticsService;
import bms.analytics.infrastructure.InMemoryAnalyticsRepository;
import bms.domain.RepairTicket;
import bms.infrastructure.InMemoryTicketRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsServiceTest {
    @Test
    void readsLiveTicketsAndStoresLatestSnapshot() {
        var ticketRepo = new InMemoryTicketRepository();
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
}
