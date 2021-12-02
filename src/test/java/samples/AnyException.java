package samples;

import utils.UtilityMainTest;

import java.time.Instant;
import java.util.logging.Logger;

public class AnyException extends IllegalStateException {

	static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());

	public AnyException() {
		//
		LOGGER.info("AnyException(" + Instant.now().toString() + ")");
	}
}
