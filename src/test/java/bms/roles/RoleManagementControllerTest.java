package bms.roles;

import bms.domain.Role;
import bms.roles.application.RoleManagementService;
import bms.roles.domain.ManagedRole;
import bms.roles.domain.RoleAssignment;
import bms.roles.presentation.RoleManagementController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.NoSuchElementException;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Web MVC tests for the custom-role API. */
@WebMvcTest(RoleManagementController.class)
class RoleManagementControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean RoleManagementService roles;

    @Test
    void listsRoles() throws Exception {
        when(roles.getRoles()).thenReturn(List.of(new ManagedRole("HELPER", "Helper", Role.USER)));
        mvc.perform(get("/api/roles")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("HELPER"));
    }

    @Test
    void createsRole() throws Exception {
        when(roles.createRole(any(ManagedRole.class))).thenReturn(new ManagedRole("HELPER", "Helper", Role.USER));
        mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"HELPER\",\"name\":\"Helper\",\"baseRole\":\"USER\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.code").value("HELPER"));
    }

    @Test
    void updatesRole() throws Exception {
        when(roles.updateRole(eq("HELPER"), any(ManagedRole.class)))
                .thenReturn(new ManagedRole("HELPER", "Updated", Role.USER));
        mvc.perform(put("/api/roles/HELPER").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"HELPER\",\"name\":\"Updated\",\"baseRole\":\"USER\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Updated"));
    }

    @Test
    void deletesRole() throws Exception {
        mvc.perform(delete("/api/roles/HELPER")).andExpect(status().isNoContent());
        verify(roles).deleteRole("HELPER");
    }

    @Test
    void assignsRole() throws Exception {
        when(roles.assignRole(1L, "HELPER")).thenReturn(new RoleAssignment(1L, "HELPER"));
        mvc.perform(put("/api/roles/assignments/1").contentType(MediaType.APPLICATION_JSON)
                .content("{\"roleCode\":\"HELPER\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.roleCode").value("HELPER"));
    }

    @Test
    void invalidRoleReturns400() throws Exception {
        when(roles.createRole(any(ManagedRole.class))).thenThrow(new IllegalArgumentException("Duplicate role."));
        mvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"HELPER\",\"name\":\"Helper\",\"baseRole\":\"USER\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.error").value("Duplicate role."));
    }

    @Test
    void missingRoleReturns404() throws Exception {
        when(roles.getRole("MISSING")).thenThrow(new NoSuchElementException("Role not found."));
        mvc.perform(get("/api/roles/MISSING")).andExpect(status().isNotFound());
    }
}
