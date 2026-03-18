package ai.concerto.event.service;

import ai.concerto.event.exchange.OgTagsResponse;
import ai.concerto.event.exchange.TextToSpeechRequest;
import com.google.cloud.speech.v1.*;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.opengraph.OpenGraph;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
public class AllegroUiApiService {

  public Map<String, String> convertTextToSpeech(TextToSpeechRequest request) {


    try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {

      SynthesisInput input = null;
      if (StringUtils.hasText(request.getInput().getSsml())) {
        input = SynthesisInput.newBuilder().setSsml(request.getInput().getSsml()).build();
      }

      VoiceSelectionParams voice =
          VoiceSelectionParams.newBuilder().setLanguageCode(request.getVoice().getLanguageCode())
              .setSsmlGender(SsmlVoiceGender.valueOf(request.getVoice().getSsmlGender()))
              .setName(request.getVoice().getName()).build();

      AudioConfig audioConfig = AudioConfig.newBuilder()
          .setAudioEncoding(AudioEncoding.valueOf(request.getAudioConfig().getAudioEncoding()))
          .setSampleRateHertz(request.getAudioConfig().getSpeakingRate())
          .setPitch(request.getAudioConfig().getPitch()).build();

      SynthesizeSpeechResponse speechResponse =
          textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

      Map<String, String> response = new HashMap<>();
      response.put("audioContent",
          Base64.getEncoder().encodeToString(speechResponse.getAudioContent().toByteArray()));
      return response;

    } catch (Exception e) {
      log.error("Error occurred while parsing text to speech {}", e);
    }
    return Collections.emptyMap();
  }

  public String convertSpeechToText(byte[] content) {

    StringBuilder text = new StringBuilder();
    try (SpeechClient speechClient = SpeechClient.create()) {

      RecognitionAudio recognitionAudio =
          RecognitionAudio.newBuilder().setContent(ByteString.copyFrom(content)).build();

      SpeechContext speechContext = SpeechContext.newBuilder().build();

      RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
          .setEncoding(RecognitionConfig.AudioEncoding.LINEAR16).addSpeechContexts(speechContext)
          .setLanguageCode("en-IN").setSampleRateHertz(8000).build();

      RecognizeResponse recognizeResponse =
          speechClient.recognize(recognitionConfig, recognitionAudio);

      for (SpeechRecognitionResult result : recognizeResponse.getResultsList()) {
        text.append(result.getAlternatives(0).getTranscript());
      }

    } catch (Exception ex) {
      log.error("Error occurred while converting speech to text", ex);
    }

    return text.toString();
  }


  public OgTagsResponse getOgTags(String url) {

    try {
      OpenGraph parsedUrl = new OpenGraph(url, false);
      OgTagsResponse ogTagsResponse = new OgTagsResponse();

      ogTagsResponse.getData().setOgSiteName(parsedUrl.getContent("site_name"));
      ogTagsResponse.getData().setOgUrl(parsedUrl.getOriginalUrl());
      ogTagsResponse.getData().setOgTitle(parsedUrl.getContent("title"));
      ogTagsResponse.getData().setOgDescription(parsedUrl.getContent("description"));
      ogTagsResponse.getData().setOgType(parsedUrl.getContent("type"));

      ogTagsResponse.getData().getOgImage().setUrl(parsedUrl.getContent("image"));
      ogTagsResponse.getData().getOgImage().setWidth(parsedUrl.getContent("image:width"));
      ogTagsResponse.getData().getOgImage().setHeight(parsedUrl.getContent("image:height"));
      ogTagsResponse.getData().getOgImage().setType(parsedUrl.getContent("image:type"));

      ogTagsResponse.getData().getOgVideo().setUrl(parsedUrl.getContent("video:url"));
      ogTagsResponse.getData().getOgVideo().setWidth(parsedUrl.getContent("video:width"));
      ogTagsResponse.getData().getOgVideo().setHeight(parsedUrl.getContent("video:height"));
      ogTagsResponse.getData().getOgVideo().setType(parsedUrl.getContent("video:type"));

      return ogTagsResponse;
    } catch (Exception e) {
      log.error("Unable to parse url :", e);
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to parse url");
    }
  }

}
