package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BookingWidgetConfig {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Theme {
    private String primaryColor;
    private String secondaryColor;
    private String linkColor;
    private String font;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class BookingWidgetResponse {
    private String id;
    private String projectId;
    private String tenantId;
    private String name;
    private String label;
    private String title;
    private String type;
    private String template;
    private List<Section> sections;
    private Map<String, Object> submitAction;
    private Long createdAt;
    private Long updatedAt;
    private Long version;
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Section {
    private String name;
    private String label;
    private String iconUrl;
    private String title;
    private Boolean disabled;
    private List<Map<String, Object>> fields;
    private Map<String, Object> nextAction;
  }


  private Object widgetSettings;
  private String headerHtml;
  private String footerHtml;
  private Theme theme;
  private BookingWidgetResponse widget;
}
