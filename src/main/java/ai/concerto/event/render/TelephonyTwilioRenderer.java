package ai.concerto.event.render;

import ai.concerto.event.dto.DEBotResponse;
import ai.concerto.event.dto.DERequest;
import ai.concerto.event.dto.Session;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.exchange.BotTelephonyTwilioResponse;
import ai.concerto.event.publisher.AnalyticsStreamPublisher;
import ai.concerto.event.service.SessionService;
import ai.concerto.event.utils.Constants;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@Slf4j
@Service
public class TelephonyTwilioRenderer implements ResponseRenderer {

  private static final String SAY_XML_FORMAT = "<Say>%s</Say>";
  private static final String DUMMY_XML_FORMAT = "<Dummy>%s</Dummy>";
  private static final String LANGUAGE = "language";
  private static final String VOICE = "voice";
  private static final String SPEECH_MODEL = "speechModel";

  @Autowired
  private SessionService sessionService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private AnalyticsStreamPublisher analyticsStreamPublisher;

  @Override
  @SneakyThrows
  public Object render(Object userRequest, DERequest deRequest, DEBotResponse deResponse) {
    Map<String, String> params =
        objectMapper.convertValue(userRequest, new TypeReference<Map<String, String>>() {});

    Session session = sessionService.getSession(deRequest.getProjectId(),
        deRequest.getUniversalUserId(), Channel.TELEPHONY);

    if (!ObjectUtils.isEmpty(deResponse)) {
      String botreply = "";
      if (!CollectionUtils.isEmpty(deResponse.getBotReply().getVoice())) {
        StringBuilder textBuilder = new StringBuilder();
        deResponse.getBotReply().getVoice().stream()
            .forEach(text -> textBuilder.append(text.trim()).append(System.lineSeparator()));
        botreply = textBuilder.toString();
      }

      if (botreply.endsWith(".")) {
        // remove the last character
        botreply = botreply.substring(0, botreply.length() - 1);
      }

      StringBuilder messageBuilder = new StringBuilder();
      String message = messageBuilder.append(botreply).append(System.lineSeparator()).toString()
          .replace("'", "").replace("&", "and").replace("`", "").replace("’", "");
      List<String> asrlist = deResponse.getSuggestASR();

      String language = getLanguage(deRequest.getLanguage());
      String ntts =
          Constants.TelephonyConstants.NuralVoice.getOrDefault(params.get("ntts"), "Polly.Joanna");

      DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      docFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      Document doc = docBuilder.newDocument();
      doc.setXmlStandalone(true);

      // create Response Node
      Element responseNode = doc.createElement("Response");
      doc.appendChild(responseNode);


      // update the language from deRequest
      UriComponentsBuilder uriComponentsBuilder =
          UriComponentsBuilder.fromHttpUrl(params.get("uri"));
      uriComponentsBuilder.replaceQueryParam(LANGUAGE, deRequest.getLanguage());

      if (language.equals("hi-IN")) {
        uriComponentsBuilder.replaceQueryParam("ntts", "Polly.Aditi");
        ntts = "Polly.Aditi";
      }
      // create Gather Node
      Element gatherNode = doc.createElement("Gather");
      gatherNode.setAttribute("action", uriComponentsBuilder.build().toUriString());
      gatherNode.setAttribute("actionOnEmptyResult", "true");
      gatherNode.setAttribute("input", "speech");
      gatherNode.setAttribute(LANGUAGE, language);
      gatherNode.setAttribute(SPEECH_MODEL, params.get(SPEECH_MODEL));
      gatherNode.setAttribute("speechTimeout", "auto");
      if (params.get(SPEECH_MODEL).equals("phone_call")) {
        gatherNode.setAttribute("enhanced", "true");
      }

      if (!CollectionUtils.isEmpty(asrlist)) {
        gatherNode.setAttribute("hints",
            asrlist.stream().collect(Collectors.joining(", ")).replace("-", " ").replace("'", "")
                .replace("&", "and").replace("`", "").replace("’", ""));
      }

      // create the Say Node with ssml tags if present
      NodeList sayNodeList =
          getSayNodes(docBuilder, message, language, ntts).getDocumentElement().getChildNodes();

      if (!ObjectUtils.isEmpty(deResponse.getIsDialogExit())
          && Boolean.TRUE.equals(deResponse.getIsDialogExit())) {
        setSayNodes(doc, responseNode, sayNodeList);
        responseNode.appendChild(doc.createElement("Hangup"));
        // publish to abandoned response to analytics during hangup
        analyticsStreamPublisher.publishAbandonedResponsesAnalytics(deRequest.getProjectId(),
            Channel.TELEPHONY.getName(), deRequest.getUserId(), deRequest.getUniversalUserId(),
            session.getSessionId(), session.isInsideForm(), session.isInsideQuiz());
      } else {
        if (Boolean.FALSE.equals(Boolean.valueOf(params.get("bargeIn")))) {
          setSayNodes(doc, responseNode, sayNodeList);
          responseNode.appendChild(gatherNode);
        } else {
          responseNode.appendChild(gatherNode);
          setSayNodes(doc, gatherNode, sayNodeList);
        }
      }

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

      Transformer transformer = transformerFactory.newTransformer();
      StringWriter writer = new StringWriter();
      transformer.transform(new DOMSource(doc), new StreamResult(writer));

      String response = writer.toString();
      writer.close();
      log.info("Twilio voice event response generated successfully");
      BotTelephonyTwilioResponse botTelephonyTwilioResponse = new BotTelephonyTwilioResponse();
      botTelephonyTwilioResponse.setXmlResponse(response);
      return botTelephonyTwilioResponse;
    }
    log.debug("Botresponse from DE is empty");
    return null;
  }


  private void setSayNodes(Document doc, Element responseNode, NodeList sayNodeList) {
    for (int itr = 0; itr < sayNodeList.getLength(); itr++) {
      Node node = doc.importNode(sayNodeList.item(itr), true);
      responseNode.appendChild(node);
    }
  }


  private Document getSayNodes(DocumentBuilder docBuilder, String message, String language,
      String ntts) throws SAXException, IOException {
    // if message starts with <Say> parse the xml string and update each say node
    if (message.toLowerCase().startsWith("<say")) {
      // add a dummy root node to the message string in order to parse the xml
      Document doc = docBuilder
          .parse(new InputSource(new StringReader(String.format(DUMMY_XML_FORMAT, message))));
      NodeList nodeList = doc.getElementsByTagName("Say");
      for (int itr = 0; itr < nodeList.getLength(); itr++) {
        Element say = (Element) nodeList.item(itr);
        if (ObjectUtils.isEmpty(say.getAttribute(LANGUAGE))) {
          say.setAttribute(LANGUAGE, language);
        }
        if (ObjectUtils.isEmpty(say.getAttribute(VOICE))) {
          say.setAttribute(VOICE, ntts);
        }
      }
      return doc;
    } else {
      // else create a say node with the given message
      Document doc = docBuilder.parse(new InputSource(new StringReader(
          String.format(DUMMY_XML_FORMAT, String.format(SAY_XML_FORMAT, message)))));
      Element say = (Element) doc.getDocumentElement().getFirstChild();
      if (ObjectUtils.isEmpty(say.getAttribute(LANGUAGE))) {
        say.setAttribute(LANGUAGE, language);
      }
      if (ObjectUtils.isEmpty(say.getAttribute(VOICE))) {
        say.setAttribute(VOICE, ntts);
      }

      return doc;
    }
  }

  private String getLanguage(String language) {
    if (StringUtils.hasText(language)) {
      if (Constants.TelephonyConstants.languageSet.contains(language)) {
        return language;
      } else {
        return "en-US";
      }
    } else {
      return "en-US";
    }
  }
}
