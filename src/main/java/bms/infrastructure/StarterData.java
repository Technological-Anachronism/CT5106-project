package bms.infrastructure;

import bms.application.UserRepository;
import bms.domain.RepairTicket;
import bms.domain.Role;
import bms.domain.TicketRepository;
import bms.domain.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// Adds the example tickets and admin user the first time the database is empty.
@Component
public class StarterData implements CommandLineRunner {

    private final TicketRepository tickets;
    private final UserRepository users;

    public StarterData(TicketRepository tickets, UserRepository users) {
        this.tickets = tickets;
        this.users = users;
    }

    @Override
    public void run(String... args) {
        if (tickets.findAll().isEmpty()) {
            RepairTicket flickeringScreen = RepairTicket.open(tickets.nextId(), "flickering monitor in AnML Socs Suite");
            tickets.save(flickeringScreen);

            RepairTicket internetIssue = RepairTicket.open(tickets.nextId(), "Internet connectivity issues in CSB-G005");
            internetIssue.resolve();
            tickets.save(internetIssue);
        }

        if (!users.existsById(1234L)) {
            users.save(new User(1234L, "admin", "admin@admin.ie", Role.SUPER_ADMIN));
        }

        // Two staff members so tickets can be assigned in a demo.
        if (!users.existsById(2001L)) {
            users.save(new User(2001L, "Sam Staff", "sam@bms.ie", Role.STAFF));
        }
        if (!users.existsById(2002L)) {
            users.save(new User(2002L, "Alex Staff", "alex@bms.ie", Role.STAFF));
        }
    }
}
