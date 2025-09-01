package com.teamsync.taskservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamsync.taskservice.model.Task;
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
class TaskResourceTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldAcceptValidTaskWithMockData() throws Exception {
        Task validTask = new Task();
        validTask.setTitle("Test Task");
        validTask.setTeamId(1);
        validTask.setAssignedToId(1);
        
        String taskJson = objectMapper.writeValueAsString(validTask);

        // Should succeed with mock database
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(taskJson))
               .andExpect(status().isOk())
               .andExpect(content().string("Task added successfully."));
    }
}