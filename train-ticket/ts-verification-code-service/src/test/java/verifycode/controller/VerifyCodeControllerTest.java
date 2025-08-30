package verifycode.controller;

import com.alibaba.fastjson.JSONObject;
import edu.fudan.common.util.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import verifycode.service.VerifyCodeService;

import javax.servlet.*;
import javax.servlet.http.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.rmi.MarshalledObject;
import java.security.Principal;
import java.util.*;

@RunWith(JUnit4.class)
public class VerifyCodeControllerTest {

    @InjectMocks
    private VerifyCodeController verifyCodeController;

    @Mock
    private VerifyCodeService verifyCodeService;
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(verifyCodeController).build();
    }

    @Test
    public void testGenerateVerifyCode() throws Exception {
        Mockito.doNothing().when(verifyCodeService).generateVerifyCode(Mockito.any(HttpServletResponse.class));
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/verifycode/generate")).andReturn();
        Mockito.verify(verifyCodeService, Mockito.times(1)).generateVerifyCode(Mockito.any(HttpServletResponse.class));
    }

    @Test
    public void testVerifyCode() throws Exception {
        Mockito.when(verifyCodeService.verifyCode(Mockito.anyString(), Mockito.any(HttpHeaders.class))).thenReturn(true);
        String result = mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/verifycode/verify/verifyCode"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString();
        Response response = JSONObject.parseObject(result, Response.class);
        Assert.assertEquals(1, response.getStatus().intValue());
    }

}
