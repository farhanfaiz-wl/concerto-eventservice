package ai.concerto.event.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Channel {
  AMAZON("amazon"),
  FACEBOOK("facebook"),
  CHATBOT("chatbot"),
  HTML5("html5"),
  EMAIL("email"),
  WEBSITE("website"),
  MOBILE("mobile"),
  WIDGET("widget"),
  BOOKING_WIDGET("booking_widget"),
  GOOGLE("google"),
  SMS("sms"),
  SEARCH_API("search_api"),
  WHATSAPP("whatsapp"),
  SLACK("slack"),
  GOOGLE_BUSINESS_MESSAGE("google_business_message"),
  API("api"),
  TELEPHONY("telephony");

  @JsonValue
  private String name;

  Channel(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
