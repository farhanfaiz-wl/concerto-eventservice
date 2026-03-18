package ai.concerto.event.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
  USER_MESSAGE("user_message"),
  BOT_TURN("bot_turn"),
  CONTROL_CHANGE("control_change");

  @JsonValue private String name;

  EventType(String name) {
    this.name = name;
  }
}
