package ai.concerto.event.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Builder
@Data
@JsonInclude(Include.NON_NULL)
public class DataSources {

  @JsonProperty("bodyTemplate3Data")
  private BodyTemplate3Data bodyTemplate3Data;
  @JsonProperty("listBodyTemplate1")
  private ListBodyTemplate1 listBodyTemplate1;


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class BodyTemplate3Data {

    private String type;
    @JsonProperty("objectId")
    private String objectId;
    private String title;
    @JsonProperty("backgroundImage")
    private ImageDto backgroundImage;
    private ImageDto image;
    @JsonProperty("textContent")
    private TextContent textContent;
    @JsonProperty("logoUrl")
    private String logoUrl;
    @JsonProperty("hintText")
    private String hintText;
  }


  @Getter
  @Setter
  public class ListBodyTemplate1 {
    @JsonProperty("listTemplate1Metadata")
    private ListTemplate1Metadata listTemplate1Metadata;
    @JsonProperty("listTemplate1ListData")
    private ListTemplate1ListData listTemplate1ListData;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class ImageDto {

    @JsonProperty("contentDescription")
    private String contentDescription;
    @JsonProperty("smallSourceUrl")
    private String smallSourceUrl;
    @JsonProperty("largeSourceUrl")
    private String largeSourceUrl;
    List<Sources> sources;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class Sources {

    private String url;
    private String size;
    @JsonProperty("widthPixels")
    private Integer widthPixels;
    @JsonProperty("heightPixels")
    private Integer heightPixels;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class TextContent {

    private Text title;
    private Text subtitle;
    @JsonProperty("primaryText")
    private Text primaryText;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class Text {
    private String type;
    private String text;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class ListTemplate1Metadata {

    private String type;
    @JsonProperty("objectId")
    private String objectId;
    @JsonProperty("backgroundImage")
    private ImageDto backgroundImage;
    private String title;
    @JsonProperty("logoUrl")
    private String logoUrl;
  }


  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class ListTemplate1ListData {
    private String type;
    @JsonProperty("listId")
    private String listId;
    @JsonProperty("totalNumberOfItems")
    private Integer totalNumberOfItems;
    @JsonProperty("listPage")
    private ListPage listPage;

  }

  @Getter
  @Setter
  @JsonInclude(Include.NON_NULL)
  public class ListItem {
    @JsonProperty("listItemIdentifier")
    private String listItemIdentifier;
    @JsonProperty("ordinalNumber")
    private Integer ordinalNumber;
    @JsonProperty("textContent")
    private TextContent textContent;
    private ImageDto image;
    private String token;
  }

  @Getter
  @Setter
  public class ListPage {
    @JsonProperty("listItems")
    private List<ListItem> listItems;
  }
}
