package samples;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter @Setter
public class AnyObject {

	private String alpha;
	private String beta;
	private String gamma;

	public AnyObject( ) {
		alpha = "ALEPH";
		beta = "BETH";
		gamma = "GIMMEL";
	}

	@NotNull @Contract( pure = true )
	private String getPrivateText( ) { return "PRIVATE_TEXT!"; }

}