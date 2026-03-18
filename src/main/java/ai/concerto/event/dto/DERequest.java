package ai.concerto.event.dto;

import ai.concerto.event.enums.Source;
import ai.concerto.event.exchange.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DERequest {

  private boolean logMe = true;
  private String template;
  private String cqamodelId;
  private String deviceId;
  private DESettings deSettins;
  private String deviceType;
  private String projectId;
  private String skillId;
  private String turnId;
  private String sessionId;
  private boolean sessionNew;
  private Integer sessionTimeOut;
  private Long timestampS;
  private Long timestampMs;
  private String userId;
  private String userFirstName;
  private String userLastName;
  private String userFullName;
  private String userProfilePic;
  private String userLocale;
  private String email;
  private String phoneNumber;
  private String phoneNumberLineType;
  private String phoneNumberCountryCode;
  private String phoneNumberWithCountryCode;
  private String clientType;
  private Source source;
  private String sourceType;
  private Boolean llmEnabled;
  private String sourceUrl;
  private Boolean sendEmail;
  private Boolean sendSearchResults;
  private String attachmentPath;
  private String attachmentType;
  private Long attachmentSize;
  private String userInputLast;
  private String userAuthCode;
  private boolean isQAEnable;
  private boolean liveAgentRunning;
  private String dayOfWeekNyc;
  private String hourOfDayNyc;
  private String language;
  private String vendor;
  private String slackChannel;
  private String universalUserId;
  private String host;
  private String complianceType;
  private String phoneNumberId;
  private String accessToken;
  private String tenantId;
  private Integer ticketNumber;
  private String ticketId;
  private String webSocketUserName;
  private UserDetails.UserProfile userProfile;
  private String baseUrl;
  private String centerId;
  private boolean isChatbotVoice = false;
  private String languageCode;
  private String voiceName;
  private String voiceSsmlGender;
  private boolean forceNewSession = false;


  @JsonIgnore
  private String botAuthId;
  @JsonIgnore
  private String botAuthToken;
  @JsonIgnore
  private String botPhoneNumber;
  @JsonIgnore
  private String sessionTimeoutPrompt;


  /*
   * TODO: remove below element because DE is not using them
   */
  // private RequestType requestType;
  // private IntentName intentName;
  // private String requestTimestamp;
  // private Object supportedInterfaces;
  // private Map<String, Object> submittedHtmlForm;
  // private Boolean requestShouldLinkResultBeReturned;
  // private String apiEndpoint;
  // private String apiAccessToken;
  // private String message;
  // private String originalProjectId;
  // private boolean fakedCqaModelId;
  // private String falconApi;
  // private List<String> tags;
  // private boolean isGetStarted = false;
  // private String priviousAppData;

  public DERequest() {
    timestampMs = System.currentTimeMillis();
    timestampS = timestampMs / 1000L;

    LocalDateTime nycTime = LocalDateTime.now(ZoneId.of("America/New_York"));
    hourOfDayNyc = String.valueOf(nycTime.getHour());
    dayOfWeekNyc = nycTime.getDayOfWeek().toString();
  }
}
