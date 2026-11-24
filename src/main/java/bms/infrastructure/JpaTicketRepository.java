package bms.infrastructure;

import bms.domain.RepairTicket;
import bms.domain.TicketRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter: implements the domain TicketRepository, maps to TicketEntity and hands the work to Spring Data.
 */
@Repository
public class JpaTicketRepository implements TicketRepository {

    private final SpringDataTicketRepository springData;

    /**
     * @param springData Spring Data helper that talks to PostgreSQL
     */
    public JpaTicketRepository(SpringDataTicketRepository springData) {
        this.springData = springData;
    }

    /**
     * @return every ticket, ordered by id
     */
    @Override
    public List<RepairTicket> findAll() {
        List<RepairTicket> tickets = new ArrayList<>();
        for (TicketEntity entity : springData.findAll(Sort.by("id"))) {
            tickets.add(entity.toTicket());
        }
        return tickets;
    }

    /**
     * @param id ticket id
     * @return the ticket
     * @throws RepairTicket.TicketProblem if it is not in the database
     */
    @Override
    public RepairTicket findById(String id) {
        return springData.findById(id)
                .map(TicketEntity::toTicket)
                .orElseThrow(() -> new RepairTicket.TicketProblem("Ticket not found"));
    }

    /**
     * Inserts or updates the ticket row.
     *
     * @param ticket domain ticket to save
     */
    @Override
    public void save(RepairTicket ticket) {
        springData.save(TicketEntity.from(ticket));
    }

    /**
     * Tickets are never deleted, so the count gives the next free number.
     *
     * @return e.g. ticket-3
     */
    @Override
    public String nextId() {
        return "ticket-" + (springData.count() + 1);
    }
}
