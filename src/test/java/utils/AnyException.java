package utils;

import java.time.Instant;
import java.util.logging.Logger;

class AnyException extends IllegalStateException {

	static final Logger LOGGER = Logger.getLogger(UtilityMainTest.class.getName());

	public AnyException() {
		//
		LOGGER.info(Instant.now().toString());
	}
}
