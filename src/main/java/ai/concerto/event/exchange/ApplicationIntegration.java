package ai.concerto.event.exchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.util.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import ai.concerto.event.enums.SourceType;
import ai.concerto.event.enums.Vendor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ApplicationIntegration {

  private AmazonIntegration amazonIntegration;
  private FacebookIntegration facebookIntegration;
  private EmailIntegration emailIntegration;
  private ChatbotIntegration chatbotIntegration;
  private WebsiteIntegration websiteIntegration;
  private GoogleHomeIntegration googleHomeIntegration;
  private WidgetIntegration widgetIntegration;
  private WhatsappIntegration whatsappIntegration;
  private SmsIntegration smsIntegration;
  private SlackIntegration slackIntegration;
  private BookingWidgetIntegration bookingWidgetIntegration;
  private GoogleBusinessMessageIntegration googleBusinessIntegration;
  private Map<String, Object> projectSettings;
  private String template;
  private String tenantId;

  public enum WidgetTypes {
    QA, QAAndSearch, Promotions, QuickLinks
  }

  public enum ChatPromotionMediaType {
    VIDEO, IMAGE
  }

  public enum ChatMenuType {
    EXTERNAL_LINKS, PRIVACY, POST_BACK, SEARCH
  }

  public enum StructuredDataSetSchemaType {
    PEOPLE
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ChannelIntegration {
    private Integer sessionTimeout;
    private String sessionTimeoutPrompt;
    protected Boolean showSearchResult;
    protected Boolean enableQA;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class AmazonIntegration extends ChannelIntegration {
    private String alexaSkillId;
    private String alexaSkillName;
    private Boolean recommendationEnabled;
    private Boolean sendEmail;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FacebookIntegration extends MessageIntegration {
    private String pageAccessToken;
    private String pageName;
    private String secondaryAppId;
    private Integer timeoutDurationInHrs;
    private Boolean enableLiveAgentTimeout;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class EmailIntegration extends ChannelIntegration {
    private String userName;
    private String password;
    private String imapUrl;
    private String smtpUrl;
    private String smtpPort;
    private String smtpUserName;
    private String smtpPassword;
    private String headerLogoUrl;
    private String footerLogoUrl;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ChatbotIntegration extends ChannelIntegration {

    private String logoUrl;
    private String chatIconUrl;
    private String iconColor;
    private String primaryColor;
    private String secondaryColor;
    private String headerColor;
    private String chatClientName;
    private Boolean dedicatedHomePage;
    private Boolean isknowledgeBasePresent;
    private Boolean showSearchInChat;
    private String searchMessage;
    private String language;
    private Boolean collectPersonalDetails;
    private Boolean showDisclaimer;
    private String disclaimerText;
    private String fontStyle;
    private String privacyPolicyLink;
    private String templateName;
    private String switchAppLabel;
    private String welcomePrompt;
    private List<ChatMenu> menus;
    private List<ChatPromotion> promotions;
    private List<ChatBotComponentOrder> chatBotComponentOrders;
    private List<StructuredDataSetSchema> structuredDataSetSchemas;
    private Map<String, String> voiceSetting;
    private Boolean hasMultipleApplication;
    private String multiApplicationLabel;
    private List<Map<String, String>> linkedApplications;
    private JsonNode selectors;
    private JsonNode entity;
    private String chatPrompt;
    private String chatPromptTimeout;
    private String chatPromptBorder;
    private String chatPromptBackground;
    private String askPrompt;
    private String askPromptColor;
    private ChatIcon chatIcon;
    private Boolean isMicEnabled;
    private Boolean speakBotResponse;
    private Boolean isLogRocketEnabled;
    private Boolean typingAnimationEnabled;
    private Boolean bubbleDelayEnabled;
    private String projectTemplate;
    private ChatPreIntegration chatBotPreIntegration;
    private ChatbotIntegrationLocationSearch locationSearch;
    private Boolean homeEnabled;
    private Boolean promotionsEnabled;
    private Boolean menuEnabled;
    private Boolean faqEnabled;
    private Boolean quickLinksEnabled;
    private Boolean quickRepliesEnabled;
    private String defaultPageOnLoad;
    private ChatBotDefaultValueForm chatbotDefaultValue;
    private Boolean isLlmProject;
  }


  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatbotIntegrationForUI {
    @JsonProperty("logoUrl")
    private String logoUrl;
    @JsonProperty("chatIconUrl")
    private String chatIconUrl;
    @JsonProperty("iconColor")
    private String iconColor;
    @JsonProperty("primaryColor")
    private String primaryColor;
    @JsonProperty("secondaryColor")
    private String secondaryColor;
    @JsonProperty("headerColor")
    private String headerColor;
    @JsonProperty("chatClientName")
    private String chatClientName;
    @JsonProperty("dedicatedHomePage")
    private Boolean dedicatedHomePage;
    @JsonProperty("isknowledgeBasePresent")
    private Boolean isknowledgeBasePresent;
    @JsonProperty("showSearchInChat")
    private Boolean showSearchInChat;
    @JsonProperty("searchMessage")
    private String searchMessage;
    @JsonProperty("language")
    private String language;
    @JsonProperty("collectPersonalDetails")
    private Boolean collectPersonalDetails;
    @JsonProperty("showDisclaimer")
    private Boolean showDisclaimer;
    @JsonProperty("disclaimerText")
    private String disclaimerText;
    @JsonProperty("fontStyle")
    private String fontStyle;
    @JsonProperty("privacyPolicyLink")
    private String privacyPolicyLink;
    @JsonProperty("templateName")
    private String templateName;
    @JsonProperty("switchAppLabel")
    private String switchAppLabel;
    @JsonProperty("welcomePrompt")
    private String welcomePrompt;
    @JsonProperty("menus")
    private List<ChatMenuForUI> menus;
    @JsonProperty("promotions")
    private List<ChatPromotion> promotions;
    @JsonProperty("chatBotComponentOrders")
    private List<ChatBotComponentOrder> chatBotComponentOrders;
    @JsonProperty("structuredDataSetSchemas")
    private List<StructuredDataSetSchema> structuredDataSetSchemas;
    @JsonProperty("voiceSetting")
    private Map<String, String> voiceSetting;
    @JsonProperty("hasMultipleApplication")
    private Boolean hasMultipleApplication;
    @JsonProperty("multiApplicationLabel")
    private String multiApplicationLabel;
    @JsonProperty("linkedApplications")
    private List<Map<String, String>> linkedApplications;
    @JsonProperty("selectors")
    private JsonNode selectors;
    @JsonProperty("entity")
    private JsonNode entity;
    @JsonProperty("sessionTimeout")
    private Integer sessionTimeout;
    @JsonProperty("sessionTimeoutPrompt")
    private String sessionTimeoutPrompt;
    @JsonProperty("chatPrompt")
    private String chatPrompt;
    @JsonProperty("chatPromptTimeout")
    private String chatPromptTimeout;
    @JsonProperty("chatPromptBorder")
    private String chatPromptBorder;
    @JsonProperty("chatPromptBackground")
    private String chatPromptBackground;
    @JsonProperty("askPrompt")
    private String askPrompt;
    @JsonProperty("askPromptColor")
    private String askPromptColor;
    @JsonProperty("chatIcon")
    private ChatIcon chatIcon;
    @JsonProperty("isMicEnabled")
    private Boolean isMicEnabled;
    @JsonProperty("speakBotResponse")
    private Boolean speakBotResponse;
    @JsonProperty("isLogRocketEnabled")
    private Boolean isLogRocketEnabled;
    @JsonProperty("typingAnimationEnabled")
    private Boolean typingAnimationEnabled;
    @JsonProperty("bubbleDelayEnabled")
    private Boolean bubbleDelayEnabled;
    @JsonProperty("projectTemplate")
    private String projectTemplate;
    @JsonProperty("chatBotPreIntegration")
    private ChatPreIntegration chatBotPreIntegration;
    @JsonProperty("locationSearch")
    private ChatbotIntegrationLocationSearch locationSearch;
    @JsonProperty("homeEnabled")
    private Boolean homeEnabled;
    @JsonProperty("promotionsEnabled")
    private Boolean promotionsEnabled;
    @JsonProperty("menuEnabled")
    private Boolean menuEnabled;
    @JsonProperty("faqEnabled")
    private Boolean faqEnabled;
    @JsonProperty("quickLinksEnabled")
    private Boolean quickLinksEnabled;
    @JsonProperty("quickRepliesEnabled")
    private Boolean quickRepliesEnabled;
    @JsonProperty("defaultPageOnLoad")
    private String defaultPageOnLoad;
    @JsonProperty("chatbotDefaultValue")
    private ChatBotDefaultValueForm chatbotDefaultValue;
    @JsonProperty("showSearchResult")
    private Boolean showSearchResult;
    @JsonProperty("enableQA")
    private Boolean enableQA;
    @JsonProperty("isLlmProject")
    private Boolean isLlmProject;

    public static ChatbotIntegrationForUI from(ChatbotIntegration chatbotIntegration) {

      ChatbotIntegrationForUI chatbotIntegrationForUI = new ChatbotIntegrationForUI();
      chatbotIntegrationForUI.setLogoUrl(chatbotIntegration.getLogoUrl());
      chatbotIntegrationForUI.setChatIcon(chatbotIntegration.getChatIcon());
      chatbotIntegrationForUI.setChatIconUrl(chatbotIntegration.getChatIconUrl());
      chatbotIntegrationForUI.setIconColor(chatbotIntegration.getIconColor());
      chatbotIntegrationForUI.setPrimaryColor(chatbotIntegration.getPrimaryColor());
      chatbotIntegrationForUI.setSecondaryColor(chatbotIntegration.getSecondaryColor());
      chatbotIntegrationForUI.setHeaderColor(chatbotIntegration.getHeaderColor());
      chatbotIntegrationForUI.setChatClientName(chatbotIntegration.getChatClientName());
      chatbotIntegrationForUI.setDedicatedHomePage(chatbotIntegration.getDedicatedHomePage());
      chatbotIntegrationForUI
          .setIsknowledgeBasePresent(chatbotIntegration.getIsknowledgeBasePresent());
      chatbotIntegrationForUI.setShowSearchInChat(chatbotIntegration.getShowSearchInChat());
      chatbotIntegrationForUI.setSearchMessage(chatbotIntegration.getSearchMessage());
      chatbotIntegrationForUI.setLanguage(chatbotIntegration.getLanguage());
      chatbotIntegrationForUI
          .setCollectPersonalDetails(chatbotIntegration.getCollectPersonalDetails());
      chatbotIntegrationForUI.setShowDisclaimer(chatbotIntegration.getShowDisclaimer());
      chatbotIntegrationForUI.setDisclaimerText(chatbotIntegration.getDisclaimerText());
      chatbotIntegrationForUI.setFontStyle(chatbotIntegration.getFontStyle());
      chatbotIntegrationForUI.setPrivacyPolicyLink(chatbotIntegration.getPrivacyPolicyLink());
      chatbotIntegrationForUI.setTemplateName(chatbotIntegration.getTemplateName());
      chatbotIntegrationForUI.setSwitchAppLabel(chatbotIntegration.getSwitchAppLabel());
      chatbotIntegrationForUI.setWelcomePrompt(chatbotIntegration.getWelcomePrompt());
      if (!ObjectUtils.isEmpty(chatbotIntegration.getMenus())) {
        chatbotIntegrationForUI.setMenus(fromChatMenu(chatbotIntegration.getMenus()));
      }
      chatbotIntegrationForUI.setPromotions(chatbotIntegration.getPromotions());
      chatbotIntegrationForUI
          .setChatBotComponentOrders(chatbotIntegration.getChatBotComponentOrders());
      chatbotIntegrationForUI
          .setStructuredDataSetSchemas(chatbotIntegration.getStructuredDataSetSchemas());
      chatbotIntegrationForUI.setVoiceSetting(chatbotIntegration.getVoiceSetting());
      chatbotIntegrationForUI
          .setHasMultipleApplication(chatbotIntegration.getHasMultipleApplication());
      chatbotIntegrationForUI
          .setMultiApplicationLabel(chatbotIntegration.getMultiApplicationLabel());
      chatbotIntegrationForUI.setLinkedApplications(chatbotIntegration.getLinkedApplications());
      chatbotIntegrationForUI.setSelectors(chatbotIntegration.getSelectors());
      chatbotIntegrationForUI.setEntity(chatbotIntegration.getEntity());
      chatbotIntegrationForUI.setSessionTimeout(chatbotIntegration.getSessionTimeout());
      chatbotIntegrationForUI.setSessionTimeoutPrompt(chatbotIntegration.getSessionTimeoutPrompt());
      chatbotIntegrationForUI.setChatPrompt(chatbotIntegration.getChatPrompt());
      chatbotIntegrationForUI.setChatPromptTimeout(chatbotIntegration.getChatPromptTimeout());
      chatbotIntegrationForUI.setChatPromptBorder(chatbotIntegration.getChatPromptBorder());
      chatbotIntegrationForUI.setChatPromptBackground(chatbotIntegration.getChatPromptBackground());
      chatbotIntegrationForUI.setAskPrompt(chatbotIntegration.getAskPrompt());
      chatbotIntegrationForUI.setAskPromptColor(chatbotIntegration.getAskPrompt());
      chatbotIntegrationForUI.setIsMicEnabled(chatbotIntegration.getIsMicEnabled());
      chatbotIntegrationForUI.setSpeakBotResponse(chatbotIntegration.getSpeakBotResponse());
      chatbotIntegrationForUI.setIsLogRocketEnabled(chatbotIntegration.getIsLogRocketEnabled());
      chatbotIntegrationForUI
          .setTypingAnimationEnabled(chatbotIntegration.getTypingAnimationEnabled());
      chatbotIntegrationForUI.setBubbleDelayEnabled(chatbotIntegration.getBubbleDelayEnabled());
      chatbotIntegrationForUI.setProjectTemplate(chatbotIntegration.getProjectTemplate());
      chatbotIntegrationForUI.setLocationSearch(chatbotIntegration.getLocationSearch());
      chatbotIntegrationForUI.setHomeEnabled(chatbotIntegration.getHomeEnabled());
      chatbotIntegrationForUI.setPromotionsEnabled(chatbotIntegration.getPromotionsEnabled());
      chatbotIntegrationForUI.setMenuEnabled(chatbotIntegration.getMenuEnabled());
      chatbotIntegrationForUI.setFaqEnabled(chatbotIntegration.getFaqEnabled());
      chatbotIntegrationForUI.setQuickLinksEnabled(chatbotIntegration.getQuickLinksEnabled());
      chatbotIntegrationForUI.setQuickRepliesEnabled(chatbotIntegration.getQuickRepliesEnabled());
      chatbotIntegrationForUI.setDefaultPageOnLoad(chatbotIntegration.getDefaultPageOnLoad());
      chatbotIntegrationForUI.setShowSearchResult(chatbotIntegration.getShowSearchResult());
      chatbotIntegrationForUI.setEnableQA(chatbotIntegration.getEnableQA());
      chatbotIntegrationForUI.setChatbotDefaultValue(chatbotIntegration.getChatbotDefaultValue());
      chatbotIntegrationForUI
          .setChatBotPreIntegration(chatbotIntegration.getChatBotPreIntegration());
      chatbotIntegrationForUI.setIsLlmProject(chatbotIntegration.getIsLlmProject());
      return chatbotIntegrationForUI;
    }

    @Data
    @JsonInclude(Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatMenuForUI {

      @JsonProperty("index")
      private Integer index;
      @JsonProperty("masterProjectId")
      private String masterProjectId;
      @JsonProperty("title")
      private String title;
      @JsonProperty("iconUrl")
      private String iconUrl;
      @JsonProperty("type")
      private ChatMenuType type;
      @JsonProperty("action")
      private String action;
    }

    private static List<ChatMenuForUI> fromChatMenu(List<ChatMenu> chatMenus) {

      List<ChatMenuForUI> chatMenusForUI = new ArrayList<>();
      for (ChatMenu chatMenu : chatMenus) {
        ChatMenuForUI chatMenuForUI = new ChatMenuForUI();
        chatMenuForUI.setIndex(chatMenu.getIndex());
        chatMenuForUI.setAction(chatMenu.getAction());
        chatMenuForUI.setIconUrl(chatMenu.getIconUrl());
        chatMenuForUI.setMasterProjectId(chatMenu.getMasterProjectId());
        chatMenuForUI.setTitle(chatMenu.getTitle());
        chatMenuForUI.setType(chatMenu.getType());
        chatMenusForUI.add(chatMenuForUI);
      }
      return chatMenusForUI;
    }
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatBotDefaultValueForm {
    @JsonProperty("welcomeText")
    private String welcomeText;
    @JsonProperty("welcomeTextSecond")
    private String welcomeTextSecond;
    @JsonProperty("assistantName")
    private String assistantName;
    @JsonProperty("disclaimerText")
    private String disclaimerText;
    @JsonProperty("disclaimerTitle")
    private String disclaimerTitle;
    @JsonProperty("welcomeCTA")
    private String welcomeCTA;
    @JsonProperty("feedbackNotFound")
    private String feedbackNotFound;
    @JsonProperty("feedbackSorry")
    private String feedbackSorry;
    @JsonProperty("feedbackSorryDetails")
    private String feedbackSorryDetails;
    @JsonProperty("feedbackThanksRevert")
    private String feedbackThanksRevert;
    @JsonProperty("feedbackReceived")
    private String feedbackReceived;
    @JsonProperty("feedbackOption1")
    private String feedbackOption1;
    @JsonProperty("feedbackOption2")
    private String feedbackOption2;
    @JsonProperty("feedbackOption3")
    private String feedbackOption3;
    @JsonProperty("feedbackThanks")
    private String feedbackThanks;
    @JsonProperty("feedbackReason")
    private String feedbackReason;
    @JsonProperty("feedbackThanksApology")
    private String feedbackThanksApology;
    @JsonProperty("feedbackPrompt")
    private String feedbackPrompt;
    @JsonProperty("inputComments")
    private String inputComments;
    @JsonProperty("inputQuery")
    private String inputQuery;
    @JsonProperty("errorInvalidEmail")
    private String errorInvalidEmail;
    @JsonProperty("promptAsk")
    private String promptAsk;
    @JsonProperty("searchTitle")
    private String searchTitle;
    @JsonProperty("domains")
    private String domains;
    @JsonProperty("inputEmail")
    private String inputEmail;
    @JsonProperty("sectionPromotionTitle")
    private String sectionPromotionTitle;
    @JsonProperty("sectionPromotionCTA")
    private String sectionPromotionCTA;
    @JsonProperty("sectionMenuTitle")
    private String sectionMenuTitle;
    @JsonProperty("sectionMenuCTA")
    private String sectionMenuCTA;
    @JsonProperty("sectionQLTitle")
    private String sectionQLTitle;
    @JsonProperty("sectionFaqTitle")
    private String sectionFaqTitle;
    @JsonProperty("promptResetTitle")
    private String promptResetTitle;
    @JsonProperty("promptReset")
    private String promptReset;
    @JsonProperty("promptContinueYes")
    private String promptContinueYes;
    @JsonProperty("poweredBy")
    private String poweredBy;
    @JsonProperty("inputChat")
    private String inputChat;
    @JsonProperty("inputSearchLocation")
    private String inputSearchLocation;
    @JsonProperty("inputSearchRegion")
    private String inputSearchRegion;
    @JsonProperty("textOr")
    private String textOr;
    @JsonProperty("textSend")
    private String textSend;
    @JsonProperty("textYes")
    private String textYes;
    @JsonProperty("textNo")
    private String textNo;
    @JsonProperty("textOk")
    private String textOk;
    @JsonProperty("textMore")
    private String textMore;
    @JsonProperty("textLess")
    private String textLess;
    @JsonProperty("textResults")
    private String textResults;
    @JsonProperty("textAccept")
    private String textAccept;
    @JsonProperty("textExplore")
    private String textExplore;
    @JsonProperty("browseAll")
    private String browseAll;
    @JsonProperty("viewResults")
    private String viewResults;
    @JsonProperty("textClose")
    private String textClose;
    @JsonProperty("textBrowse")
    private String textBrowse;
    @JsonProperty("searchErrorLocation")
    private String searchErrorLocation;
    @JsonProperty("searchErrorRegion")
    private String searchErrorRegion;
    @JsonProperty("searchErrorLocationDesc")
    private String searchErrorLocationDesc;
    @JsonProperty("searchErrorRegionDesc")
    private String searchErrorRegionDesc;
    @JsonProperty("nearbyResultsPre")
    private String nearbyResultsPre;
    @JsonProperty("nearbyResultsPost")
    private String nearbyResultsPost;
    @JsonProperty("sectionFaqCTA")
    private String sectionFaqCTA;
    @JsonProperty("promptContinueOk")
    private String promptContinueOk;
    @JsonProperty("contactUs")
    private String contactUs;
    @JsonProperty("privacy")
    private String privacy;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatbotIntegrationLocationSearch {
    @JsonProperty("searchEnabled")
    private Boolean searchEnabled;
    @JsonProperty("autoCompleteRegion")
    private String autoCompleteRegion;
    @JsonProperty("distanceUnit")
    private String distanceUnit;
    @JsonProperty("searchRadius")
    private String searchRadius;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatPreIntegration {

    @JsonProperty("primaryColor")
    private String primaryColor;
    @JsonProperty("secondaryColor")
    private String secondaryColor;
    @JsonProperty("headerColor")
    private String headerColor;
    @JsonProperty("fontStyle")
    private String fontStyle;
    @JsonProperty("openOnLoadMobile")
    private Boolean openOnLoadMobile;
    @JsonProperty("openOnLoadDesktop")
    private Boolean openOnLoadDesktop;
    @JsonProperty("chatPrompt")
    private String chatPrompt;
    @JsonProperty("chatPromptTimeout")
    private String chatPromptTimeout;
    @JsonProperty("chatPromptBorder")
    private String chatPromptBorder;
    @JsonProperty("chatPromptBackground")
    private String chatPromptBackground;
    @JsonProperty("askPrompt")
    private String askPrompt;
    @JsonProperty("askPromptColor")
    private String askPromptColor;
    @JsonProperty("chatIcon")
    private ChatIcon chatIcon;
    @JsonProperty("chatPopup")
    private ChatPopup chatPopup;
    @JsonProperty("customButtons")
    private List<CustomButton> customButtons;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class CustomButton {
    @JsonProperty("htmlId")
    private String htmlId;
    @JsonProperty("sourceType")
    private String sourceType;
    @JsonProperty("initialUtterance")
    private String initialUtterance;
    @JsonProperty("sessionBehavior")
    private String sessionBehavior;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatPopup {
    @JsonProperty("type")
    private String type;
    @JsonProperty("animation")
    private Boolean animation;
    @JsonProperty("sound")
    private Boolean sound;
    @JsonProperty("heading")
    private String heading;
    @JsonProperty("ctaText")
    private String ctaText;
    @JsonProperty("message")
    private String message;
    @JsonProperty("placement")
    private String placement;
    @JsonProperty("titleColor")
    private String titleColor;
    @JsonProperty("themeColor")
    private String themeColor;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatIcon {
    @JsonProperty("backgroundColor")
    private String backgroundColor;
    @JsonProperty("borderColor")
    private String borderColor;
    @JsonProperty("placement")
    private String placement;
    @JsonProperty("iconStyle")
    private String iconStyle;
    @JsonProperty("iconURL")
    private String iconURL;
    @JsonProperty("iconColor")
    private String iconColor;
    @JsonProperty("borderEnabled")
    private Boolean borderEnabled;
    @JsonProperty("animation")
    private Boolean animation;
    @JsonProperty("sound")
    private Boolean sound;
    @JsonProperty("animationTimeout")
    private int animationTimeout;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatPromotionMedia {

    private ChatPromotionMediaType type;
    private String thumbnail;
    private String link;
    private String info;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatPromotion {

    @JsonProperty("title")
    private String title;
    @JsonProperty("description")
    private String description;
    @JsonProperty("media")
    private ChatPromotionMedia media;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ChatMenu {

    private Integer index;
    private String masterProjectId;
    private String title;
    private String iconUrl;
    private ChatMenuType type;
    private String action;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class SlackIntegration extends MessageIntegration {
    private String botToken;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class GoogleBusinessMessageIntegration extends ChannelIntegration {
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ChatBotComponentOrder {
    private Integer id;
    private String component;

    @JsonProperty("component_index")
    private Integer componentIndex;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class WidgetIntegration extends ChannelIntegration {
    private String logoUrl;
    private String iconColor;
    private String primaryColor;
    private String secondaryColor;
    private String headerColor;
    private Boolean showCategory;
    private String privacyPolicyLink;
    private String fontFamily;
    private String searchTitle;
    private String answerTitle;
    private String questionThreshold;
    private String statementThreshold;
    private String language;
    private List<WidgetTypes> activeWidgetList;
    private List<StructuredDataSetSchema> structuredDataSetSchemas;
    private Map<String, String> voiceSetting;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class BookingWidgetIntegration extends ChannelIntegration {
    private String headerHtml;
    private String footerHtml;
    private BookingWidgetsPreIntegration widgetsPreIntegration;
    private Boolean enable;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class BookingWidgetsPreIntegration {
    private Theme theme;
    private FloatingButton floatingButton;
    private List<HtmlButton> htmlButtons;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Theme {
    private String primaryColor;
    private String secondaryColor;
    private String linkColor;
    private String font;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class FloatingButton {
    private Boolean enabled;
    private Boolean animation;
    private String position;
    private List<Widget> widgets;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class HtmlButton {
    private String htmlId;
    private WidgetConfig widgetConfig;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class Widget {
    private String icon;
    private String label;
    private WidgetConfig widgetConfig;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class WidgetConfig {
    private String widgetId;
    private SourceType type;
    private Boolean header;
    private Boolean footer;
    private String size;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class MessageIntegration extends ChannelIntegration {
    private String phoneNumber;
    private String webhookUrl;
    private List<ServiceProvider> serviceProvidersList;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ServiceProvider {
    private Vendor messageServiceProvider;
    private ProviderDetails providerDetails;

    public ServiceProvider(ServiceProvider serviceProvider) {
      this.messageServiceProvider = serviceProvider.getMessageServiceProvider();
      this.providerDetails = serviceProvider.getProviderDetails();
    }

    public ServiceProvider() {}
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class ProviderDetails {
    private String accountId;
    private String authToken;
    private String accessToken;

    public ProviderDetails(ProviderDetails serviceProviderDetails) {
      this.accountId = serviceProviderDetails.getAccountId();
      this.authToken = serviceProviderDetails.getAuthToken();
      this.accessToken = serviceProviderDetails.getAccessToken();
    }

    public ProviderDetails() {
      super();
    }
  }

  public static class WhatsappIntegration extends MessageIntegration {
  }

  public static class SmsIntegration extends MessageIntegration {
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class StructuredDataSetSchema {
    @JsonProperty("id")
    private String id;
    @JsonProperty("schemaType")
    private StructuredDataSetSchemaType schemaType;
    @JsonProperty("name")
    private String name;
    @JsonProperty("label")
    private String label;
    @JsonProperty("data")
    private Map<String, Object> data;
    @JsonProperty("createdAt")
    private Long createdAt;
    @JsonProperty("updatedAt")
    private Long updatedAt;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class ImageFormat {
    private String link;
    private String compressedLink;
    private Integer cropX;
    private Integer cropY;
    private Integer cropWidth;
    private Integer cropHeight;
    private Double cropWidthPercentage;
  }

  @Data
  @EqualsAndHashCode(callSuper = true)
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class WebsiteIntegration extends ChannelIntegration {
    private String logoUrl;
    private ImageFormat backgroundImageUrl;
    private String themeColor;
    private String themeTextColor;
    private String headingTextColor;
    private String resourceDns;
    private String termsAndConditionUrl;
    private String privacyPolicyUrl;
    private String youtubeLink;
    private String facebookLink;
    private String twitterLink;
    private String homePageLink;
    private Boolean isknowledgeBasePresent;
    private String language;
    private Map<String, String> voiceSetting;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
  public static class GoogleHomeIntegration {
  }
}
