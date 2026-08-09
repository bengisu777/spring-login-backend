package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.security.JwtService;
import com.bengisu.spring_login_backend.config.SecurityConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeController.class)
@Import(SecurityConfig.class)
public class MeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    // JwtAuthFilter, @WebMvcTest tarafından otomatik yüklendiği için
    // onun bağımlılığı olan JwtService'i de mock'lamamız gerekiyor
    @MockitoBean
    private JwtService jwtService;

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
