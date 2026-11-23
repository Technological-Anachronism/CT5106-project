package bms.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RepairTicketTest {

    @Test
    void openingATicketStartsItAsOpen() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Leaking tap");

        assertEquals(RepairTicket.Status.OPEN, ticket.getStatus());
    }

    @Test
    void aBlankDescriptionIsRefused() {
        assertThrows(RepairTicket.TicketProblem.class, () -> RepairTicket.open("ticket-1", "  "));
    }

    @Test
    void anOpenTicketCannotBeClosed() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Leaking tap");

        RepairTicket.TicketProblem problem = assertThrows(RepairTicket.TicketProblem.class, ticket::close);

        assertEquals("A ticket has to be resolved before it can be closed", problem.getMessage());
        assertEquals(RepairTicket.Status.OPEN, ticket.getStatus());
    }

    @Test
    void aResolvedTicketCanBeClosed() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken chair");
        ticket.assignTo(2001L);
        ticket.resolveBy(2001L);

        ticket.close();

        assertEquals(RepairTicket.Status.CLOSED, ticket.getStatus());
    }
}
