package ai.concerto.event.service;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.exception.RequestHandlerException;
import ai.concerto.event.exchange.*;
import ai.concerto.event.exchange.ApplicationIntegration.ServiceProvider;
import ai.concerto.event.exchange.ApplicationIntegration.SlackIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.SmsIntegration;
import ai.concerto.event.exchange.ApplicationIntegration.WhatsappIntegration;
import ai.concerto.event.handler.BotResponseHandler;
import ai.concerto.event.handler.BotResponseHandlerFactory;
import ai.concerto.event.utils.PhoneUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Slf4j
@Service
public class PushMessageService {

  @Autowired
  private BotResponseHandlerFactory botResponseHandlerFactory;

  @Autowired
  private IntegrationService integrationService;

  @Autowired
  private FalconService falconService;

  @Autowired
  private ObjectMapper mapper;

  @Value("${twilio.accountSid}")
  private String accountSid;

  @Value("${twilio.authToken}")
  private String authToken;

  @Value("${twilio.phoneNumber}")
  private String phoneNumber;

  @SneakyThrows
  public MessageInfo pushMessageOnChannel(PushMessage pushMessage) {
    Channel channel = Channel.valueOf(pushMessage.getChannel().toUpperCase());

    try {
      ApplicationIntegration integration =
          integrationService.getApplicationIntegration(pushMessage.getProjectId());
      // standardize phonenumber with country code
      Object countryCode = integration.getProjectSettings().get("user_phone_number_country_code");
      if (ObjectUtils.isEmpty(countryCode)) {
        countryCode = "+1";
      }
      if (!ObjectUtils.isEmpty(pushMessage.getTo())) {
        pushMessage.setTo(PhoneUtils.standardizedPhoneNumberWithCountryCode(pushMessage.getTo(),
            countryCode.toString()));
      }

      Object response = getBotResponseFromPushMessage(integration, pushMessage, channel);
      log.info("Message recieved for push message {}", mapper.writeValueAsString(response));

      BotResponseHandler botResponseHandler =
          botResponseHandlerFactory.getBotResponseHandler(channel, pushMessage.getVendor());
      log.info("Message got for push message {}", mapper.writeValueAsString(response));
      if (!ObjectUtils.isEmpty(response)) {
        Object message = botResponseHandler.postBotResponse(response);
        return botResponseHandler.getMessageInfo(message);
      }
    } catch (Exception e) {
      log.error(String.format("Unable to send agent message on channel %s due to %s",
          pushMessage.getChannel(), e.getMessage()));
      if (Channel.SMS.equals(channel)) {
        Project project = falconService.getProjectById(pushMessage.getProjectId());
        log.error(String.format("Agent SMS failure -> %s/%s -> %s", project.getName(),
            pushMessage.getTicketId(), e.getMessage()));
      }
    }
    return new MessageInfo();
  }

  private Object getBotResponseFromPushMessage(ApplicationIntegration integration,
      PushMessage pushMessage, Channel channel) {

    switch (channel) {
      case WHATSAPP:
        BotWhatsappResponse botWhatsappResponse = new BotWhatsappResponse();
        botWhatsappResponse.setProjectId(pushMessage.getProjectId());
        botWhatsappResponse.setBody(pushMessage.getMessage());
        botWhatsappResponse.setTo(pushMessage.getTo());

        if (Boolean.TRUE.equals(pushMessage.getIsNotification())) {
          botWhatsappResponse.setAuthId(accountSid);
          botWhatsappResponse.setAuthToken(authToken);
          botWhatsappResponse.setFrom(phoneNumber);
          pushMessage.setVendor("TWILIO");
        } else {
          WhatsappIntegration whatsappIntegration = (WhatsappIntegration) integrationService
              .getChannelIntegration(pushMessage.getProjectId(), channel, integration);

          Optional<ServiceProvider> serviceProvider =
              whatsappIntegration.getServiceProvidersList().stream().findFirst();
          if (!serviceProvider.isPresent())
            throw new RequestHandlerException(String
                .format("Whatsapp provider not found for project %s!", pushMessage.getProjectId()));

          if (ObjectUtils.isEmpty(pushMessage.getVendor())) {
            pushMessage.setVendor(serviceProvider.get().getMessageServiceProvider().name());
          }
          botWhatsappResponse.setAuthId(serviceProvider.get().getProviderDetails().getAccountId());
          botWhatsappResponse
              .setAuthToken(serviceProvider.get().getProviderDetails().getAuthToken());
          botWhatsappResponse.setFrom(whatsappIntegration.getPhoneNumber());
        }
        return botWhatsappResponse;
      case HTML5:
        BotChatBotResponse botChatBotResponse = new BotChatBotResponse();
        botChatBotResponse.setBotReplies(Arrays.asList(pushMessage.getMessage()));
        botChatBotResponse.setUserId(pushMessage.getUserId());
        botChatBotResponse.setProjectId(pushMessage.getProjectId());
        botChatBotResponse.setSessionId(pushMessage.getSessionId());
        botChatBotResponse.setTurnId(pushMessage.getTurnId());
        return botChatBotResponse;
      case SMS:
        BotSmsResponse botSmsResponse = new BotSmsResponse();
        botSmsResponse.setProjectId(pushMessage.getProjectId());
        botSmsResponse.setTo(pushMessage.getTo());
        botSmsResponse.setMessage(pushMessage.getMessage());
        botSmsResponse.setTicketId(pushMessage.getTicketId());
        botSmsResponse.setIsAgentMessage(true);

        if (Boolean.TRUE.equals(pushMessage.getIsNotification())) {
          botSmsResponse.setAuthId(accountSid);
          botSmsResponse.setAuthToken(authToken);
          botSmsResponse.setFrom(phoneNumber);
          pushMessage.setVendor("TWILIO");
        } else {
          SmsIntegration smsIntegration = (SmsIntegration) integrationService
              .getChannelIntegration(pushMessage.getProjectId(), channel, integration);

          Optional<ServiceProvider> smsServiceProvider =
              smsIntegration.getServiceProvidersList().stream().findFirst();
          if (!smsServiceProvider.isPresent())
            throw new RequestHandlerException(String
                .format("Sms provider not found for project %s!", pushMessage.getProjectId()));

          if (ObjectUtils.isEmpty(pushMessage.getVendor())) {
            pushMessage.setVendor(smsServiceProvider.get().getMessageServiceProvider().name());
          }
          botSmsResponse.setAuthId(smsServiceProvider.get().getProviderDetails().getAccountId());
          botSmsResponse.setAuthToken(smsServiceProvider.get().getProviderDetails().getAuthToken());
          botSmsResponse.setFrom(smsIntegration.getPhoneNumber());
        }
        return botSmsResponse;
      case SLACK:
        BotSlackResponse botSlackResponse = new BotSlackResponse();
        SlackIntegration slackIntegration = (SlackIntegration) integrationService
            .getChannelIntegration(pushMessage.getProjectId(), channel, integration);
        botSlackResponse.setProjectId(pushMessage.getProjectId());
        botSlackResponse.setChannel(pushMessage.getChannel());
        botSlackResponse.setText(pushMessage.getMessage());
        botSlackResponse.setToken(slackIntegration.getBotToken());
        return botSlackResponse;
      case FACEBOOK:
        BotFacebookResponse botFacebookResponse = new BotFacebookResponse();
        botFacebookResponse.getMessage().setText(pushMessage.getMessage());
        botFacebookResponse.getRecipient().setId(pushMessage.getUserId());
        botFacebookResponse.getMessage().setMetadata("DEVELOPER_DEFINED_METADATA");
        botFacebookResponse.setProjectId(pushMessage.getProjectId());
        return botFacebookResponse;
      case EMAIL:
        EmailResponse emailResponse = new EmailResponse();

        emailResponse.getResponse().setEmailBcc(pushMessage.getEmailBcc());
        emailResponse.getResponse().setEmailCc(pushMessage.getEmailCc());
        emailResponse.getResponse().setToEmails(pushMessage.getToEmails());
        emailResponse.setIsAsync(true);

        emailResponse.setProjectId(pushMessage.getProjectId());

        List<String> botReplies = new ArrayList<>();
        botReplies.add(pushMessage.getMessage());
        emailResponse.getResponse().setBotReplies(botReplies);
        emailResponse.getResponse().setSubject(pushMessage.getSubject());
        return emailResponse;

      default:
        return null;

    }
  }

}
