package com.example.proyectobackend.controller;

import com.example.proyectobackend.dto.MetricasGlobalesDto;
import com.example.proyectobackend.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminService adminService;

    @Test
    @DisplayName("Usuario no autenticado recibe 401 o 403")
    void testAccesoSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metricas-globales"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("Usuario con ROLE_USER recibe 403 Forbidden en endpoints de admin")
    void testAccesoRolUserForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/metricas-globales").with(user("morty@c137.com").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Usuario con ROLE_ADMIN accede exitosamente (200 OK)")
    void testAccesoRolAdminOk() throws Exception {
        when(adminService.obtenerMetricasGlobales()).thenReturn(MetricasGlobalesDto.builder()
                .totalUsuarios(10)
                .totalRecaidasRegistradas(3)
                .build());

        mockMvc.perform(get("/api/v1/admin/metricas-globales").with(user("rickprime@omega.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
