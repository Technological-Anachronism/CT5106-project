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

    private final String id;
    private final String description;
    private Status status;
    private Priority priority;
    private Long assigneeId;
    // empty unitl someone is assigned
    //rn its just a placeholder because we still havent fully decided on a domain to centre arounf, could be renamed to technicianID


    private RepairTicket(String id, String description, Status status, Priority priority, Long assigneeId) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
    }

    // Given a description, when the user opens a ticket, then it starts as OPEN.
    // A blank description is refused.
    public static RepairTicket open(String id, String description) {
        if (description == null || description.isBlank()) {
            throw new TicketProblem("A ticket needs a description");
        }
        return new RepairTicket(id, description.trim(), Status.OPEN, Priority.MEDIUM, null);
    }


    // Rebuilds a ticket that was already saved, keeping whatever status it had.
    //makes it so tcikets made before Priorities  existed are automtically assigned MEDIUM
    public static RepairTicket restore(String id, String description, Status status, Priority priority, Long assigneeId) {
        Priority savedPriority = priority == null ? Priority.MEDIUM : priority;
        return new RepairTicket(id, description, status, savedPriority, assigneeId);
    }

    // Private so the only way to resolve a ticket is resolveBy, which checks the assignee.
    // A resolved or closed ticket cannot be resolved again.
    private void resolve() {
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

    // Given an open ticket, when the admin assigns it, then it records the assignee.
    // A resolved or closed ticket cant be assigned
    public void assignTo(Long userId) {
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be assigned");
        }
        if (userId == null){
            throw new TicketProblem("Choose someone to assign the ticket to");
        }
        assigneeId = userId;
    }

    //Given a ticket is assigned, when its assignee resolves it, then it becomes RESOLVED
    // given it is unassigned, or someone else tries to resolve it, then its refused and stays OPEN
    public void resolveBy( Long userId) {
        if (assigneeId == null){
            throw new TicketProblem("A ticket must be assigned before it can be resolved");
        }
        if (!assigneeId.equals(userId)) {
            throw new TicketProblem("Only the assignee can resolve this ticket");
        }
        resolve();
    }

    //Given a ticket is not closed, when the admin changes its priority, then the new priority is kept
    public void changePriority(Priority newPriority) {
        if (newPriority == null) {
            throw new TicketProblem("A ticket needs a new priority");
        }
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be changed");
        }

        priority = newPriority;
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

    public Priority getPriority() {
        return priority;
    }

    public Long getAssigneeId() {
        return assigneeId;
    }

    public static class TicketProblem extends RuntimeException {
        public TicketProblem(String message) {
            super(message);
        }
    }
}
