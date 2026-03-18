package ai.concerto.event.notification.supplier;

import ai.concerto.event.enums.Channel;
import ai.concerto.event.notification.handler.*;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChannelNotificationHandlerSupplier {

  @Autowired
  WhatsAppNotificationHandler whatsAppNotificationHandler;
  @Autowired
  SmsNotificationHandler smsNotificationHandler;
  @Autowired
  ChatBotNotificationHandler chatBotNotificationHandler;
  @Autowired
  FacebookNotificationHandler facebookNotificationHandler;
  @Autowired
  SlackNotificationHandler slackNotificationHandler;
  @Autowired
  GoogleBusinessMessageNotificationHandler googleBusinessMessageNotificationHandler;


  private final Supplier<ChannelNotificationHandler> WHATSAPP = () -> whatsAppNotificationHandler;
  private final Supplier<ChannelNotificationHandler> SMS = () -> smsNotificationHandler;
  private final Supplier<ChannelNotificationHandler> CHATBOT = () -> chatBotNotificationHandler;
  private final Supplier<ChannelNotificationHandler> FACEBOOK = () -> facebookNotificationHandler;
  private final Supplier<ChannelNotificationHandler> SLACK = () -> slackNotificationHandler;
  private final Supplier<ChannelNotificationHandler> GOOGLE_BUSINESS_MESSAGE =
      () -> googleBusinessMessageNotificationHandler;

  private final Map<Channel, Supplier<ChannelNotificationHandler>> channelHandlerMap =
      new EnumMap<>(Channel.class);

  public void setMapContent() {
    channelHandlerMap.put(Channel.WHATSAPP, WHATSAPP);
    channelHandlerMap.put(Channel.SMS, SMS);
    channelHandlerMap.put(Channel.CHATBOT, CHATBOT);
    channelHandlerMap.put(Channel.FACEBOOK, FACEBOOK);
    channelHandlerMap.put(Channel.SLACK, SLACK);
    channelHandlerMap.put(Channel.GOOGLE_BUSINESS_MESSAGE, GOOGLE_BUSINESS_MESSAGE);
  }

  public ChannelNotificationHandler getChannelTimeOutHandler(Channel channel) {
    setMapContent();
    return channelHandlerMap.get(channel).get();
  }
}
