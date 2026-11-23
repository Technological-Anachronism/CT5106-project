package bms.roles.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data access to custom roles. */
interface SpringDataManagedRoleRepository extends JpaRepository<ManagedRoleEntity, String> { }
