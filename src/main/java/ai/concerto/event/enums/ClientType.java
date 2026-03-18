package ai.concerto.event.enums;

public enum ClientType {
  RICH_TEXT("rich_text"),
  NON_CONVERSATIONAL("non_conversational"),
  MESSAGING("messaging"),
  VOICE("voice"),
  SMART_SCREEN("smart_screen");

  private final String name;

  ClientType(String value) {
    name = value;
  }

  public String getName() {
    return name;
  }
}
