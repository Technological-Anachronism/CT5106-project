package bms.application;

import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// The three things a user can do on the portal.
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

    public TicketView open(String description) {
        RepairTicket ticket = RepairTicket.open(tickets.nextId(), description);
        tickets.save(ticket);
        return TicketView.from(ticket);
    }

    public TicketView resolve(String id) {
        RepairTicket ticket = tickets.findById(id);
        ticket.resolve();
        return TicketView.from(ticket);
    }

    public TicketView close(String id) {
        RepairTicket ticket = tickets.findById(id);
        ticket.close();
        return TicketView.from(ticket);
    }

    // The fields the page needs. Not the whole ticket object.
    public record TicketView(String id, String description, String status) {
        static TicketView from(RepairTicket ticket) {
            return new TicketView(ticket.getId(), ticket.getDescription(), ticket.getStatus().name());
        }
    }
}
