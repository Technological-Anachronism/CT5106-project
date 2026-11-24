package bms.ui;

import bms.application.TicketActions;
import bms.application.TicketActions.TicketView;
import bms.domain.RepairTicket;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Urls the portal tab calls.
 * List, open and close only. Resolve is on the management API instead.
 */
@RestController
@RequestMapping("/api/portal/tickets")
public class PortalController {

    private final TicketActions actions;

    /**
     * @param actions portal use cases
     */
    public PortalController(TicketActions actions) {
        this.actions = actions;
    }

    /**
     * @return all tickets
     */
    @GetMapping
    public List<TicketView> list() {
        return actions.list();
    }

    /**
     * Opens a ticket from the description the user typed.
     *
     * @param description what needs fixing
     * @return the new ticket
     */
    @PostMapping
    public TicketView open(@RequestParam String description) {
        return actions.open(description);
    }

    /**
     * Closes a resolved ticket.
     *
     * @param id ticket id
     * @return the updated ticket
     */
    @PostMapping("/{id}/close")
    public TicketView close(@PathVariable String id) {
        return actions.close(id);
    }
}

/**
 * Turns "has to be resolved first" into a normal error message for the browser.
 */
@RestControllerAdvice
class TicketProblemHandler {

    /**
     * @param problem the rule that was broken
     * @return HTTP 400 with {@code {"error": "..."}}
     */
    @ExceptionHandler(RepairTicket.TicketProblem.class)
    public ResponseEntity<Map<String, String>> handle(RepairTicket.TicketProblem problem) {
        return ResponseEntity.badRequest().body(Map.of("error", problem.getMessage()));
    }
}
