package bms.domain;

/**
 * A repair ticket. This is the thing the portal is about.
 * A ticket starts open. Staff resolve it. The user can only close it after that.
 * Given a ticket is still open, when the user tries to close it, then it is refused.
 */
public class RepairTicket {

    /** Where the ticket is in its life: OPEN, then RESOLVED, then CLOSED. */
    public enum Status {
        OPEN,
        RESOLVED,
        CLOSED
    }

    private final String id;
    private final String description;
    private Status status;
    private Priority priority;
    /** Empty until someone is assigned. */
    private Long assigneeId;

    private RepairTicket(String id, String description, Status status, Priority priority, Long assigneeId) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.assigneeId = assigneeId;
    }

    /**
     * Given a description, when the user opens a ticket, then it starts as OPEN.
     * A blank description is refused.
     *
     * @param id ticket id
     * @param description what needs fixing
     * @return a new OPEN ticket on MEDIUM with nobody assigned
     */
    public static RepairTicket open(String id, String description) {
        if (description == null || description.isBlank()) {
            throw new TicketProblem("A ticket needs a description");
        }
        return new RepairTicket(id, description.trim(), Status.OPEN, Priority.MEDIUM, null);
    }

    /**
     * Rebuilds a ticket that was already saved, keeping whatever status it had.
     * Tickets saved before priorities existed come back as MEDIUM.
     *
     * @param id ticket id
     * @param description what needs fixing
     * @param status the saved status
     * @param priority the saved priority, or null for MEDIUM
     * @param assigneeId the assigned user's id, or null if unassigned
     * @return the rebuilt ticket
     */
    public static RepairTicket restore(String id, String description, Status status, Priority priority, Long assigneeId) {
        Priority savedPriority = priority == null ? Priority.MEDIUM : priority;
        return new RepairTicket(id, description, status, savedPriority, assigneeId);
    }

    /**
     * Private so the only way to resolve a ticket is {@link #resolveBy(Long)}, which checks the assignee.
     * A resolved or closed ticket cannot be resolved again.
     */
    private void resolve() {
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be resolved");
        }
        status = Status.RESOLVED;
    }

    /**
     * Given a ticket is resolved, when the user closes it, then it becomes CLOSED.
     * Given it is still open, when the user tries to close it, then nothing changes.
     */
    public void close() {
        if (status != Status.RESOLVED) {
            throw new TicketProblem("A ticket has to be resolved before it can be closed");
        }
        status = Status.CLOSED;
    }

    /**
     * Given an open ticket, when staff assign it, then it records the assignee.
     * A resolved or closed ticket cannot be assigned.
     *
     * @param userId the staff member taking the ticket
     * @throws TicketProblem if the ticket is not open or no user is given
     */
    public void assignTo(Long userId) {
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be assigned");
        }
        if (userId == null) {
            throw new TicketProblem("Choose someone to assign the ticket to");
        }
        assigneeId = userId;
    }

    /**
     * Given a ticket is assigned, when its assignee resolves it, then it becomes RESOLVED.
     * Given it is unassigned, or someone else tries to resolve it, then it is refused and stays OPEN.
     *
     * @param userId the person trying to resolve it
     * @throws TicketProblem if it is unassigned, the wrong person, or not open
     */
    public void resolveBy(Long userId) {
        if (assigneeId == null) {
            throw new TicketProblem("A ticket must be assigned before it can be resolved");
        }
        if (!assigneeId.equals(userId)) {
            throw new TicketProblem("Only the assignee can resolve this ticket");
        }
        resolve();
    }

    /**
     * Given a ticket is open, when the priority is changed, then the new priority is kept.
     * A resolved or closed ticket's priority cannot change.
     *
     * @param newPriority HIGH, MEDIUM or LOW
     * @throws TicketProblem if no priority is given or the ticket is not open
     */
    public void changePriority(Priority newPriority) {
        if (newPriority == null) {
            throw new TicketProblem("A ticket needs a new priority");
        }
        if (status != Status.OPEN) {
            throw new TicketProblem("Only an open ticket can be changed");
        }
        priority = newPriority;
    }

    /** @return ticket id */
    public String getId() {
        return id;
    }

    /** @return what needs fixing */
    public String getDescription() {
        return description;
    }

    /** @return OPEN, RESOLVED or CLOSED */
    public Status getStatus() {
        return status;
    }

    /** @return how urgent the ticket is */
    public Priority getPriority() {
        return priority;
    }

    /** @return the assigned user's id, or null if nobody is assigned */
    public Long getAssigneeId() {
        return assigneeId;
    }

    /**
     * Thrown when a ticket rule is broken, e.g. closing before it is resolved.
     */
    public static class TicketProblem extends RuntimeException {
        /**
         * @param message plain English reason the rule failed
         */
        public TicketProblem(String message) {
            super(message);
        }
    }
}
