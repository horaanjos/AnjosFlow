package com.anjosflow.common.health;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.anjosflow.common.error.GlobalExceptionHandler;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HealthControllerTest {
    private final HealthService service = mock(HealthService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new HealthController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void healthyDatabaseReturns200() throws Exception {
        when(service.check()).thenReturn(new HealthResponse("UP", "UP", Instant.now()));
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void unavailableDatabaseReturns503() throws Exception {
        when(service.check()).thenReturn(new HealthResponse("DOWN", "DOWN", Instant.now()));
        mvc.perform(get("/api/health")).andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value("DOWN"));
    }

    @Test
    void unexpectedErrorReturnsSanitizedProblem() throws Exception {
        when(service.check()).thenThrow(new IllegalStateException("secret connection details"));
        mvc.perform(get("/api/health")).andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Erro interno"))
                .andExpect(jsonPath("$.detail").value("Não foi possível concluir a solicitação. Tente novamente mais tarde."));
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/health"))
                .andExpect(status().isMethodNotAllowed());
    }
}
