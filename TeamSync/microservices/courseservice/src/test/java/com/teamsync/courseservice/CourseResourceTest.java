package com.teamsync.courseservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamsync.courseservice.model.Course;
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
class CourseResourceTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldReturnEmptyCoursesWithMockData() throws Exception {
        // Should succeed and return empty array with mock database
        mockMvc.perform(get("/api/courses"))
               .andExpect(status().isOk())
               .andExpect(content().json("[]"));
    }
}