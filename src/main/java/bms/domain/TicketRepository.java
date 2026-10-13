package bms.domain;

import java.util.List;

// Where tickets are stored. A list for now, a database later.
public interface TicketRepository {

    List<RepairTicket> findAll();

    RepairTicket findById(String id);

    void save(RepairTicket ticket);

    String nextId();
}
