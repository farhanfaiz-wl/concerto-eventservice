package ai.concerto.event.handler;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.enums.Vendor;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.handler.request.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserRequestHandlerFactory {

  @Autowired
  private ChatBotRequestHandler chatBotRequestHandler;

  @Autowired
  private SlackRequestHandler slackRequestHandler;

  @Autowired
  private WhatsappKaleyraRequestHandler whatsappKaleyraRequestHandler;

  @Autowired
  private WhatsappRouteRequestHandler whatsappRouteRequestHandler;

  @Autowired
  private WhatsappTwilioRequestHandler whatsappTwilioRequestHandler;

  @Autowired
  private WhatsappVFRequesthandler whatsappVFRequesthandler;

  @Autowired
  private WhatsappCloudApiRequestHandler whatsappCloudApiRequestHandler;

  @Autowired
  private SmsTwilioRequestHandler smsTwilioRequestHandler;

  @Autowired
  private FacebookRequestHandler facebookRequestHandler;

  @Autowired
  private EmailRequestHandler emailRequestHandler;

  @Autowired
  private ApiRequestHandler apiRequestHandler;

  @Autowired
  private TelephonyTwilioRequestHandler telephonyTwilioRequestHandler;

  @Autowired
  private GoogleBusinessMessageRequestHandler googleBusinessMessageRequestHandler;

  public UserRequestHandler getUserRequestHandler(Channel channel, String vendor) {
    return switch (channel) {
      case HTML5, CHATBOT -> chatBotRequestHandler;
      case SLACK -> slackRequestHandler;
      case WHATSAPP -> switch (Vendor.valueOf(vendor)) {
        case KALEYRA -> whatsappKaleyraRequestHandler;
        case ROUTE -> whatsappRouteRequestHandler;
        case TWILIO -> whatsappTwilioRequestHandler;
        case VF -> whatsappVFRequesthandler;
        case CLOUD_API -> whatsappCloudApiRequestHandler;
        default -> throw new RequestHandlerException("No vendor Integration found.");
      };
      case TELEPHONY -> switch (Vendor.valueOf(vendor)) {
        case TWILIO -> telephonyTwilioRequestHandler;
        default -> throw new RequestHandlerException("No vendor Integration found.");
      };
      case SMS -> switch (Vendor.valueOf(vendor)) {
        case TWILIO -> smsTwilioRequestHandler;
        default -> throw new RequestHandlerException("No valid sms vendor integration");
      };
      case FACEBOOK -> facebookRequestHandler;
      case API -> apiRequestHandler;
      case EMAIL -> emailRequestHandler;
      case GOOGLE_BUSINESS_MESSAGE -> googleBusinessMessageRequestHandler;
      default -> throw new RequestHandlerException(
          String.format("No valid channel integration found for %s", channel.getName()));
    };
  }
}
