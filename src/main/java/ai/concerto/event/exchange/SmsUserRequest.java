package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class SmsUserRequest {

  private String from;
  private String to;
  private String name;
  private String text;

  public SmsUserRequest(String from, String to, String name, String text) {
    this.from = from;
    this.to = to;
    this.name = name;
    this.text = text;

  }
}
