package ai.concerto.event.render;

import java.io.IOException;
import java.io.StringWriter;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TelephonyOzonetelRenderer {

  public String render(String botReply, String lang, String speed, String type, String timeout,
      String speechCompleteTimeout, String speechIncompleteTimeout, boolean isDialogExit)
      throws ParserConfigurationException, TransformerException, IOException {

    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

    Document doc = docBuilder.newDocument();
    doc.setXmlStandalone(true);
    Element rootElement = doc.createElement("response");
    doc.appendChild(rootElement);

    Element playtext = doc.createElement("playtext");
    rootElement.appendChild(playtext);
    playtext.setAttribute("lang", lang);
    playtext.setAttribute("speed", "3");
    playtext.setAttribute("quality", "best");
    playtext.setAttribute("bargein", "false");
    playtext.setAttribute("type", "ggl");
    botReply = botReply.replace("'", "").replace("&", "and").replace("-", " ").replace("`", "")
        .replace("’", "");
    playtext.setTextContent(botReply);

    if (!isDialogExit) {
      Element recognize = doc.createElement("recognize");
      rootElement.appendChild(recognize);
      recognize.setAttribute("type", "ggl-stream");
      if (StringUtils.hasText(timeout)) {
        recognize.setAttribute("timeout", timeout);
      }
      recognize.setAttribute("lang", lang);
      recognize.setAttribute("grammar", "recogmo");
      if (StringUtils.hasText(speechCompleteTimeout)) {
        recognize.setAttribute("speech_complete_timeout", speechCompleteTimeout);
      }
      if (StringUtils.hasText(speechIncompleteTimeout)) {
        recognize.setAttribute("speech_incomplete_timeout", speechIncompleteTimeout);
      }
    } else {
      rootElement.appendChild(doc.createElement("hangup"));
    }

    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
    transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

    Transformer transformer = transformerFactory.newTransformer();
    StringWriter writer = new StringWriter();
    transformer.transform(new DOMSource(doc), new StreamResult(writer));

    String response = writer.toString();
    writer.close();
    log.debug("Response to ozonetel {}", response);
    return response;
  }

}
