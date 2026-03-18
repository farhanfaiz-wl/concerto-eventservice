package ai.concerto.event.exception;

public class ResponseHandlerException extends RuntimeException {

  public ResponseHandlerException(String message) {
    super(message);
  }

  public ResponseHandlerException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public ResponseHandlerException(Throwable throwable) {
    super(throwable);
  }
}
