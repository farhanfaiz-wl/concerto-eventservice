package ai.concerto.event.render;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse.Row;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse.Section;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse.WhatsappInteractive;
import ai.concerto.event.exchange.BotWhatsappCloudApiResponse.WhatsappText;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WhatsappCloudApiRenderer implements ResponseRenderer {

  private static final String WHATSAPP = "whatsapp";

  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    BotWhatsappCloudApiResponse response = new BotWhatsappCloudApiResponse();
    response.setAccessToken(deRequest.getAccessToken());
    response.setMessagingProduct(WHATSAPP);
    response.setTo(deRequest.getPhoneNumberWithCountryCode());
    response.setPhoneNumberId(deRequest.getPhoneNumberId());

    StringBuilder textBuilder = new StringBuilder();
    deResponse.getBotReply().getText().stream()
        .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
    // add recommendation
    List<Map<String, String>> recommendations = new ArrayList<>();
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getRecommend());
    }
    if (!CollectionUtils.isEmpty(deResponse.getBotReply().getIntentRecommend())) {
      recommendations.addAll(deResponse.getBotReply().getIntentRecommend());
    }

    if (!recommendations.isEmpty()) {
      WhatsappInteractive interactive = new WhatsappInteractive();
      response.setType("interactive");
      interactive.getBody().setText(textBuilder.toString().trim());
      interactive.setType("list");

      List<Section> sections = new ArrayList<>();
      Section section = new Section();
      section.setTitle("Suggestions");
      List<Row> rows = new ArrayList<>();
      for (Map<String, String> rcmMap : recommendations) {
        Row row = new Row();
        row.setId(UUID.randomUUID().toString());
        if (rcmMap.getOrDefault(WHATSAPP, rcmMap.get("text")).length() > 24) {
          row.setTitle(
              rcmMap.getOrDefault(WHATSAPP, rcmMap.get("text")).substring(0, 20).concat("..."));
        } else {
          row.setTitle(rcmMap.getOrDefault(WHATSAPP, rcmMap.get("text")));
        }
        row.setDescription(rcmMap.getOrDefault(WHATSAPP, rcmMap.get("text")));
        rows.add(row);
      }
      section.setRows(rows);
      sections.add(section);
      interactive.getAction().setSections(sections);
      interactive.getAction().setButton("Send");
      response.setInteractive(interactive);
    } else {
      response.setType("text");
      WhatsappText whatsappText = new WhatsappText();
      whatsappText.setBody(textBuilder.toString().trim());
      response.setText(whatsappText);
    }

    return response;
  }

}
