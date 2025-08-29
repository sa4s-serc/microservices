package cancel.controller;

import cancel.service.CancelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.fudan.common.util.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CancelController.class)
class CancelControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CancelService cancelService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Setup common mock responses
    }

    @Test
    void testWelcomeEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/cancelservice/welcome"))
                .andExpect(status().isOk())
                .andExpect(content().string("Welcome to [ Cancel Service ] !"));
    }

    @Test
    void testCalculateRefund() throws Exception {
        Response<String> mockResponse = new Response<>(1, "Success", "80.00");
        when(cancelService.calculateRefund(anyString(), any(HttpHeaders.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/cancelservice/cancel/refound/order123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").value("80.00"));
    }

    @Test
    void testCancelOrder() throws Exception {
        Response<String> mockResponse = new Response<>(1, "Cancel Success", "80.00");
        when(cancelService.cancelOrder(anyString(), anyString(), any(HttpHeaders.class))).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/cancelservice/cancel/order123/user456")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data").value("80.00"));
    }
}