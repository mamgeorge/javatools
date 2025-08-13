package utils;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static utils.Aws1Class.EOL;
import static utils.Aws1Class.MAX_DISPLAY;

// implementation 'software.amazon.awssdk:s3:2.16.48' // for AWS2 2.16.48 > 2.16.59
public class Aws2Class {

	private S3Client s3Client = null;
	private static final Region REGION = Region.US_EAST_2;

	public Aws2Class( ) {

		String txtLines = "#### Aws2Class ####" + EOL;
		s3Client = S3Client.builder().region(REGION).build();
		System.out.println(txtLines);
	}

	public String listBuckets( ) {

		StringBuilder stringBuilder = new StringBuilder("listBuckets" + EOL);
		ListBucketsRequest LB_REQ = ListBucketsRequest.builder().build();
		ListBucketsResponse LB_RES = s3Client.listBuckets(LB_REQ);
		LB_RES.buckets().stream().forEach(x -> stringBuilder.append("* ").append(x.name() + EOL));
		return stringBuilder.toString();
	}

	public String listObjects(String bucket_name) {
		//
		StringBuilder stringBuilder = new StringBuilder("listObjects" + EOL);
		boolean done = false;
		ListObjectsV2Request LO2_REQ = ListObjectsV2Request.builder().bucket(bucket_name).maxKeys(1).build();
		ListObjectsV2Response LO2_RES = null;
		while ( !done ) {
			//
			LO2_RES = s3Client.listObjectsV2(LO2_REQ);
			for ( S3Object s3Object : LO2_RES.contents() ) {
				stringBuilder.append("* ").append(s3Object.key()).append(EOL);
			}
			if ( LO2_RES.nextContinuationToken() == null ) {
				done = true;
			}
			LO2_REQ = LO2_REQ.toBuilder().continuationToken(LO2_RES.nextContinuationToken()).build();
		}
		return stringBuilder.toString();
	}

	public String getObject(String bucket_name, String key_name) {

		StringBuilder stringBuilder = new StringBuilder("getObject" + EOL);
		GetObjectRequest GOR = GetObjectRequest.builder().bucket(bucket_name).key(key_name).build();
		ResponseInputStream<GetObjectResponse> RIS = s3Client.getObject(GOR);

		// stringBuilder.append(IoUtils.toUtf8String(RIS));
		InputStreamReader ISR = new InputStreamReader(RIS);
		BufferedReader BR = new BufferedReader(ISR);
		Stream<String> stream = BR.lines();
		String ris = stream.collect(Collectors.joining(EOL));
		stringBuilder.append(ris);

		String txtLines = stringBuilder.toString();
		if ( txtLines.length() > MAX_DISPLAY ) {
			txtLines = "[ " + txtLines.substring(0, MAX_DISPLAY) + " ]";
		}
		return txtLines;
	}
}
