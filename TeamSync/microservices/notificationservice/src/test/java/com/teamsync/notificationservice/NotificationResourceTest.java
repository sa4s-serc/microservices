package com.teamsync.notificationservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ActiveProfiles("test")
class NotificationResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnEmptyNotificationsWithMockData() throws Exception {
        // Should succeed and return empty array with mock database
        mockMvc.perform(get("/api/notifications"))
               .andExpect(status().isOk())
               .andExpect(content().json("[]"));
    }
}