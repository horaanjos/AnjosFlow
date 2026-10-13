package com.anjosflow.common.error;

import com.anjosflow.subject.SubjectNotFoundException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ApiException.class)
  public ProblemDetail handleApi(ApiException exception) {
    return ProblemDetail.forStatusAndDetail(exception.getStatus(), exception.getMessage());
  }

  @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
  public ProblemDetail handleIntegrity(
      org.springframework.dao.DataIntegrityViolationException exception) {
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.CONFLICT,
        "Este registro possui vínculos ou entra em conflito com dados existentes. Remova os"
            + " vínculos antes de excluir.");
  }

  @ExceptionHandler(SubjectNotFoundException.class)
  public ProblemDetail handleSubjectNotFound(SubjectNotFoundException exception) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    problem.setTitle("Disciplina não encontrada");
    return problem;
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(status, "Verifique os campos informados.");
    problem.setTitle("Dados inválidos");
    Map<String, String> errors = new LinkedHashMap<>();
    exception.getBindingResult().getFieldErrors().stream()
        .sorted(
            java.util.Comparator.comparing(org.springframework.validation.FieldError::getField)
                .thenComparing(error -> String.valueOf(error.getDefaultMessage())))
        .forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
    problem.setProperty("errors", errors);
    return handleExceptionInternal(exception, problem, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException exception,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, "Envie um corpo JSON válido.");
    problem.setTitle("JSON inválido");
    return handleExceptionInternal(exception, problem, headers, status, request);
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception exception) {
    log.error(
        "Falha inesperada ao processar solicitação. Tipo: {}",
        exception.getClass().getSimpleName());
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Não foi possível concluir a solicitação. Tente novamente mais tarde.");
    problem.setTitle("Erro interno");
    return problem;
  }
}
