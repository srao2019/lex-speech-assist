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

public class AddToPhrases {
	static AmazonDynamoDB dynamoDB;
	static String tableName = "Phrases";
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
        
        HashMap<Integer,Map<String,AttributeValue>> phrases = new HashMap<Integer,Map<String,AttributeValue>>();
        int id = 2;
        FileReader fr = new FileReader("C:\\Users\\Sahana\\SpeechTherapy\\phrases.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;
        while((line = br.readLine()) != null){
        	Map<String,AttributeValue> item = new HashMap<String,AttributeValue>();
        	item.put("pid",new AttributeValue().withN(Integer.toString(id)));
        	item.put("phrase",new AttributeValue(line));
        	phrases.put(id,item);
        	id++;
        }
        br.close();
        for(int i = 2; i < id;i++){
             PutItemRequest putItemRequest = new PutItemRequest(tableName, phrases.get(i));
             PutItemResult putItemResult = dynamoDB.putItem(putItemRequest);
             System.out.println("Result: " + putItemResult);
        }
        
        HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
	    ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
	    ScanResult scanResult = dynamoDB.scan(scanRequest);
	    int size = scanResult.getItems().size();
	    
	    Condition condition = new Condition()
	        .withComparisonOperator(ComparisonOperator.EQ.toString())
	        .withAttributeValueList(new AttributeValue().withN(new Integer(rand.nextInt(size-1)+1).toString()));
	    scanFilter.put("pid", condition);
	    scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
	    scanResult = dynamoDB.scan(scanRequest);
	    System.out.println(scanResult);
	}
}