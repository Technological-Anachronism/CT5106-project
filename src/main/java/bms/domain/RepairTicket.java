package bms.domain;

// A repair ticket. This is the thing the portal is about.
// A ticket starts open. Someone else resolves it. The user can only close it after that.
// Given a ticket is still open, when the user tries to close it, then it is refused.
public class RepairTicket {

    public enum Status {
        OPEN,
        RESOLVED,
        CLOSED
    }

    public enum priority {
        LOW,
        MEDIUM,
        HIGH
    }

    private final String id;
    private final String description;
    private Status status;

    private RepairTicket(String id, String description, Status status) {
        this.id = id;
        this.description = description;
        this.status = status;
    }

    // Given a description, when the user opens a ticket, then it starts as OPEN.
    // A blank description is refused.
    public static RepairTicket open(String id, String description) {
        if (description == null || description.isBlank()) {
            throw new TicketProblem("A ticket needs a description");
        }
        return new RepairTicket(id, description.trim(), Status.OPEN);
    }

    // Rebuilds a ticket that was already saved, keeping whatever status it had.
    public static RepairTicket restore(String id, String description, Status status) {
        return new RepairTicket(id, description, status);
    }

    // Given a ticket is open, when an admin resolves it, then it becomes RESOLVED.
    // A resolved or closed ticket cannot be resolved again.
    public void resolve() {
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be resolved");
        }
        status = Status.RESOLVED;
    }

    // Given a ticket is resolved, when the user closes it, then it becomes CLOSED.
    // Given it is still open, when the user tries to close it, then nothing changes.
    public void close() {
        if (status != Status.RESOLVED) {
            throw new TicketProblem("A ticket has to be resolved before it can be closed");
        }
        status = Status.CLOSED;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public static class TicketProblem extends RuntimeException {
        public TicketProblem(String message) {
            super(message);
        }
    }
}
