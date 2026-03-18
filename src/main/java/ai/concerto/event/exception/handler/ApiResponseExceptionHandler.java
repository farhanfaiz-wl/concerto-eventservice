package ai.concerto.event.exception.handler;

import ai.concerto.event.exception.dto.ApiError;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

@ControllerAdvice
public class ApiResponseExceptionHandler extends ResponseEntityExceptionHandler {

  @Override
  protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append(ex.getMethod());
    errorBuilder.append(" is not supported for this request. Supported methods are ");
    ex.getSupportedHttpMethods().stream().forEach(method -> errorBuilder.append(method + " "));

    ApiError apiError =
        new ApiError(
            HttpStatus.METHOD_NOT_ALLOWED,
            ex.getLocalizedMessage(),
            errorBuilder.toString().trim());
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    StringBuilder errorBuilder = new StringBuilder();
    errorBuilder.append(ex.getContentType());
    errorBuilder.append(" is not supported for this request. Supported media types are ");
    ex.getSupportedMediaTypes().stream().forEach(mediaType -> errorBuilder.append(mediaType + " "));

    ApiError apiError =
        new ApiError(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            ex.getLocalizedMessage(),
            errorBuilder.toString().trim());
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleMissingPathVariable(
      MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    String error = ex.getVariableName() + " path variable is missing";

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    String error = ex.getParameterName() + " parameter is missing";

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleServletRequestBindingException(
      ServletRequestBindingException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleExceptionInternal(ex, null, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleConversionNotSupported(
      ConversionNotSupportedException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleExceptionInternal(ex, null, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleTypeMismatch(
      TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    String error = ex.getPropertyName() + " should be of type " + ex.getRequiredType().getName();

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleExceptionInternal(ex, null, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleHttpMessageNotWritable(
      HttpMessageNotWritableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleExceptionInternal(ex, null, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    return handleBindException(ex, headers, status, request);
  }

  @Override
  protected ResponseEntity<Object> handleMissingServletRequestPart(
      MissingServletRequestPartException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request) {
    String error = ex.getRequestPartName() + " request part is missing";

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), error);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleBindException(
      BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    List<String> errors = new ArrayList<>();
    ex.getBindingResult().getFieldErrors().stream()
        .forEach(error -> errors.add(error.getField() + ": " + error.getDefaultMessage()));
    ex.getBindingResult().getGlobalErrors().stream()
        .forEach(error -> errors.add(error.getObjectName() + ": " + error.getDefaultMessage()));

    ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage(), errors);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleNoHandlerFoundException(
      NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
    String error = "No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL();

    ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), error);
    return ResponseEntity.status(apiError.getStatus()).headers(headers).body(apiError);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {

    if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
      request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
    }
    return super.handleExceptionInternal(ex, body, headers, status, request);
  }
}
