package ai.concerto.event.notification.supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ai.concerto.event.enums.Channel;
import ai.concerto.event.notification.handler.*;
import javax.annotation.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ChannelNotificationHandlerSupplierTest {
  @InjectMocks
  @Resource
  ChannelNotificationHandlerSupplier channelNotificationHandlerSupplier;

  @Mock
  WhatsAppNotificationHandler whatsAppTimeOutHandler;
  @Mock
  SmsNotificationHandler smsTimeOutHandler;
  @Mock
  ChatBotNotificationHandler chatBotTimeOutHandler;
  @Mock
  FacebookNotificationHandler facebookTimeOutHandler;
  @Mock
  SlackNotificationHandler slackTimeOutHandler;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void getWhatsAppTimeOutHandler() {
    assertEquals(
        channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.WHATSAPP).getClass(),
        whatsAppTimeOutHandler.getClass());
  }

  @Test
  void getSmsTimeOutHandler() {
    assertEquals(
        channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.SMS).getClass(),
        smsTimeOutHandler.getClass());
  }

  @Test
  void getFacebookTimeOutHandler() {
    assertEquals(
        channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.FACEBOOK).getClass(),
        facebookTimeOutHandler.getClass());
  }

  @Test
  void getSlackTimeOutHandler() {
    assertEquals(
        channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.SLACK).getClass(),
        slackTimeOutHandler.getClass());
  }

  @Test
  void getChatBotHandler() {
    assertEquals(
        channelNotificationHandlerSupplier.getChannelTimeOutHandler(Channel.CHATBOT).getClass(),
        chatBotTimeOutHandler.getClass());
  }

}
