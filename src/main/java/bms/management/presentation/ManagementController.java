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

// Urls the service management dashboard calls.
// A broken rule (TicketProblem) becomes a 400 error through TicketProblemHandler in bms.ui.
@RestController
@RequestMapping("/api/management/tickets")
public class ManagementController {

    private final TicketManagement management;

    public ManagementController(TicketManagement management) {
        this.management = management;
    }

    @GetMapping
    public List<ManagedTicket> queue(@RequestParam(required = false) Long assigneeId,
                                     @RequestParam(required = false) Priority priority) {
        return management.workQueue(assigneeId, priority);
    }

    @PostMapping("/{id}/assign")
    public ManagedTicket assign(@PathVariable String id, @RequestParam Long assigneeId) {
        return management.assign(id, assigneeId);
    }

    @PostMapping("/{id}/priority")
    public ManagedTicket changePriority(@PathVariable String id, @RequestParam Priority priority) {
        return management.changePriority(id, priority);
    }

    @PostMapping("/{id}/resolve")
    public ManagedTicket resolve(@PathVariable String id, @RequestParam Long userId) {
        return management.resolve(id, userId);
    }
}