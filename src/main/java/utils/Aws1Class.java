package utils;

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
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;

// implementation 'com.amazonaws:aws-java-sdk-s3:1.11.1015'
public class Aws1Class {

	private AmazonS3 s3Client = null;

	private static final Logger LOGGER = Logger.getLogger(Aws1Class.class.getName());
	private static final Regions REGION = Regions.US_EAST_2; // Regions.DEFAULT_REGION
	//
	private static final String[] ROLE_ARN_LIST = {"arn:aws:iam::817749704698:user/martin", "arn:aws:iam::817749704698:group/admin", "arn:aws:iam::817749704698:mfa/root-account-mfa-device"};
	private static final String[] ROLE_SESSIONNAME_LIST = {"martin", "admin", "aws-codestar-service-role"};
	private static final String[] ACCESS_LIST = {"AWS", "IAM"};
	private static final int ACCESS_INT = 0;
	private static final String ROLE_ARN = ROLE_ARN_LIST[ACCESS_INT];
	private static final String ROLE_SESS = ROLE_SESSIONNAME_LIST[ACCESS_INT];
	private static final String ACCESS = ACCESS_LIST[ACCESS_INT];
	//
	public static final String BUCKET_NAME = "mamgeorgebucket1";
	public static final String KEYFILE_NAME = "humor_properTea.txt";
	public static final String EOL = "\n";
	public static final int MAX_DISPLAY = 80;

	public Aws1Class() {
		//
		String txtLines = "#### Aws1Class ####" + EOL;
		ProfileCredentialsProvider PCP = new ProfileCredentialsProvider();
		AWSCredentials awsCredentials = PCP.getCredentials();
		txtLines += awsCredentials.getAWSAccessKeyId() + EOL;
		txtLines += awsCredentials.getAWSSecretKey() + EOL;
		txtLines += showUsers();
		System.out.println(txtLines);
		//
		switch (ACCESS) {
			case "AWS":
				initAmazonS3_fromAWSaccount(PCP, REGION);
				break;
			case "IAM":
				initAmazonS3_fromIAMcredentials(PCP, REGION, ROLE_ARN, ROLE_SESS);
				break;
		}
	}

	private void initAmazonS3_fromAWSaccount(ProfileCredentialsProvider PCP, Regions region) {
		//
		// https://docs.aws.amazon.com/general/latest/gr/acct-identifiers.html
		// https://docs.aws.amazon.com/AmazonS3/latest/userguide/AuthUsingAcctOrUserCredentials.html
		// s3Client = AmazonS3ClientBuilder.standard().withRegion( US_EAST_2 ).build();
		s3Client = AmazonS3ClientBuilder.standard()
				.withCredentials(PCP)
				.withRegion(REGION)
				.build();
	}

	private void initAmazonS3_fromIAMcredentials(ProfileCredentialsProvider PCP, Regions region,
	                                             String roleArn, String roleSessionName) {
		//
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
			s3Client = AmazonS3ClientBuilder.standard()
					.withCredentials(ASCP)
					.withRegion(region)
					.build();
		} catch (AmazonServiceException ex) {
			LOGGER.info("call sent completed, but AmazonS3 could NOT process: " + ex.getMessage());
		} catch (SdkClientException ex) {
			LOGGER.info("AmazonS3 or client could NOT handle or parse response: " + ex.getMessage());
		}
	}

	private String showUsers() {
		//
		// https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-iam-users.html
		String txtLines = "";
		return txtLines;
	}

	public static void main(String[] args) {
		//
		String txtLines = "";
		Aws1Class aws1Class = new Aws1Class();
		txtLines += aws1Class.listBuckets() + EOL;
		txtLines += aws1Class.listObjects(BUCKET_NAME) + EOL;
		txtLines += aws1Class.getObject(BUCKET_NAME, KEYFILE_NAME) + EOL;
		//
		System.out.println(txtLines);
		System.out.println("DONE");
	}

	public String listBuckets() {
		//
		String txtLine = "";
		List<Bucket> buckets = s3Client.listBuckets();
		for (Bucket bucket : buckets) {
			txtLine += "* " + bucket.getName() + EOL;
		}
		return txtLine;
	}

	public String listObjects(String bucket_name) {
		//
		String txtLines = "";
		//
		ListObjectsV2Result LO2_RES = s3Client.listObjectsV2(bucket_name);
		List<S3ObjectSummary> s3ObjectSummaries = LO2_RES.getObjectSummaries();
		for (S3ObjectSummary s3ObjectSummary : s3ObjectSummaries) {
			txtLines += s3ObjectSummary.getKey() + EOL;
		}
		return txtLines;
	}

	public String getObject(String bucket_name, String key_name) {
		//
		String txtLines = "";
		try {
			S3Object s3Object = s3Client.getObject(bucket_name, key_name);
			S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent();
			//
			InputStreamReader ISR = new InputStreamReader(s3ObjectInputStream, UTF_8);
			txtLines = new BufferedReader(ISR).lines().collect(Collectors.joining("\n"));
			s3ObjectInputStream.close();
		} catch (AmazonServiceException ex) {
			System.err.println(ex.getErrorMessage());
		} catch (FileNotFoundException ex) {
			System.err.println(ex.getMessage());
		} catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
		if (txtLines.length() > MAX_DISPLAY) {
			txtLines = txtLines.substring(0, txtLines.indexOf(EOL));
		}
		return txtLines;
	}
}
