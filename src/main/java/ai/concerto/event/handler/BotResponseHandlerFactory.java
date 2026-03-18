package ai.concerto.event.handler;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exception.ResponseHandlerException;
import ai.concerto.event.handler.response.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BotResponseHandlerFactory {

  @Autowired
  private ChatBotResponseHandler chatBotResponseHandler;

  @Autowired
  private SlackResponseHandler slackResponseHandler;

  @Autowired
  private WhatsappKaleyraResponseHandler whatsappKaleyraResponseHandler;

  @Autowired
  private WhatsappRouteResponseHandler whatsappRouteResponseHandler;

  @Autowired
  private WhatsappTwilioResponseHandler whatsappTwilioResponseHandler;

  @Autowired
  private WhatsappVFResponseHandler whatsappVFResponseHandler;

  @Autowired
  private WhatsappCloudApiResponseHandler whatsappCloudApiResponseHandler;

  @Autowired
  private SmsTwilioResponseHandler smsTwilioResponseHandler;

  @Autowired
  private FacebookResponseHandler facebookResponseHandler;

  @Autowired
  private EmailResponseHandler emailResponseHandler;

  @Autowired
  private GoogleBusinessMessageResponseHandler googleBusinessMessageResponseHandler;

  public BotResponseHandler getBotResponseHandler(Channel channel, String vendor) {
    return switch (channel) {
      case HTML5, CHATBOT -> chatBotResponseHandler;
      case SLACK -> slackResponseHandler;
      case WHATSAPP -> switch (Vendor.valueOf(vendor)) {
        case KALEYRA -> whatsappKaleyraResponseHandler;
        case ROUTE -> whatsappRouteResponseHandler;
        case TWILIO -> whatsappTwilioResponseHandler;
        case VF -> whatsappVFResponseHandler;
        case CLOUD_API -> whatsappCloudApiResponseHandler;
        default -> throw new ResponseHandlerException("No valid whatsapp vendor integration");
      };
      case SMS -> switch (Vendor.valueOf(vendor)) {
        case TWILIO -> smsTwilioResponseHandler;
        default -> throw new ResponseHandlerException("No valid sms vendor integration");
      };
      case FACEBOOK -> facebookResponseHandler;
      case EMAIL -> emailResponseHandler;
      case GOOGLE_BUSINESS_MESSAGE -> googleBusinessMessageResponseHandler;
      default -> throw new ResponseHandlerException("No valid channel integration found.");
    };
  }
}
