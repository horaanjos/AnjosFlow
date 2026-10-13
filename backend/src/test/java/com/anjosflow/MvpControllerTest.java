package com.anjosflow;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.anjosflow.calendar.*;
import com.anjosflow.common.error.*;
import com.anjosflow.exam.*;
import com.anjosflow.goal.*;
import com.anjosflow.review.*;
import com.anjosflow.study.*;
import com.anjosflow.task.*;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class MvpControllerTest {
  private MockMvc mvc;

  @BeforeEach
  void setup() {
    var tasks = mock(TaskService.class);
    var events = mock(CalendarEventService.class);
    var sessions = mock(StudySessionService.class);
    var exams = mock(ExamService.class);
    var goals = mock(StudyGoalService.class);
    var reviews = mock(ReviewService.class);
    when(tasks.find(99L)).thenThrow(ApiException.missing("Tarefa"));
    when(events.find(99L)).thenThrow(ApiException.missing("Evento"));
    when(sessions.find(99L)).thenThrow(ApiException.missing("Sessão"));
    when(exams.find(99L)).thenThrow(ApiException.missing("Prova"));
    when(goals.find(99L)).thenThrow(ApiException.missing("Meta"));
    when(reviews.find(99L)).thenThrow(ApiException.missing("Revisão"));
    when(tasks.list(null, null, null, null)).thenReturn(List.of());
    when(events.list(null, null, null)).thenReturn(List.of());
    when(sessions.list(null, null, null)).thenReturn(List.of());
    when(exams.list()).thenReturn(List.of());
    when(goals.list()).thenReturn(List.of());
    when(reviews.list()).thenReturn(List.of());
    mvc =
        MockMvcBuilders.standaloneSetup(
                new TaskController(tasks),
                new CalendarEventController(events),
                new StudySessionController(sessions),
                new ExamController(exams),
                new StudyGoalController(goals),
                new ReviewController(reviews))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  @ParameterizedTest
  @ValueSource(strings = {"tasks", "events", "sessions", "exams", "goals", "reviews"})
  void emptyRequestReturns400(String route) throws Exception {
    mvc.perform(post("/api/" + route).contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors").exists());
  }

  @ParameterizedTest
  @ValueSource(strings = {"tasks", "events", "sessions", "exams", "goals", "reviews"})
  void missingResourceReturns404(String route) throws Exception {
    mvc.perform(get("/api/" + route + "/99")).andExpect(status().isNotFound());
  }

  @ParameterizedTest
  @ValueSource(strings = {"tasks", "events", "sessions", "exams", "goals", "reviews"})
  void emptyListReturnsArray(String route) throws Exception {
    mvc.perform(get("/api/" + route)).andExpect(status().isOk()).andExpect(content().json("[]"));
  }
}
