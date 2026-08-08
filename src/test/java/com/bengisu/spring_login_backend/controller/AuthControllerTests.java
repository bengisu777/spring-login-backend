package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.model.User;
import com.bengisu.spring_login_backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void registerWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(
                post("/api/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerWithExistingEmailReturns409() throws Exception {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"123456\"}")).andExpect(status().isConflict());
    }

    @Test
    void registerWithValidDataReturns201() throws Exception {
        when(userRepository.findByEmail("yeni@test.com")).thenReturn(Optional.empty());

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("yeni@test.com");

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"yeni@test.com\",\"password\":\"123456\"}")).andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("yeni@test.com"));
    }

}