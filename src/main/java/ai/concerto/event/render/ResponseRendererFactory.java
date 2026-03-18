package ai.concerto.event.render;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exception.ResponseHandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ResponseRendererFactory {

  @Autowired
  private ChatBotRenderer chatBotRenderer;

  @Autowired
  private SlackRenderer slackRenderer;

  @Autowired
  private WhatsappKaleyraRenderer whatsappKaleyraRenderer;

  @Autowired
  private WhatsappRouteRenderer whatsappRouteRenderer;

  @Autowired
  private WhatsappTwilioRenderer whatsappTwilioRenderer;

  @Autowired
  private WhatsappVFRenderer whatsappVFRenderer;

  @Autowired
  private WhatsappCloudApiRenderer whatsappCloudApiRenderer;

  @Autowired
  private SmsTwilioRenderer smsTwilioRenderer;

  @Autowired
  private FacebookRenderer facebookRenderer;

  @Autowired
  private AlexaRenderer alexaRenderer;

  @Autowired
  private EmailRenderer emailRenderer;

  @Autowired
  private ApiRenderer apiRenderer;

  @Autowired
  private TelephonyTwilioRenderer telephonyTwilioRenderer;

  @Autowired
  private GoogleBusinessMessageRenderer googleBusinessMessageRenderer;

  public ResponseRenderer getResponseRenderer(Channel channel, String vendor) {
    return switch (channel) {
      case CHATBOT -> chatBotRenderer;
      case SLACK -> slackRenderer;
      case WHATSAPP -> switch (Vendor.valueOf(vendor)) {
        case KALEYRA -> whatsappKaleyraRenderer;
        case ROUTE -> whatsappRouteRenderer;
        case TWILIO -> whatsappTwilioRenderer;
        case VF -> whatsappVFRenderer;
        case CLOUD_API -> whatsappCloudApiRenderer;
        default -> null;
      };
      case TELEPHONY -> switch (Vendor.valueOf(vendor)) {
        case TWILIO -> telephonyTwilioRenderer;
        default -> throw new ResponseHandlerException("No vendor Integration found.");
      };
      case SMS -> switch (Vendor.valueOf(vendor)) {
        case TWILIO -> smsTwilioRenderer;
        default -> null;
      };
      case FACEBOOK -> facebookRenderer;
      case AMAZON -> alexaRenderer;
      case API -> apiRenderer;
      case EMAIL -> emailRenderer;
      case GOOGLE_BUSINESS_MESSAGE -> googleBusinessMessageRenderer;
      default -> throw new ResponseHandlerException("No active channel integration found");
    };
  }
}
