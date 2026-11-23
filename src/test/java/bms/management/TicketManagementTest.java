package bms.management;

import bms.application.UserRepository;
import bms.domain.Priority;
import bms.domain.RepairTicket;
import bms.domain.Role;
import bms.domain.TicketRepository;
import bms.domain.User;
import bms.management.application.TicketManagement;
import bms.management.application.TicketManagement.ManagedTicket;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketManagementTest {

    // Stand-ins for the database, so these tests run without PostgreSQL.
    static class FakeTickets implements TicketRepository {
        private final Map<String, RepairTicket> saved = new LinkedHashMap<>();

        public List<RepairTicket> findAll() {
            return new ArrayList<>(saved.values());
        }

        public RepairTicket findById(String id) {
            RepairTicket ticket = saved.get(id);
            if (ticket == null) {
                throw new RepairTicket.TicketProblem("Ticket not found");
            }
            return ticket;
        }

        public void save(RepairTicket ticket) {
            saved.put(ticket.getId(), ticket);
        }

        public String nextId() {
            return "ticket-" + (saved.size() + 1);
        }
    }

    static class FakeUsers implements UserRepository {
        private final Map<Long, User> saved = new HashMap<>();

        public void save(User user) {
            saved.put(user.getId(), user);
        }

        public List<User> findAll() {
            return new ArrayList<>(saved.values());
        }

        public Optional<User> findById(Long id) {
            return Optional.ofNullable(saved.get(id));
        }

        public boolean existsById(Long id) {
            return saved.containsKey(id);
        }

        public boolean existsByEmail(String email) {
            return false;
        }

        public void deleteById(Long id) {
            saved.remove(id);
        }
    }

    private final FakeTickets tickets = new FakeTickets();
    private final FakeUsers users = new FakeUsers();
    private final TicketManagement management = new TicketManagement(tickets, users);

    private RepairTicket newTicket(String description) {
        RepairTicket ticket = RepairTicket.open(tickets.nextId(), description);
        tickets.save(ticket);
        return ticket;
    }

    @Test
    void staffCanBeAssigned() {
        users.save(new User(7L, "Sam", "sam@bms.ie", Role.STAFF));
        RepairTicket ticket = newTicket("Broken projector");

        management.assign(ticket.getId(), 7L);

        assertEquals(7L, tickets.findById(ticket.getId()).getAssigneeId());
    }

    @Test
    void onlyStaffCanBeAssigned() {
        users.save(new User(5L, "Pat", "pat@bms.ie", Role.USER));
        RepairTicket ticket = newTicket("Broken projector");

        RepairTicket.TicketProblem problem = assertThrows(RepairTicket.TicketProblem.class,
                () -> management.assign(ticket.getId(), 5L));

        assertEquals("Only staff can be assigned", problem.getMessage());
        assertNull(tickets.findById(ticket.getId()).getAssigneeId());
    }

    @Test
    void theQueueShowsHighPriorityFirst() {
        RepairTicket low = newTicket("Squeaky chair");
        RepairTicket high = newTicket("Server room is flooding");
        management.changePriority(low.getId(), Priority.LOW);
        management.changePriority(high.getId(), Priority.HIGH);

        List<ManagedTicket> queue = management.workQueue(null, null);

        assertEquals(high.getId(), queue.get(0).id());
        assertEquals(low.getId(), queue.get(1).id());
    }

    @Test
    void closedTicketsAreLeftOutOfTheQueue() {
        users.save(new User(7L, "Sam", "sam@bms.ie", Role.STAFF));
        RepairTicket done = newTicket("Broken projector");
        newTicket("Squeaky chair");
        management.assign(done.getId(), 7L);
        management.resolve(done.getId(), 7L);
        done.close();

        List<ManagedTicket> queue = management.workQueue(null, null);

        assertEquals(1, queue.size());
    }

    @Test
    void theQueueCanBeFilteredByAssignee() {
        users.save(new User(7L, "Sam", "sam@bms.ie", Role.STAFF));
        RepairTicket mine = newTicket("Broken projector");
        newTicket("Squeaky chair");
        management.assign(mine.getId(), 7L);

        List<ManagedTicket> queue = management.workQueue(7L, null);

        assertEquals(1, queue.size());
        assertEquals(mine.getId(), queue.get(0).id());
    }
}