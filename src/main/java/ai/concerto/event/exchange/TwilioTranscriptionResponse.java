package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TwilioTranscriptionResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Transcripts {
    private String text;
    private String confidence;
    private String srt;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class TranscriptionMedia {
    private String status;
    private Transcripts transcripts;
  }

  private TranscriptionMedia media;
}
