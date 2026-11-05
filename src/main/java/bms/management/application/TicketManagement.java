package bms.management.application;

import bms.application.UserRepository;
import bms.domain.Priority;
import bms.domain.RepairTicket;
import bms.domain.Role;
import bms.domain.TicketRepository;
import bms.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// What an admin can do on the service management dashboard.
@Service
public class TicketManagement {

    private final TicketRepository tickets;
    private final UserRepository users;

    public TicketManagement(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    // Outstanding tickets (not CLOSED), HIGH priority first.
    // assigneeId and priority are optional filters; null means "any".
    public List<ManagedTicket> workQueue(Long assigneeId, Priority priority) {
        List<ManagedTicket> queue = new ArrayList<>();
        for (RepairTicket ticket : tickets.findAll()) {
            boolean outstanding = ticket.getStatus() != RepairTicket.Status.CLOSED;
            boolean assigneeMatches = assigneeId == null || assigneeId.equals(ticket.getAssigneeId());
            boolean priorityMatches = priority == null || priority == ticket.getPriority();
            if (outstanding && assigneeMatches && priorityMatches) {
                queue.add(ManagedTicket.from(ticket));
            }
        }
        queue.sort(Comparator.comparing(ManagedTicket::priority));
        return queue;
    }

    // The "must be staff" rule needs the user store, so it lives here rather than in RepairTicket.
    @Transactional
    public ManagedTicket assign(String ticketId, Long assigneeId) {
        User user = users.findById(assigneeId)
                .orElseThrow(() -> new RepairTicket.TicketProblem("No user with id " + assigneeId));
        if (user.getRole() != Role.STAFF) {
            throw new RepairTicket.TicketProblem("Only staff can be assigned");
        }
        RepairTicket ticket = tickets.findById(ticketId);
        ticket.assignTo(assigneeId);
        tickets.save(ticket);
        return ManagedTicket.from(ticket);
    }

    @Transactional
    public ManagedTicket changePriority(String ticketId, Priority priority) {
        RepairTicket ticket = tickets.findById(ticketId);
        ticket.changePriority(priority);
        tickets.save(ticket);
        return ManagedTicket.from(ticket);
    }

    @Transactional
    public ManagedTicket resolve(String ticketId, Long userId) {
        RepairTicket ticket = tickets.findById(ticketId);
        ticket.resolveBy(userId);
        tickets.save(ticket);
        return ManagedTicket.from(ticket);
    }

    // The fields the dashboard needs. Not the whole ticket object.
    public record ManagedTicket(String id, String description, String status, Priority priority, Long assigneeId) {
        static ManagedTicket from(RepairTicket ticket) {
            return new ManagedTicket(ticket.getId(), ticket.getDescription(), ticket.getStatus().name(),
                    ticket.getPriority(), ticket.getAssigneeId());
        }
    }
}