package bms.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketAssignmentTest {

    @Test
    void aNewTicketStartsAtMediumPriorityWithNoAssignee() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");

        assertEquals(Priority.MEDIUM, ticket.getPriority());
        assertNull(ticket.getAssigneeId());
    }

    @Test
    void anOpenTicketCanBeAssigned() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");

        ticket.assignTo(7L);

        assertEquals(7L, ticket.getAssigneeId());
    }

    @Test
    void aResolvedTicketCannotBeAssigned() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");
        ticket.assignTo(7L);
        ticket.resolveBy(7L);

        RepairTicket.TicketProblem problem = assertThrows(RepairTicket.TicketProblem.class, () -> ticket.assignTo(8L));

        assertEquals("Only an open ticket can be assigned", problem.getMessage());
        assertEquals(7L, ticket.getAssigneeId());
    }

    @Test
    void anUnassignedTicketCannotBeResolved() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");

        assertThrows(RepairTicket.TicketProblem.class, () -> ticket.resolveBy(7L));

        assertEquals(RepairTicket.Status.OPEN, ticket.getStatus());
    }

    @Test
    void onlyTheAssigneeCanResolve() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");
        ticket.assignTo(7L);

        RepairTicket.TicketProblem problem = assertThrows(RepairTicket.TicketProblem.class, () -> ticket.resolveBy(8L));

        assertEquals("Only the assignee can resolve this ticket", problem.getMessage());
        assertEquals(RepairTicket.Status.OPEN, ticket.getStatus());
    }

    @Test
    void theAssigneeCanResolve() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");
        ticket.assignTo(7L);

        ticket.resolveBy(7L);

        assertEquals(RepairTicket.Status.RESOLVED, ticket.getStatus());
    }

    @Test
    void aClosedTicketsPriorityCannotChange() {
        RepairTicket ticket = RepairTicket.open("ticket-1", "Broken projector");
        ticket.assignTo(7L);
        ticket.resolveBy(7L);
        ticket.close();

        assertThrows(RepairTicket.TicketProblem.class, () -> ticket.changePriority(Priority.HIGH));

        assertEquals(Priority.MEDIUM, ticket.getPriority());
    }
}