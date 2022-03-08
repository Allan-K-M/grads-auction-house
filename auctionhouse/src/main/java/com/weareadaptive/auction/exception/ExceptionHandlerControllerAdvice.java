package com.weareadaptive.auction.exception;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;

import com.weareadaptive.auction.model.BusinessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlerControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_PROBLEM_JSON);

    var invalidFields = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> new InvalidField(error.getField(), error.getDefaultMessage())).toList();

    return new ResponseEntity<>(new BadRequestInvalidFieldsProblem(invalidFields), headers,
        BAD_REQUEST);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Object> handleNotFoundException(BusinessException ex) {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_PROBLEM_JSON);
    return new ResponseEntity<>(
        new Problem(BAD_REQUEST.value(), BAD_REQUEST.name(), ex.getMessage()), headers,
        BAD_REQUEST);
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
    return ResponseEntity.notFound().build();
  }

  @ExceptionHandler(UnauthorizedActivityException.class)
  public ResponseEntity<Object> handleUnAuthorizedActivityException(
      UnauthorizedActivityException ex) {
    var headers = new HttpHeaders();
    headers.setContentType(APPLICATION_PROBLEM_JSON);
    return new ResponseEntity<>(
        new Problem(UNAUTHORIZED.value(), UNAUTHORIZED.name(), ex.getMessage()), headers,
        UNAUTHORIZED);
  }
}
