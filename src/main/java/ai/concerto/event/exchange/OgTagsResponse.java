package ai.concerto.event.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OgTagsResponse {

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public class OgTagsData {
    @JsonProperty("ogSiteName")
    private String ogSiteName;
    @JsonProperty("ogUrl")
    private String ogUrl;
    @JsonProperty("ogTitle")
    private String ogTitle;
    @JsonProperty("ogDescription")
    private String ogDescription;
    @JsonProperty("ogType")
    private String ogType;
    @JsonProperty("ogImage")
    private OgMedia ogImage = new OgMedia();
    @JsonProperty("ogVideo")
    private OgMedia ogVideo = new OgMedia();
  }

  @Data
  @JsonInclude(Include.NON_NULL)
  @JsonIgnoreProperties(ignoreUnknown = true)
  public class OgMedia {
    private String url;
    private String width;
    private String height;
    private String type;
  }

  private OgTagsData data = new OgTagsData();
}
