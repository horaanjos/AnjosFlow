package com.anjosflow.subject;

import java.time.Instant;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.anjosflow.common.error.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubjectControllerTest {
    private final SubjectService service = mock(SubjectService.class);
    private final ObjectMapper json = new ObjectMapper();
    private MockMvc mvc;
    private final SubjectResponse subject = new SubjectResponse(1L, "Java", null, "#A3B18A",
            Instant.parse("2026-10-13T12:00:00Z"), Instant.parse("2026-10-13T12:00:00Z"));

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new SubjectController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createsSubjectAndReturnsLocationAndAuditFields() throws Exception {
        when(service.create(new SubjectRequest("Java", null, null))).thenReturn(subject);
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Java  \"}"))
                .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/subjects/1"))
                .andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.name").value("Java"))
                .andExpect(jsonPath("$.color").value("#A3B18A"))
                .andExpect(jsonPath("$.createdAt").exists()).andExpect(jsonPath("$.updatedAt").exists());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n", "A", " A "})
    void rejectsInvalidNames(String name) throws Exception {
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SubjectRequest(name, null, null))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists());
        verifyNoInteractions(service);
    }

    @ParameterizedTest
    @ValueSource(strings = {"green", "123456", "#123", "#GG1234", "#1234567", ""})
    void rejectsInvalidColors(String color) throws Exception {
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SubjectRequest("Java", null, color))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.color").exists());
        verifyNoInteractions(service);
    }

    @Test
    void rejectsOversizedNameAndDescription() throws Exception {
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(new SubjectRequest("a".repeat(101), "d".repeat(501), null))))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.description").exists());
        verifyNoInteractions(service);
    }

    @Test
    void acceptsMaximumLengthsAndValidMixedCaseHex() throws Exception {
        SubjectRequest request = new SubjectRequest("a".repeat(100), "d".repeat(500), "#aB12fF");
        when(service.create(request)).thenReturn(subject);
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON)
                        .content(json.writeValueAsString(request))).andExpect(status().isCreated());
        verify(service).create(request);
    }

    @Test
    void listsSubjects() throws Exception {
        when(service.list()).thenReturn(List.of(subject));
        mvc.perform(get("/api/subjects")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"));
    }

    @Test
    void listsEmptyArray() throws Exception {
        when(service.list()).thenReturn(List.of());
        mvc.perform(get("/api/subjects")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void findsById() throws Exception {
        when(service.findById(1L)).thenReturn(subject);
        mvc.perform(get("/api/subjects/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updatesSubject() throws Exception {
        when(service.update(1L, new SubjectRequest("Java", null, null))).thenReturn(subject);
        mvc.perform(put("/api/subjects/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void validatesUpdateBeforeCallingService() throws Exception {
        mvc.perform(put("/api/subjects/1").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.name").exists());
        verifyNoInteractions(service);
    }

    @Test
    void deletesWithoutResponseBody() throws Exception {
        mvc.perform(delete("/api/subjects/1")).andExpect(status().isNoContent()).andExpect(content().string(""));
        verify(service).delete(1L);
    }

    @Test
    void missingSubjectReturns404ForFindUpdateAndDelete() throws Exception {
        when(service.findById(99L)).thenThrow(new SubjectNotFoundException());
        when(service.update(99L, new SubjectRequest("Java", null, null))).thenThrow(new SubjectNotFoundException());
        doThrow(new SubjectNotFoundException()).when(service).delete(99L);
        mvc.perform(get("/api/subjects/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Disciplina não encontrada."));
        mvc.perform(put("/api/subjects/99").contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Java\"}"))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/api/subjects/99")).andExpect(status().isNotFound());
    }

    @Test
    void malformedJsonReturnsSanitized400() throws Exception {
        mvc.perform(post("/api/subjects").contentType(MediaType.APPLICATION_JSON).content("{invalid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Envie um corpo JSON válido."));
        verifyNoInteractions(service);
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mvc.perform(get("/api/subjects/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}