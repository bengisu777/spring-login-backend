package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.config.SecurityConfig;
import com.bengisu.spring_login_backend.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
public class MeControllerTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    // JwtAuthFilter'ın bağımlılığı — context'in yüklenebilmesi için mock
    @MockitoBean
    private JwtService jwtService;

    // Spring Boot 4.1.0'da MockMvc-Security köprüsü otomatik kurulmuyor,
    // @WithMockUser'ın isteğe taşınabilmesi için springSecurity() ile elle
    // kuruyoruz
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // token/kimlik olmadan erişim engeli
    @Test
    void meWithoutAuthReturns401() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    // kimliği doğrulanmış kullanıcı senaryosu
    @WithMockUser(username = "test@test.com")
    @Test
    void meReturnsAuthenticatedUserEmail() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }
}