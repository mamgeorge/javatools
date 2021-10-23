package samples;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Book {

	@JsonProperty("author") public String author;
	@JsonProperty("price") public String price;
	@JsonProperty("genre") public String genre;
	@JsonProperty("description") public String description;
	@JsonProperty("id") public String id;
	@JsonProperty("title") public String title;
	@JsonProperty("publish_date") public String publish_date;
}
