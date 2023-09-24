package samples;

import java.io.Serializable;
import java.util.concurrent.Callable;

public
class DBClusterWorkingTask implements Callable<String>, Serializable {
	@Override
	public String call( ) throws Exception {

		String message = "\"Hello from DBClusterWorkingTask!";
		return message;
	}
}
