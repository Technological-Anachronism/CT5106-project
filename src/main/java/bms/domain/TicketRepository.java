package bms.domain;

import java.util.List;

/**
 * Where tickets are stored. The domain only knows this interface, not the database.
 */
public interface TicketRepository {

    List<RepairTicket> findAll();

    RepairTicket findById(String id);

    void save(RepairTicket ticket);

    String nextId();
}
