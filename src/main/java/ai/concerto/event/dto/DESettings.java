package ai.concerto.event.dto;

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
public class DESettings {

  private Boolean useChitchat = false;
  private Boolean useLongAnswer = false;
  private Boolean liveAgent = false;
  private Boolean contextSwitching = false;
  private Boolean disableRecommendations = false;
  private Boolean disableQaOnMessage = false;
  private Boolean contextSwitchingMessage = false;
  private String selectCorrectAppointMsg;
}
