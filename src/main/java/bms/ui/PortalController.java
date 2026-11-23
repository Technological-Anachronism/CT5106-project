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

    public PortalController(TicketActions actions) {
        this.actions = actions;
    }

    @GetMapping
    public List<TicketView> list() {
        return actions.list();
    }

    @PostMapping
    public TicketView open(@RequestParam String description) {
        return actions.open(description);
    }

    @PostMapping("/{id}/close")
    public TicketView close(@PathVariable String id) {
        return actions.close(id);
    }
}

// Turns "has to be resolved first" into a normal error message.
@RestControllerAdvice
class TicketProblemHandler {

    @ExceptionHandler(RepairTicket.TicketProblem.class)
    public ResponseEntity<Map<String, String>> handle(RepairTicket.TicketProblem problem) {
        return ResponseEntity.badRequest().body(Map.of("error", problem.getMessage()));
    }
}
