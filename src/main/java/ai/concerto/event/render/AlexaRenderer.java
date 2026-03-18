package ai.concerto.event.render;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import com.amazon.ask.model.ResponseEnvelope;
import com.amazon.ask.model.interfaces.alexa.presentation.apl.RenderDocumentDirective;
import com.amazon.ask.model.ui.Image;
import com.amazon.ask.response.ResponseBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.DataSources;
import ai.concerto.event.dto.DataSources.BodyTemplate3Data;
import ai.concerto.event.dto.DataSources.ImageDto;
import ai.concerto.event.dto.DataSources.ListBodyTemplate1;
import ai.concerto.event.dto.DataSources.ListItem;
import ai.concerto.event.dto.DataSources.ListPage;
import ai.concerto.event.dto.DataSources.ListTemplate1ListData;
import ai.concerto.event.dto.DataSources.ListTemplate1Metadata;
import ai.concerto.event.dto.DataSources.Sources;
import ai.concerto.event.dto.DataSources.Text;
import ai.concerto.event.dto.DataSources.TextContent;
import ai.concerto.event.enums.Source;
import ai.concerto.event.utils.Constants.DEResponseConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AlexaRenderer implements ResponseRenderer {

  private static final String RESPONSE_VERSION = "1.0";
  private static final String RESPONSE_USER_AGENT = "eventService";
  private static final int CARD_TITLE_MAXLEN = 25;
  private static final String BODY_TEMPALTE_3_DOCUMENT = "bodyTemplate3Document.json";
  private static final String LIST_1_BODY_TEMPLATE_DOCUMENT = "list1BodyTemplateDocument.json";
  private static final String PLAIN_TEXT = "PlainText";

  @Autowired
  private ObjectMapper mapper;



  @SneakyThrows
  @Override
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    ResponseBuilder responseBuilder = new ResponseBuilder();

    try {
      StringBuilder textBuilder = new StringBuilder();
      deResponse.getBotReply().getText().stream()
          .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
      String botReply = speakEncapsulateIfNeeded(textBuilder.toString());
      StringBuilder repeateBuilder = new StringBuilder();
      deResponse.getBotRepeats().stream()
          .forEach(text -> repeateBuilder.append(text.trim()).append(System.lineSeparator()));
      String repeate = null;
      if (StringUtils.hasText(repeateBuilder.toString())) {
        repeate = speakEncapsulateIfNeeded(repeateBuilder.toString());
      } else {
        repeate = botReply;
      }
      List<Map<String, Object>> alexaCards = deResponse.getBotReply().getAlexaCard();
      if (!CollectionUtils.isEmpty(alexaCards)) {
        addCardInResponse(responseBuilder, alexaCards.get(0), botReply);
      }
      if (deRequest.getSource().equals(Source.echo_show)
          && !CollectionUtils.isEmpty(deResponse.getBotReply().getEchoCard())) {
        Map<String, Object> botRepliesEchoShow = deResponse.getBotReply().getEchoCard().get(0);

        if (shouldRenderlistTemplate1(botRepliesEchoShow)) {

          DataSources dataSources = getListTemplate1Data(botRepliesEchoShow);
          responseBuilder
              .addDirective(getRenderDocumentDirective(LIST_1_BODY_TEMPLATE_DOCUMENT, dataSources));

        } else if (shouldRenderBodyTemplate3Data(botRepliesEchoShow)) {
          DataSources dataSources = getBodyTemplate3Data(botRepliesEchoShow);
          responseBuilder
              .addDirective(getRenderDocumentDirective(BODY_TEMPALTE_3_DOCUMENT, dataSources));

        }
      }
      responseBuilder.withSpeech(botReply).withReprompt(repeate)
          .withShouldEndSession(deResponse.getIsDialogExit());
      return mapper.writeValueAsString(ResponseEnvelope.builder().withVersion(RESPONSE_VERSION)
          .withUserAgent(RESPONSE_USER_AGENT).withResponse(responseBuilder.build().orElse(null))
          .build());
    } catch (Exception ex) {
      log.error("Error occured while building alexa response {}", ex);
    }

    return mapper.writeValueAsString(
        ResponseEnvelope.builder().withVersion(RESPONSE_VERSION).withUserAgent(RESPONSE_USER_AGENT)
            .withResponse(responseBuilder
                .withSpeech("An unknown error occured. Ending the skill. Please try again later.")
                .withShouldEndSession(true).build().orElse(null))
            .build());
  }


  @SneakyThrows
  private RenderDocumentDirective getRenderDocumentDirective(String fileName,
      DataSources dataSource) {

    log.info("Rendering document ={}", fileName);
    InputStream template = this.getClass().getClassLoader().getResourceAsStream(fileName);
    Map<String, Object> document =
        mapper.readValue(template, new TypeReference<Map<String, Object>>() {});
    Map<String, Object> dataSources = mapper.readValue(mapper.writeValueAsString(dataSource),
        new TypeReference<Map<String, Object>>() {});
    return RenderDocumentDirective.builder().withDocument(document).withDatasources(dataSources)
        .build();
  }

  private String speakEncapsulateIfNeeded(String s) {
    if (StringUtils.hasText(s)) {
      return String.format("<speak>%s</speak>",
          s.replaceAll("<\\s*\\/?\\s*speak\\s*>", "").replace("& ", "and "));
    }
    return s;
  }


  private void addCardInResponse(ResponseBuilder responseBuilder, Map<String, Object> alexaCard,
      String say) {

    log.info("Adding basic card in response.");

    String cardTitle = getCardPart(alexaCard, DEResponseConstants.ALEXA_CARD_TITLE_SUFFIX, say);
    String cardText = getCardPart(alexaCard, DEResponseConstants.ALEXA_CARD_TEXT_SUFFIX, say);
    String smallImageUrl =
        getCardPart(alexaCard, DEResponseConstants.ALEXA_CARD_IMAGE_URL_SMALL_SUFFIX, say);
    String largeImageUrl =
        getCardPart(alexaCard, DEResponseConstants.ALEXA_CARD_IMAGE_URL_LARGE_SUFFIX, say);
    log.info("CardText = {}", cardText);
    if (!StringUtils.hasText(smallImageUrl)) {
      smallImageUrl = largeImageUrl;
    }
    if (!StringUtils.hasText(largeImageUrl)) {
      largeImageUrl = smallImageUrl;
    }

    if (StringUtils.hasText(smallImageUrl) && StringUtils.hasText(largeImageUrl)) {
      responseBuilder.withStandardCard(cardTitle, cardText, Image.builder()
          .withSmallImageUrl(smallImageUrl).withLargeImageUrl(largeImageUrl).build());
    } else {
      responseBuilder.withSimpleCard(cardTitle, cardText);
    }
  }



  private String getCardPart(Map<String, Object> cardObject, final String key, final String say) {

    if (cardObject == null) {
      return null;
    }
    String cardPart = (String) cardObject.get(key);
    try {
      if (DEResponseConstants.ALEXA_CARD_TITLE_SUFFIX.equals(key)) {

        if (StringUtils.hasText(cardPart)) {
          return cardPart;
        } else {
          String cardText = (String) cardObject
              .getOrDefault(DEResponseConstants.ALEXA_CARD_TEXT_SUFFIX, unProsody(say));
          // if no title, return card text or say
          return (cardText.length() > CARD_TITLE_MAXLEN)
              ? cardText.substring(0, CARD_TITLE_MAXLEN) + "..."
              : cardText;

        }
      } else if (DEResponseConstants.ALEXA_CARD_TEXT_SUFFIX.equals(key)) {
        if (cardPart instanceof String && StringUtils.hasText(cardPart)) {
          if (cardPart.startsWith("[") && cardPart.endsWith("]")) {
            cardPart = Arrays.stream(mapper.convertValue(cardPart, String[].class))
                .collect(Collectors.joining());
          }
          return cardPart;
        } else {
          return unProsody(say);
        }
      }
    } catch (Exception ex) {
      log.error("Error occured while processing card detail of alexa.");
    }
    return cardPart;
  }



  private DataSources getBodyTemplate3Data(Map<String, Object> botRepliesEchoShow) {

    DataSources dataSources = DataSources.builder().build();

    if (ObjectUtils.isNotEmpty(botRepliesEchoShow)) {

      BodyTemplate3Data bodyTemplate3Data = dataSources.new BodyTemplate3Data();
      ImageDto image = dataSources.new ImageDto();
      List<Sources> sources = new ArrayList<>();
      TextContent textContent = dataSources.new TextContent();
      bodyTemplate3Data.setImage(image);
      bodyTemplate3Data.setTextContent(textContent);
      bodyTemplate3Data.setObjectId("bt3data");
      bodyTemplate3Data.setType("object");

      if (ObjectUtils
          .isNotEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TITLE))) {
        bodyTemplate3Data
            .setTitle((String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TITLE));
      }

      if (ObjectUtils.isNotEmpty(
          botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_SMALL_SOURCE_URL))) {
        Sources source = dataSources.new Sources();
        source.setUrl(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_SMALL_SOURCE_URL));
        source.setSize("small");
        source.setHeightPixels(0);
        source.setWidthPixels(0);
        sources.add(source);
        image.setSources(sources);
      }

      if (ObjectUtils.isNotEmpty(
          botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_LARGE_SOURCE_URL))) {

        Sources source = dataSources.new Sources();
        source.setUrl(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_LARGE_SOURCE_URL));
        source.setSize("large");
        source.setHeightPixels(0);
        source.setWidthPixels(0);
        sources.add(source);
        image.setSources(sources);
      }

      if (ObjectUtils.isNotEmpty(
          botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_TITLE))) {
        Text title = dataSources.new Text();
        title.setType(PLAIN_TEXT);
        title.setText(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_TITLE));
        textContent.setTitle(title);
      }

      if (ObjectUtils.isNotEmpty(
          botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_SUBTITLE))) {
        Text subTitle = dataSources.new Text();
        subTitle.setType(PLAIN_TEXT);
        subTitle.setText((String) botRepliesEchoShow
            .get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_SUBTITLE));
        textContent.setSubtitle(subTitle);
      }

      if (ObjectUtils.isNotEmpty(
          botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_PRIMARY_TEXT))) {
        Text primarytext = dataSources.new Text();
        primarytext.setType(PLAIN_TEXT);
        primarytext.setText((String) botRepliesEchoShow
            .get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_PRIMARY_TEXT));
        textContent.setPrimaryText(primarytext);
      }

      dataSources.setBodyTemplate3Data(bodyTemplate3Data);
    }
    return dataSources;
  }


  private DataSources getListTemplate1Data(Map<String, Object> botRepliesEchoShow) {

    DataSources dataSources = DataSources.builder().build();

    if (ObjectUtils.isNotEmpty(botRepliesEchoShow)) {

      ListBodyTemplate1 listBodyTemplate1 = dataSources.new ListBodyTemplate1();
      ListTemplate1Metadata listTemplate1Metadata = dataSources.new ListTemplate1Metadata();
      ListTemplate1ListData lisTemplate1ListData = dataSources.new ListTemplate1ListData();
      ListPage listPage = dataSources.new ListPage();
      List<ListItem> listItems = new ArrayList<>();

      if (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_TITLE) && !ObjectUtils
          .isEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_TITLE))) {
        listTemplate1Metadata
            .setTitle((String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_TITLE));
      }
      if (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION1) && !ObjectUtils
          .isEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION1))) {

        listItems.add(getListItem(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION1), 1));
      }
      if (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION2) && !ObjectUtils
          .isEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION2))) {

        listItems.add(getListItem(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION2), 2));
      }

      if (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION3) && !ObjectUtils
          .isEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION3))) {

        listItems.add(getListItem(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION3), 3));
      }

      if (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION4) && !ObjectUtils
          .isEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION4))) {
        listItems.add(getListItem(
            (String) botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_LIST_OPTION4), 4));
      }
      listPage.setListItems(listItems);
      lisTemplate1ListData.setType("list");
      lisTemplate1ListData.setListId("lt1data");
      lisTemplate1ListData.setTotalNumberOfItems(listItems.size());
      lisTemplate1ListData.setListPage(listPage);
      listBodyTemplate1.setListTemplate1ListData(lisTemplate1ListData);
      listBodyTemplate1.setListTemplate1Metadata(listTemplate1Metadata);
      dataSources.setListBodyTemplate1(listBodyTemplate1);
    }
    return dataSources;
  }

  private ListItem getListItem(String listOption, Integer ordinalNumber) {

    DataSources dataSources = DataSources.builder().build();
    ListItem listItem = dataSources.new ListItem();
    listItem.setListItemIdentifier(listOption);
    listItem.setToken(listOption);
    listItem.setOrdinalNumber(ordinalNumber);
    Text primaryText = dataSources.new Text();
    primaryText.setText(listOption);
    primaryText.setType(PLAIN_TEXT);
    TextContent listOption1Content = dataSources.new TextContent();
    listOption1Content.setPrimaryText(primaryText);
    listItem.setTextContent(listOption1Content);
    return listItem;
  }


  private boolean shouldRenderlistTemplate1(Map<String, Object> botRepliesEchoShow) {

    return (ObjectUtils.isNotEmpty(botRepliesEchoShow)
        && (botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_TITLE)
            || botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION1)
            || botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION2)
            || botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION3)
            || botRepliesEchoShow.containsKey(DEResponseConstants.ECHO_SHOW_LIST_OPTION4)));
  }


  private boolean shouldRenderBodyTemplate3Data(Map<String, Object> botRepliesEchoShow) {

    return (ObjectUtils.isNotEmpty(botRepliesEchoShow)
        && (ObjectUtils.isNotEmpty(botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TITLE))
            || ObjectUtils.isNotEmpty(
                botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_SMALL_SOURCE_URL))
            || ObjectUtils
                .isNotEmpty(
                    botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_LARGE_SOURCE_URL))
            || ObjectUtils.isNotEmpty(
                botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_TITLE))
            || ObjectUtils.isNotEmpty(
                botRepliesEchoShow.get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_SUBTITLE))
            || ObjectUtils.isNotEmpty(botRepliesEchoShow
                .get(DEResponseConstants.ECHO_SHOW_BODY_TEXT_CONTENT_PRIMARY_TEXT))));
  }


  private String unProsody(String s) {
    if (s == null) {
      return null;
    }
    s = s.replaceAll("<say-as interpret-as='ordinal'>(.*)</say-as>", "$1."); // r
    return s.replaceAll("\\<.*?>", "").replaceAll("[ ]{2,}", " ").trim();
  }

  @SneakyThrows
  public String renderErrorMessage(String message, Boolean dialogExit) {
    ResponseBuilder responseBuilder = new ResponseBuilder();
    responseBuilder.withSpeech(message);
    responseBuilder.withShouldEndSession(dialogExit);
    return mapper.writeValueAsString(responseBuilder);
  }

}
