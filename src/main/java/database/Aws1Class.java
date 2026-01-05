package database;

// IAM access

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.Owner;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static utils.UtilityMain.EOL;

// implementation 'com.amazonaws:aws-java-sdk-s3:1.11.1015'
public class Aws1Class {

	private AmazonS3 amazonS3 = null;

	private static final Logger LOGGER = Logger.getLogger(Aws1Class.class.getName());
	private static final Regions REGION = Regions.US_EAST_2; // Regions.DEFAULT_REGION

	private static final String[] ROLE_ARN_LIST =
		{ "arn:aws:iam::817749704698:user/martin", "arn:aws:iam::817749704698:group/admin",
			"arn:aws:iam::817749704698:mfa/root-account-mfa-device" };
	private static final String[] ROLE_SESSIONNAME_LIST = { "martin", "admin", "aws-codestar-service-role" };
	private static final String[] ACCESS_LIST = { "AWS", "IAM" };
	private static final int ACCESS_INT = 0;
	private static final String ROLE_ARN = ROLE_ARN_LIST[ACCESS_INT];
	private static final String ROLE_SESS = ROLE_SESSIONNAME_LIST[ACCESS_INT];
	private static final String ACCESS = ACCESS_LIST[ACCESS_INT];

	public static final String[] BUCKET_NAMES = { "mlg-s3-events", "mlg-s3-sample" };
	public static final String[] KEYFILE_NAMES =
		{ "coffee.jpg", "images/01_Gen0617_Flood3_DelugeTablet_Utnapishtim_t.jpg" };
	public static final int intVal = 1;
	public static final int MAX_DISPLAY = 80;

	public Aws1Class( ) {

		String txtLines = "#### AwsClass ####" + EOL;
		ProfileCredentialsProvider PCP = new ProfileCredentialsProvider("dev");
		AWSCredentials awsCredentials = null;
		try { awsCredentials = PCP.getCredentials(); }
		catch( SdkClientException ex )
		{ System.out.println( "ERROR: " + ex.getMessage() ); }
		String awsAccessKeyId = awsCredentials.getAWSAccessKeyId();
		String awsSecretKey = awsCredentials.getAWSSecretKey();

		switch ( ACCESS ) {
			case "AWS":
				initAmazonS3_fromAWSaccount(PCP, REGION);
				break;
			case "IAM":
				initAmazonS3_fromIAMcredentials(PCP, REGION, ROLE_ARN, ROLE_SESS);
				break;
			default:
				throw new IllegalStateException("Unexpected ACCESS value: " + ACCESS);
		}
		String showUsers = showUsers();
		txtLines += "awsAccessKeyId: " + awsAccessKeyId + EOL + "awsSecretKey: " + awsSecretKey + EOL +
			"showUsers: " + showUsers + EOL;
		System.out.println(txtLines);
	}

	private void initAmazonS3_fromAWSaccount(ProfileCredentialsProvider PCP, Regions region) {

		/*
			https://docs.aws.amazon.com/general/latest/gr/acct-identifiers.html
			https://docs.aws.amazon.com/AmazonS3/latest/userguide/AuthUsingAcctOrUserCredentials.html
			s3Client = AmazonS3ClientBuilder.standard().withRegion( US_EAST_2 ).build();
		*/
		amazonS3 = AmazonS3ClientBuilder.standard()
			.withCredentials(PCP)
			.withRegion(region)
			.build();
	}

	private void initAmazonS3_fromIAMcredentials(ProfileCredentialsProvider PCP, Regions region,
		String roleArn, String roleSessionName) {

		// https://docs.aws.amazon.com/AmazonS3/latest/userguide/AuthUsingTempSessionToken.html
		try {
			// create STS client for trusted code; it has security credentials for temporary security credentials
			AWSSecurityTokenService ASTS = AWSSecurityTokenServiceClientBuilder.standard()
				.withCredentials(PCP)
				.withRegion(region)
				.build();
			// get IAM credentials; AWS root account will fail
			AssumeRoleRequest AR_REQ = new AssumeRoleRequest()
				.withRoleArn(roleArn)
				.withRoleSessionName(roleSessionName);
			AssumeRoleResult AR_RES = ASTS.assumeRole(AR_REQ);
			Credentials sessionCredentials = AR_RES.getCredentials();
			// get BSC with credentials you just retrieved
			BasicSessionCredentials BSC = new BasicSessionCredentials(
				sessionCredentials.getAccessKeyId(),
				sessionCredentials.getSecretAccessKey(),
				sessionCredentials.getSessionToken());
			// get temporary security credentials so AS3client can send authenticated requests to Amazon S3
			AWSStaticCredentialsProvider ASCP = new AWSStaticCredentialsProvider(BSC);
			amazonS3 = AmazonS3ClientBuilder.standard()
				.withCredentials(ASCP)
				.withRegion(region)
				.build();
		}
		catch (AmazonServiceException ex) {
			LOGGER.info("call sent completed, but AmazonS3 could NOT process: " + ex.getMessage());
		}
		catch (SdkClientException ex) {
			LOGGER.info("AmazonS3 or client could NOT handle or parse response: " + ex.getMessage());
		}
	}

	private String showUsers( ) {

		// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-iam-users.html
		String txtLines = "";
		Owner owner = amazonS3.getS3AccountOwner();
		txtLines = "Owner: " + owner.getId() + EOL + "getDisplayName: " + owner.getDisplayName() + EOL;
		return txtLines;
	}

	public String listBuckets( ) {

		StringBuilder stringBuilder = new StringBuilder("listBuckets" + EOL);
		List<Bucket> buckets = amazonS3.listBuckets();
		for ( Bucket bucket : buckets ) {
			stringBuilder.append("* ").append(bucket.getName()).append(EOL);
		}
		return stringBuilder.toString();
	}

	public String listObjects(String bucket_name) {

		StringBuilder stringBuilder = new StringBuilder("listObjects" + EOL);
		ListObjectsV2Result LO2_RES = amazonS3.listObjectsV2(bucket_name);
		List<S3ObjectSummary> s3ObjectSummaries = LO2_RES.getObjectSummaries();
		for ( S3ObjectSummary s3ObjectSummary : s3ObjectSummaries ) {
			stringBuilder.append("* ").append(s3ObjectSummary.getKey()).append(EOL) ;
		}
		return stringBuilder.toString();
	}

	public String getObject(String bucket_name, String key_name) {

		StringBuilder stringBuilder = new StringBuilder("getObject" + EOL);
		try {
			S3Object s3Object = amazonS3.getObject(bucket_name, key_name);
			S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();

			InputStreamReader ISR = new InputStreamReader(s3ObjectInputStream, UTF_8);
			stringBuilder.append( new BufferedReader(ISR).lines().collect(Collectors.joining(EOL)) );
			s3ObjectInputStream.close();
		}
		catch (AmazonServiceException ex) {
			System.err.println(ex.getErrorMessage());
		}
		catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
		String txtLines = stringBuilder.toString();
		if (txtLines.length() > MAX_DISPLAY ) {
			txtLines = "[ " +txtLines.substring(0, MAX_DISPLAY) + " ]";
		}
		return txtLines;
	}
}
