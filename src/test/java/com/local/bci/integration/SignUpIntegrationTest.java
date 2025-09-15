package com.local.bci.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.bci.application.dto.SignUpRequestDTO;
import com.local.bci.application.dto.UserResponseDTO;
import com.local.bci.application.usecase.SingUpUseCase;
import com.local.bci.infrastructure.persistence.jpa.UserJpaRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)

class SignUpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserJpaRepository userRepository;

    @MockBean
    private SingUpUseCase signUpUseCase;

    @Test
    void signUp_Success() throws Exception {
        userRepository.deleteAll();
        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setName("Kevin");
        request.setEmail("kevin@example.com");
        request.setPassword("Abcdef12");

        UserResponseDTO response = new UserResponseDTO();
        response.setId(UUID.randomUUID());
        response.setName("Kevin");
        response.setEmail("kevin@example.com");

        Mockito.when(signUpUseCase.apply(any())).thenReturn(response);

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Kevin"))
                .andExpect(jsonPath("$.email").value("kevin@example.com"));
    }

    @Test
    void signUp_EmailAlreadyExists() throws Exception {
        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setName("Kevin");
        request.setEmail("kevin@example.com");
        request.setPassword("Abcdef12");

        Mockito.when(signUpUseCase.apply(any()))
                .thenThrow(new IllegalStateException("User already exists"));

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("User already exists")));
    }

    @Test
    void signUp_InvalidPassword() throws Exception {
        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setName("Kevin");
        request.setEmail("kevin@example.com");
        request.setPassword("abc");

        Mockito.when(signUpUseCase.apply(any()))
                .thenThrow(new IllegalArgumentException("Invalid password format"));

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid password format")));
    }

    @Test
    void signUp_MissingEmail() throws Exception {
        SignUpRequestDTO request = new SignUpRequestDTO();
        request.setName("Kevin");
        request.setPassword("Abcdef12");

        mockMvc.perform(post("/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}