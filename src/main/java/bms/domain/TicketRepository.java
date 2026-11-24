package bms.domain;

import java.util.List;

/**
 * Where tickets are stored. The domain only knows this interface, not the database.
 */
public interface TicketRepository {

    /** @return every saved ticket */
    List<RepairTicket> findAll();

    /**
     * @param id ticket id
     * @return the ticket with that id
     */
    RepairTicket findById(String id);

    /**
     * Inserts or updates a ticket.
     *
     * @param ticket ticket to store
     */
    void save(RepairTicket ticket);

    /** @return the next free ticket id */
    String nextId();
}
