import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.sample.aws.lex.request.Bot;
import org.sample.aws.lex.request.Intent;
import org.sample.aws.lex.request.LexRequest;
import org.sample.aws.lex.response.DialogAction;
import org.sample.aws.lex.response.LexResponse;
import org.sample.aws.lex.response.Message;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class GetFromPhrases implements RequestHandler<Map<String,Object>, Object> {
	static AmazonDynamoDB dynamoDB;
	static String phrasesTable = "Phrases";
	static String phraseScoresTable = "PhraseScores";
	static Random rand = new Random();
	static Bot bot;
	static int currentUserId=1;

	 public GetFromPhrases() {
		 try {
				init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	 
	 private static void init() throws Exception {
		 ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("credentials","default");
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
	        bot = new Bot();
	        bot.setName("SpeechTherapyBot");
	        bot.setVersion("$LATEST");
	}
	 
	 @Override
	 public Object handleRequest(Map<String,Object> input, Context context) {
	    context.getLogger().log("Received request: " + context);
	    LexRequest lexRequest = createLexRequest(input);
	    String opener = "";
	    String phrase = "";
	    String phraseId = "";
	    String uid = "";
	    if(lexRequest.getUserId()!=null){
	    	uid = lexRequest.getUserId();
	    }else{
	    	uid = "1";
	    	lexRequest.setUserId(uid);
	    }
	    if(lexRequest.getSessionAttributes()!=null 
	    		&& lexRequest.getSessionAttributes().containsKey("currentPhrase")
	    		&& lexRequest.getCurrentIntent().equals("StartTherapy") 
	    		&& !lexRequest.getInputTranscript().equals("skip")){
	    	opener = "You're already in a session. ";
	    	phrase = lexRequest.getSessionAttributes().get("currentPhrase");
	    	phraseId = lexRequest.getSessionAttributes().get("currentPhraseId");
	   
	    }else if(lexRequest.getCurrentIntent()!=null && lexRequest.getCurrentIntent().equals("NextPhrase")){
	    	opener = "Great, ";
	    }else if(lexRequest.getInputTranscript()!=null && lexRequest.getInputTranscript().equals("skip")){
	    	opener = "Ok, how about this. ";
	    } else{
	    	opener = "Great, lets start! ";
	    }
	    try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
	    Condition condition = new Condition()
	        .withComparisonOperator(ComparisonOperator.EQ.toString())
	        .withAttributeValueList(new AttributeValue().withN(getNewPhraseId().toString()));
	    scanFilter.put("pid", condition);
	    ScanRequest scanRequest = new ScanRequest(phrasesTable).withScanFilter(scanFilter);
	    ScanResult scanResult = dynamoDB.scan(scanRequest);
	   
	    if(phrase.equals("")){
	    	phrase = scanResult.getItems().get(0).get("phrase").getS();
	    	phraseId = scanResult.getItems().get(0).get("pid").getN();
	 	}
	    String response = opener + "Repeat after me: "+ phrase;
	    Message message = new Message(Message.CONTENT_TYPE_PLAIN_TEXT, response);
        DialogAction dialogAction = new DialogAction();
        dialogAction.setType(DialogAction.ELICIT_SLOT_TYPE);
        dialogAction.setSlotToElicit("speech");
        dialogAction.setIntentName("VerifySpeech");
        dialogAction.addSlots("speech","catchAll");
        dialogAction.setMessage(message);
        Map<String,String> sessionAttr = new HashMap<String,String>();
        sessionAttr.put("currentPhrase",phrase);
        sessionAttr.put("currentPhraseId",phraseId);
        return new LexResponse(dialogAction,sessionAttr);

	}
	 
	private static LexRequest createLexRequest(Map<String,Object> input){
		LexRequest req = new LexRequest();
		req.setBot(bot);
		req.setCurrentIntent(createIntent((Map<String,Object>)input.get("currentIntent")));
		if(input.containsKey("confirmationStatus"))
			req.setConfirmationStatus((String)input.get("confirmationStatus"));
		if(input.containsKey("inputTranscript"))
			req.setInputTranscript((String)input.get("inputTranscript"));
		if(input.containsKey("invocationSource"))
			req.setInvocationSource((String)input.get("invocationSource"));
		if(input.containsKey("messageVersion"))
			req.setMessageVersion((String)input.get("messageVersion"));
		if(input.containsKey("outputDialogMode"))
			req.setOutputDialogMode((String)input.get("outputDialogMode"));
		if(input.containsKey("sessionAttributes"))
			req.setSessionAttributes((Map<String,String>)input.get("sessionAttributes"));
		if(input.containsKey("userId"))
			req.setUserId((String)input.get("userId"));
		return req;
	}
	private static Intent createIntent(Map<String,Object> m){
		Intent i = new Intent();
		if(m.containsKey("name"))
			i.setName((String)m.get("name"));
		if(m.containsKey("slots"))
			i.setSlots((Map<String,String>)m.get("slots"));
		return i;
	}
	public static Integer getNewPhraseId(){
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition conditionU = new Condition()
        	.withComparisonOperator(ComparisonOperator.EQ.toString())
        	.withAttributeValueList(new AttributeValue().withN(new Integer(currentUserId).toString()));
		scanFilter.put("uid", conditionU);
	    ScanRequest scanRequest = new ScanRequest(phraseScoresTable).withScanFilter(scanFilter);
	    ScanResult scanResult = dynamoDB.scan(scanRequest);
	    ArrayList<Integer> nums = new ArrayList<Integer>();
	    ArrayList<Integer> mins = new ArrayList<Integer>();
	    for(Map<String, AttributeValue> m : scanResult.getItems()){
	    	nums.add(Integer.parseInt(m.get("num").getN()));
	    }
	    Arrays.sort(nums.toArray());
	    Integer minScore = nums.get(0);
	    for(Map<String, AttributeValue> m : scanResult.getItems()){
	    	if(minScore.equals(Integer.parseInt(m.get("num").getN()))){
	    		mins.add(Integer.parseInt(m.get("pid").getN()));
	    	}
	    }
	    int randInd = rand.nextInt(mins.size()-1);
	    return mins.get(randInd);
	    
	}
}
