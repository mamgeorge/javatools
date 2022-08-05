package samples;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.UUID;

@Getter @Setter
public class AnyObject {

	private String alpha;
	private String beta;
	private String gamma;
	private String privateText = "PRIVATE_TEXT!";
	private int integerval;
	private Date dateval = new Date();
	private UUID uuid;

	public AnyObject( ) {
		alpha = "ALEPH";
		beta = "BETH";
		gamma = "GIMMEL";
	}
}