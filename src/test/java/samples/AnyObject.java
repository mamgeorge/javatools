package samples;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnyObject {

	private String alpha = "";
	private String beta = "";
	private String gamma = "";

	public AnyObject() {
		alpha = "ALEPH";
		beta = "BETH";
		gamma = "GIMMEL";
	}
}