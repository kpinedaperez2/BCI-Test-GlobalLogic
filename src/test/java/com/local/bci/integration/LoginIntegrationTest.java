package com.local.bci.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.local.bci.infrastructure.persistence.entity.UserEntity;
import com.local.bci.infrastructure.persistence.jpa.UserJpaRepository;
import com.local.bci.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LoginIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtUtil;

    @Autowired
    private UserJpaRepository userRepository;

    private String validToken;

    @BeforeEach
    void setUp() {

        userRepository.deleteAll();

        validToken = "Bearer " + jwtUtil.generateToken("kevin@example.com");

        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("kevin@example.com");
        user.setName("Kevin Test");
        user.setPassword("encrypted");
        user.setToken(validToken.replace("Bearer ", ""));
        user.setIsActive(true);

        userRepository.save(user);
    }

    @Test
    void login_WithValidToken_Returns200() throws Exception {
        mockMvc.perform(post("/login")
                        .header(HttpHeaders.AUTHORIZATION, validToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void login_WithoutAuthorizationHeader_Returns500() throws Exception {
        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error[0].codigo", is(500)));
    }

    @Test
    void login_WithMalformedToken_Returns400() throws Exception {
        String badToken = "Bearer this.is.not.valid";

        mockMvc.perform(post("/login")
                        .header(HttpHeaders.AUTHORIZATION, badToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error[0].codigo", is(400)));
    }
}
