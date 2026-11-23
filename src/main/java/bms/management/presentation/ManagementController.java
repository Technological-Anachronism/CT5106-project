package bms.management.presentation;

import bms.domain.Priority;
import bms.management.application.TicketManagement;
import bms.management.application.TicketManagement.ManagedTicket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * URLs the service management dashboard calls.
 * A broken rule (TicketProblem) becomes a 400 error through TicketProblemHandler in bms.ui.
 */
@RestController
@RequestMapping("/api/management/tickets")
public class ManagementController {

    private final TicketManagement management;

    public ManagementController(TicketManagement management) {
        this.management = management;
    }

    /**
     * GET /api/management/tickets. The work queue, optionally filtered.
     *
     * @param assigneeId optional assignee filter
     * @param priority optional priority filter
     * @return outstanding tickets, HIGH first
     */
    @GetMapping
    public List<ManagedTicket> queue(@RequestParam(required = false) Long assigneeId,
                                     @RequestParam(required = false) Priority priority) {
        return management.workQueue(assigneeId, priority);
    }

    /**
     * POST /api/management/tickets/{id}/assign?assigneeId=
     *
     * @param id the ticket id
     * @param assigneeId the staff member to assign
     * @return the updated ticket
     */
    @PostMapping("/{id}/assign")
    public ManagedTicket assign(@PathVariable String id, @RequestParam Long assigneeId) {
        return management.assign(id, assigneeId);
    }

    /**
     * POST /api/management/tickets/{id}/priority?priority=
     *
     * @param id the ticket id
     * @param priority HIGH, MEDIUM or LOW
     * @return the updated ticket
     */
    @PostMapping("/{id}/priority")
    public ManagedTicket changePriority(@PathVariable String id, @RequestParam Priority priority) {
        return management.changePriority(id, priority);
    }

    /**
     * POST /api/management/tickets/{id}/resolve?userId=
     *
     * @param id the ticket id
     * @param userId the user resolving it, who must be the assignee
     * @return the updated ticket
     */
    @PostMapping("/{id}/resolve")
    public ManagedTicket resolve(@PathVariable String id, @RequestParam Long userId) {
        return management.resolve(id, userId);
    }
}