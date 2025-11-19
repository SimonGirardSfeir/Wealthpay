package org.girardsimon.wealthpay.shared.infrastructure.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FakeController.class)
@ActiveProfiles("test")
class GlobalExceptionHandlerTest {

    @MockitoBean
    FakeService fakeService;

    @Autowired
    MockMvc mockMvc;

    @Test
    void httpMessageNotReadableException_should_returns_bad_request() throws Exception {
        // Arrange
        String message = "message";
        when(fakeService.fakeMethod()).thenThrow(new HttpMessageNotReadableException(message, new HttpInputMessage() {
            @Override
            public InputStream getBody() {
                return InputStream.nullInputStream();
            }

            @Override
            public HttpHeaders getHeaders() {
                return HttpHeaders.EMPTY;
            }
        }));

        // Act ... Assert
        mockMvc.perform(get("/fake"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(message));
    }
}