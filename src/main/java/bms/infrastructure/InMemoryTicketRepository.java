package bms.infrastructure;

import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

// Fake saved tickets so the page has something to show. Wiped when the app stops.
@Repository
public class InMemoryTicketRepository implements TicketRepository {

    private final List<RepairTicket> tickets = new ArrayList<>();
    private int nextNumber = 3;

    public InMemoryTicketRepository() {
        RepairTicket flickeringScreen = RepairTicket.open("ticket-1", "flickering monitor in AnML Socs Suite");
        RepairTicket internetIssue = RepairTicket.open("ticket-2", "Internet connectivity issues in CSB-G005");
        internetIssue.resolve();

        tickets.add(flickeringScreen);
        tickets.add(internetIssue);
    }

    @Override
    public List<RepairTicket> findAll() {
        return List.copyOf(tickets);
    }

    @Override
    public RepairTicket findById(String id) {
        for (RepairTicket ticket : tickets) {
            if (ticket.getId().equals(id)) {
                return ticket;
            }
        }
        throw new RepairTicket.TicketProblem("Ticket not found");
    }

    @Override
    public void save(RepairTicket ticket) {
        tickets.add(ticket);
    }

    @Override
    public String nextId() {
        String id = "ticket-" + nextNumber;
        nextNumber = nextNumber + 1;
        return id;
    }
}
