package samples;

public class AnyObject {

	private String stringValue = "";
	private String strongValue = "";
	private String secretValue = "";

	public AnyObject() {
		stringValue = "ANYSTRING";
		strongValue = "OTHSTRING";
		secretValue = "SECRETSTR";
	}

	//
	public String getStringValue() { return stringValue; }

	public String getStrongValue() { return strongValue; }

	private String getSecretValue() { return secretValue; }

	//
	public void setStringValue(String stringValue) { this.stringValue = stringValue; }

	public void setStrongValue(String strongValue) { this.strongValue = strongValue; }

	private void setSecretValue(String secretValue) { this.secretValue = secretValue; }
}