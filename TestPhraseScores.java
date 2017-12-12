import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class TestPhraseScores {
	static AmazonDynamoDB dynamoDB;
	static String tableName = "PhraseScores";
	static Random rand = new Random();
	
	private static void init() throws Exception {
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\Sahana\\.aws\\credentials), and is in valid format.",
                    e);
        }
        dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-east-1")
            .build();
    }
	
	public static void main(String[] args) throws Exception {
        init();
        
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
	    ScanRequest scanRequest = new ScanRequest("Phrases").withScanFilter(scanFilter);
	    ScanResult scanResult = dynamoDB.scan(scanRequest);
	    int size = scanResult.getItems().size();
		Map<String,AttributeValue> item = new HashMap<String,AttributeValue>();
    	item.put("uid",new AttributeValue().withN("1"));
    	item.put("num",new AttributeValue().withN("0"));
	    for(int i = 1; i <= size;i++){  
	    	item.put("pid",new AttributeValue().withN(new Integer(i).toString()));
		    PutItemRequest putItemRequest = new PutItemRequest(tableName,item);
	        PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
	        System.out.println("Result: " + putItemResult);
        }

	}
}
