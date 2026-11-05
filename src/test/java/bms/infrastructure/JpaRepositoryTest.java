package bms.infrastructure;

import bms.domain.Priority;
import bms.domain.RepairTicket;
import bms.domain.Role;
import bms.domain.User;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Runs against the bms_test PostgreSQL database. Each test is rolled back afterwards.
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaTicketRepository.class, JpaUserRepository.class})
class JpaRepositoryTest {

    @Autowired
    JpaTicketRepository tickets;

    @Autowired
    JpaUserRepository users;

    @Autowired
    TestEntityManager entityManager;

    @Test
    void savedTicketKeepsItsStatusWhenLoadedAgain() {
        RepairTicket ticket = RepairTicket.open(tickets.nextId(), "Broken projector");
        tickets.save(ticket);

        RepairTicket loaded = tickets.findById(ticket.getId());
        loaded.resolve();
        tickets.save(loaded);

        assertEquals(RepairTicket.Status.RESOLVED, tickets.findById(ticket.getId()).getStatus());
        assertEquals("ticket-2", tickets.nextId());
    }

    @Test
    void missingTicketIsRefused() {
        assertThrows(RepairTicket.TicketProblem.class, () -> tickets.findById("ticket-99"));
    }

    @Test
    void blankDescriptionIsStoppedBeforeSaving() {
        RepairTicket bad = RepairTicket.restore("ticket-50", " ", RepairTicket.Status.OPEN, Priority.MEDIUM, null);
        assertThrows(ConstraintViolationException.class, () -> {
            tickets.save(bad);
            entityManager.flush();
        });
    }

    @Test
    void emailCheckIgnoresCase() {
        users.save(new User(1L, "Test", "Test@Uni.ie", Role.USER));
        assertTrue(users.existsByEmail("test@uni.ie"));
    }
}
