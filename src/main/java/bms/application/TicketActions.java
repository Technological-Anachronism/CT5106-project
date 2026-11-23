package bms.application;

import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * The things a user can do on the portal: list tickets, open one, and close one once it is resolved.
 * Resolve is not here on purpose. That stays in Service Management so only the assignee can do it.
 */
@Service
public class TicketActions {

    private final TicketRepository tickets;

    public TicketActions(TicketRepository tickets) {
        this.tickets = tickets;
    }

    public List<TicketView> list() {
        List<TicketView> views = new ArrayList<>();
        for (RepairTicket ticket : tickets.findAll()) {
            views.add(TicketView.from(ticket));
        }
        return views;
    }

    // Picking the next id and saving happen together, or not at all.
    @Transactional
    public TicketView open(String description) {
        RepairTicket ticket = RepairTicket.open(tickets.nextId(), description);
        tickets.save(ticket);
        return TicketView.from(ticket);
    }

    @Transactional
    public TicketView close(String id) {
        RepairTicket ticket = tickets.findById(id);
        ticket.close();
        tickets.save(ticket);
        return TicketView.from(ticket);
    }

    // The fields the page needs. Not the whole ticket object.
    public record TicketView(String id, String description, String status) {
        static TicketView from(RepairTicket ticket) {
            return new TicketView(ticket.getId(), ticket.getDescription(), ticket.getStatus().name());
        }
    }
}
