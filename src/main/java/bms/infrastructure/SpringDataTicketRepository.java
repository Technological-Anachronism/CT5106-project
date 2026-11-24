package bms.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data writes the SQL for this.
 */
interface SpringDataTicketRepository extends JpaRepository<TicketEntity, String> {
}
