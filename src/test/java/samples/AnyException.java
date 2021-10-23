package samples;

import java.time.Instant;
import java.util.logging.Logger;
import utils.UtilityMainTest;

public class AnyException extends IllegalStateException {

	static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());

	public AnyException() {
		//
		LOGGER.info(Instant.now().toString());
	}
}
