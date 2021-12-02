package samples;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class OauthToken {

	String access_token;
	String token_type;
	String expires_in;
	String id_token;
}
