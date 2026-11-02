package bms.infrastructure;

import bms.domain.RepairTicket;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// One row in the tickets table. Kept separate so RepairTicket has no database code in it.
// The checks here are a backup; the real rules are in RepairTicket.
@Entity
@Table(name = "tickets")
public class TicketEntity {

    @Id
    private String id;

    @NotBlank
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    private RepairTicket.Status status;

    protected TicketEntity() {
    }

    static TicketEntity from(RepairTicket ticket) {
        TicketEntity entity = new TicketEntity();
        entity.id = ticket.getId();
        entity.description = ticket.getDescription();
        entity.status = ticket.getStatus();
        return entity;
    }

    RepairTicket toTicket() {
        return RepairTicket.restore(id, description, status);
    }
}
