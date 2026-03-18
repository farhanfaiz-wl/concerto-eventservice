package ai.concerto.event.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.TextToSpeechClient;
import com.google.cloud.texttospeech.v1.TextToSpeechSettings;
import ai.concerto.event.exchange.TextToSpeechRequest;

class AllegroUiApiServiceTest {

  @InjectMocks
  @Resource
  public AllegroUiApiService allegroUiApiService;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void convertTextToSpeechErrorTest() {
    TextToSpeechRequest request = new TextToSpeechRequest();
    assertEquals(Collections.emptyMap(), allegroUiApiService.convertTextToSpeech(request));
  }

  @SuppressWarnings("unchecked")
  @Test
  void convertTextToSpeechTest() throws Exception {
    // add credentials to perform google text to speech
    InputStream inputStream = getClass().getResourceAsStream("/google_creds.json");
    GoogleCredentials googleCredentials = GoogleCredentials.fromStream(inputStream);
    TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
        .setCredentialsProvider(FixedCredentialsProvider.create(googleCredentials)).build();
    TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings);

    mockStatic(TextToSpeechClient.class);
    when(TextToSpeechClient.create()).thenReturn(textToSpeechClient);

    TextToSpeechRequest request = new TextToSpeechRequest();
    request.getInput().setSsml("<speak>Hi</speak>");
    request.getAudioConfig().setAudioEncoding("MP3");
    request.getAudioConfig().setPitch(0.00);
    request.getAudioConfig().setSpeakingRate(8000);
    request.getVoice().setLanguageCode("en-US");
    request.getVoice().setName("en-IN-Wavenet-C");
    request.getVoice().setSsmlGender("MALE");

    Map<String, String> response = new ObjectMapper()
        .convertValue(allegroUiApiService.convertTextToSpeech(request), Map.class);
    // verify that audioContent is not empty
    assertNotNull(response.get("audioContent"));
    assertTrue(!response.get("audioContent").isEmpty());
  }

}
