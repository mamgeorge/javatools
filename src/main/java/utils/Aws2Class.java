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
import software.amazon.awssdk.utils.IoUtils;

import java.io.IOException;

import static utils.Aws1Class.BUCKET_NAME;
import static utils.Aws1Class.EOL;
import static utils.Aws1Class.KEYFILE_NAME;
import static utils.Aws1Class.MAX_DISPLAY;

// implementation 'software.amazon.awssdk:s3:2.16.48' // for AWS2 2.16.48 > 2.16.59
public class Aws2Class {
	//
	private S3Client s3Client = null;
	private static final Region REGION = Region.US_EAST_2;

	public Aws2Class( ) {
		//
		String txtLines = "#### Aws2Class ####" + EOL;
		s3Client = S3Client.builder().region(REGION).build();
	}

	public static void main(String[] args) {
		//
		String txtLines = "";
		Aws2Class aws2Class = new Aws2Class();
		txtLines += aws2Class.listBuckets() + EOL;
		txtLines += aws2Class.listObjects(BUCKET_NAME) + EOL;
		txtLines += aws2Class.getObject(BUCKET_NAME, KEYFILE_NAME) + EOL;
		//
		System.out.println(txtLines);
		System.out.println("DONE");
	}

	public String listBuckets( ) {
		//
		StringBuffer stringBuffer = new StringBuffer();
		ListBucketsRequest LB_REQ = ListBucketsRequest.builder().build();
		ListBucketsResponse LB_RES = s3Client.listBuckets(LB_REQ);
		LB_RES.buckets().stream().forEach(x -> stringBuffer.append(x.name() + EOL));
		return stringBuffer.toString();
	}

	public String listObjects(String bucket_name) {
		//
		String txtLines = "";
		boolean done = false;
		ListObjectsV2Request LO2_REQ = ListObjectsV2Request.builder().bucket(bucket_name).maxKeys(1).build();
		ListObjectsV2Response LO2_RES = null;
		while ( !done ) {
			//
			LO2_RES = s3Client.listObjectsV2(LO2_REQ);
			for ( S3Object s3Object : LO2_RES.contents() ) {
				txtLines += s3Object.key() + EOL;
			}
			if ( LO2_RES.nextContinuationToken() == null ) {
				done = true;
			}
			LO2_REQ = LO2_REQ.toBuilder().continuationToken(LO2_RES.nextContinuationToken()).build();
		}
		return txtLines;
	}

	public String getObject(String bucket_name, String key_name) {
		//
		String txtLines = "";
		GetObjectRequest GOR = GetObjectRequest.builder().bucket(bucket_name).key(key_name).build();
		ResponseInputStream<GetObjectResponse> RIS = s3Client.getObject(GOR);
		try {
			txtLines = IoUtils.toUtf8String(RIS);
		}
		catch (IOException ex) {
			System.err.println(ex.getMessage());
		}
		if ( txtLines.length() > MAX_DISPLAY ) {
			txtLines = txtLines.substring(0, txtLines.indexOf(EOL));
		}
		return txtLines;
	}
}
