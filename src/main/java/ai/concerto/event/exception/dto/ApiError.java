package ai.concerto.event.exception.dto;

import java.util.Arrays;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class ApiError {

  private HttpStatus status;
  private String message;
  private List<String> errors;

  public ApiError(HttpStatus status, String message, String error) {
    super();
    this.status = status;
    this.message = message;
    errors = Arrays.asList(error);
  }
}
