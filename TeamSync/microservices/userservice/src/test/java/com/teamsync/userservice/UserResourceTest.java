package com.teamsync.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamsync.userservice.model.User;
import com.teamsync.userservice.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("test")
class UserResourceTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnValidationErrorForEmptyUsername() throws Exception {
        User invalidUser = new User("", "password", UserRole.STUDENT);
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Username and password are required"));
    }

    @Test
    void shouldReturnValidationErrorForEmptyPassword() throws Exception {
        User invalidUser = new User("username", "", UserRole.STUDENT);
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Username and password are required"));
    }

    @Test
    void shouldReturnValidationErrorForNullCredentials() throws Exception {
        User invalidUser = new User(null, null, UserRole.STUDENT);
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(put("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Username and password are required"));
    }

    @Test
    void shouldReturnValidationErrorForLoginWithEmptyCredentials() throws Exception {
        User invalidUser = new User("", "", UserRole.STUDENT);
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Username and password are required"));
    }

    @Test
    void shouldReturnValidationErrorForLogoutWithEmptyUsername() throws Exception {
        User invalidUser = new User("", "password", UserRole.STUDENT);
        String userJson = objectMapper.writeValueAsString(invalidUser);

        mockMvc.perform(post("/api/users/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
               .andExpect(status().isBadRequest())
               .andExpect(content().string("Username is required"));
    }
}