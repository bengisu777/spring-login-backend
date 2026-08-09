package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.model.User;
import com.bengisu.spring_login_backend.repository.UserRepository;
import com.bengisu.spring_login_backend.security.JwtService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

@WebMvcTest(AuthController.class)
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    // email veya şifre eksik kontrolü
    @Test
    void registerWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(
                post("/api/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isBadRequest());
    }

    // email zaten kayıtlıysa 409 döner
    @Test
    void registerWithExistingEmailReturns409() throws Exception {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        mockMvc.perform(post("/api/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"123456\"}")).andExpect(status().isConflict());
    }

    // başarılı kayıt senaryosu
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

    // email veya şifre eksik kontrolü
    @Test
    void loginWithMissingFieldsReturns400() throws Exception {
        mockMvc.perform(
                post("/api/login").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"test@test.com\"}"))
                .andExpect(status().isBadRequest());
    }

    // kayıtlı olmayan kullanıcı senaryosu
    @Test
    void loginWithUnknownEmailReturns401() throws Exception {
        when(userRepository.findByEmail("yok@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"yok@test.com\",\"password\":\"123456\"}")).andExpect(status().isUnauthorized());
    }

    // yanlış şifre (gerçek bcrypt ile)
    @Test
    void loginWithWrongPasswordReturns401() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User existingUser = new User();
        existingUser.setEmail("test@test.com");
        existingUser.setPasswordHash(encoder.encode("dogruSifre123"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(existingUser));

        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"yanlisSifre\"}"))
                .andExpect(status().isUnauthorized());
    }

    // başarılı giriş (gerçek bcrypt, sahte JWT)
    @Test
    void loginWithCorrectCredentialsReturnsToken() throws Exception {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("test@test.com");

        existingUser.setPasswordHash(encoder.encode("dogruSifre123"));

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(anyLong(), anyString())).thenReturn("sahte-token");

        mockMvc.perform(post("/api/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"test@test.com\",\"password\":\"dogruSifre123\"}")).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("sahte-token"));

    }

}