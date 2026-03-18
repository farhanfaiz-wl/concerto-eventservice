package ai.concerto.event.exception;

public class RequestHandlerException extends RuntimeException {

  public RequestHandlerException(String message) {
    super(message);
  }

  public RequestHandlerException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public RequestHandlerException(Throwable throwable) {
    super(throwable);
  }
}
