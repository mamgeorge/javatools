package objects;

import java.time.Instant;
import java.util.logging.Logger;

public class AnyException extends IllegalStateException {

	static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	public AnyException( ) {
		//
		LOGGER.info("AnyException(" + Instant.now().toString() + ")");
	}
}
