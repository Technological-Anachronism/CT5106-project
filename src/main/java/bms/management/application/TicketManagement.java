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

/**
 * What an admin can do on the service management dashboard.
 * Each write loads the ticket, calls a RepairTicket method that checks the rules, then saves it.
 */
@Service
public class TicketManagement {

    private final TicketRepository tickets;
    private final UserRepository users;

    public TicketManagement(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    /**
     * Outstanding tickets (not CLOSED), HIGH priority first.
     *
     * @param assigneeId only show this user's tickets, or null for anyone
     * @param priority only show this priority, or null for any
     * @return the matching tickets
     */
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

    /**
     * Assigns a ticket to a staff member.
     * The "must be staff" rule needs the user store, so it lives here rather than in RepairTicket.
     *
     * @param ticketId the ticket to assign
     * @param assigneeId the user to assign it to
     * @return the updated ticket
     * @throws RepairTicket.TicketProblem if the user doesn't exist, isn't STAFF, or the ticket isn't open
     */
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

    /**
     * Changes an open ticket's priority.
     *
     * @param ticketId the ticket to change
     * @param priority the new priority
     * @return the updated ticket
     * @throws RepairTicket.TicketProblem if the ticket isn't open
     */
    @Transactional
    public ManagedTicket changePriority(String ticketId, Priority priority) {
        RepairTicket ticket = tickets.findById(ticketId);
        ticket.changePriority(priority);
        tickets.save(ticket);
        return ManagedTicket.from(ticket);
    }

    /**
     * Resolves a ticket on behalf of a user. Only the assignee is allowed.
     *
     * @param ticketId the ticket to resolve
     * @param userId the user doing it
     * @return the updated ticket
     * @throws RepairTicket.TicketProblem if the user isn't the assignee or the ticket isn't open
     */
    @Transactional
    public ManagedTicket resolve(String ticketId, Long userId) {
        RepairTicket ticket = tickets.findById(ticketId);
        ticket.resolveBy(userId);
        tickets.save(ticket);
        return ManagedTicket.from(ticket);
    }

    /**
     * The fields the dashboard needs. Not the whole ticket object.
     *
     * @param id the ticket id
     * @param description what needs fixing
     * @param status OPEN, RESOLVED or CLOSED
     * @param priority how urgent it is
     * @param assigneeId the assigned user's id, or null
     */
    public record ManagedTicket(String id, String description, String status, Priority priority, Long assigneeId) {
        static ManagedTicket from(RepairTicket ticket) {
            return new ManagedTicket(ticket.getId(), ticket.getDescription(), ticket.getStatus().name(),
                    ticket.getPriority(), ticket.getAssigneeId());
        }
    }
}