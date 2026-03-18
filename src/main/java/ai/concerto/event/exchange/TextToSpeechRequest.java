package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TextToSpeechRequest {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AudioConfig {
    private String audioEncoding = "MP3";
    private double pitch = 0.00;
    private int speakingRate = 8000;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class InputRequest {
    private String ssml;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class VoiceRequest {
    private String languageCode;
    private String name;
    private String ssmlGender;
  }

  private AudioConfig audioConfig = new AudioConfig();
  private InputRequest input = new InputRequest();
  private VoiceRequest voice = new VoiceRequest();

}
