package ai.concerto.event.exception;

public class ResponseRendererException extends RuntimeException {

  public ResponseRendererException(String message) {
    super(message);
  }

  public ResponseRendererException(String message, Throwable throwable) {
    super(message, throwable);
  }

  public ResponseRendererException(Throwable throwable) {
    super(throwable);
  }
}
