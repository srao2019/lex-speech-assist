import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.PutItemResult;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class VerifyPhrase  implements RequestHandler<Map<String,Object>, Object> {
	static Bot bot;
	static AmazonDynamoDB dynamoDB;
	
	public VerifyPhrase() {
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
	public Object handleRequest(Map<String, Object> input, Context context) {
		// TODO Auto-generated method stub
		context.getLogger().log("Received request: " + context);
	    LexRequest lexRequest = createLexRequest(input);
	    try {
			init();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    String currentPhrase = lexRequest.getSessionAttributes().get("currentPhrase");
	    String userResponse = lexRequest.getCurrentIntent().getSlots().get("speech");
	    String response = null;
	    DialogAction dialogAction = new DialogAction();
	    Map<String,String> sessionAttr = new HashMap<String,String>();
	    if(userResponse.replace(" ","").equalsIgnoreCase("help")){
	    	dialogAction.setType(DialogAction.ELICIT_INTENT_TYPE);
	    	response = "Say 'help' to hear instruction options.";
	    	sessionAttr = lexRequest.getSessionAttributes();
	    }else if(userResponse.replace(" ","").equalsIgnoreCase("skip")){
	    	dialogAction.setType(DialogAction.ELICIT_INTENT_TYPE);
	    	response = "Say 'skip' to skip this phrase.";
	    	sessionAttr = lexRequest.getSessionAttributes();
		}else if(userResponse.replace(" ","").equalsIgnoreCase("repeat")){
	    	dialogAction.setType(DialogAction.ELICIT_SLOT_TYPE);
	    	dialogAction.setSlotToElicit("speech");
	        dialogAction.setIntentName("VerifySpeech");
	        dialogAction.addSlots("speech","catchAll");
	    	response = "Ok! Repeat: "+currentPhrase;
	    	sessionAttr = lexRequest.getSessionAttributes();
	    }else if(currentPhrase.replace(" ", "").equalsIgnoreCase(userResponse.replace(" ",""))){
	    	HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
	    	Condition conditionUid = new Condition()
	    				.withComparisonOperator(ComparisonOperator.EQ.toString())
	    				.withAttributeValueList(new AttributeValue().withN(lexRequest.getUserId()));
	        Condition conditionPid = new Condition()
	        			.withComparisonOperator(ComparisonOperator.EQ.toString())
	        			.withAttributeValueList(new AttributeValue().withN(lexRequest.getSessionAttributes().get("currentPhraseId")));
		    scanFilter.put("uid", conditionUid);
		    scanFilter.put("pid",conditionPid);
		    ScanRequest scanRequest = new ScanRequest("PhraseScores").withScanFilter(scanFilter);
		    ScanResult scanResult = dynamoDB.scan(scanRequest);
	    	
		    String currCount = "";
		    if(!scanResult.getItems().isEmpty()){
			    currCount = scanResult.getItems().get(0).get("num").getN();
		    }else{
		    	currCount = "0";
		    }
	    	Map<String,AttributeValue> item = new HashMap<String,AttributeValue>();
        	item.put("uid",new AttributeValue().withN(lexRequest.getUserId()));
	    	item.put("pid",new AttributeValue().withN(lexRequest.getSessionAttributes().get("currentPhraseId")));
        	item.put("num",new AttributeValue().withN(new Integer(currCount+1).toString()));
        	PutItemRequest putItemRequest = new PutItemRequest("PhraseScores",item);
            dynamoDB.putItem(putItemRequest);
	    	response = "Great job! Say next for another phrase";
	    	dialogAction.setType(DialogAction.ELICIT_INTENT_TYPE);	    	
	    }else{
	    	ArrayList<String> mistakes = findMistakes(currentPhrase,userResponse);
	    	if(mistakes.size()==1){
	    		response = "Very close, you just missed 1 word. Repeat after me: "+mistakes.get(0);
	    		sessionAttr.put("currentPhrase", mistakes.get(0));
	    		sessionAttr.put("currentPhraseId",lexRequest.getSessionAttributes().get("currentPhraseId"));
	    	}else{
	    		response = "You made " + mistakes.size()+ " mistakes. Lets practice! Repeat after me: "+currentPhrase;
	    		sessionAttr.put("currentPhrase",lexRequest.getSessionAttributes().get("currentPhrase"));
	    		sessionAttr.put("currentPhraseId",lexRequest.getSessionAttributes().get("currentPhraseId"));
	    	}
	    	dialogAction.setType(DialogAction.ELICIT_SLOT_TYPE);
    		dialogAction.setSlotToElicit("speech");
    		dialogAction.setIntentName("VerifySpeech");
    		dialogAction.addSlots("speech","catchAll"); 
	    	
	    }
	    Message message = new Message(Message.CONTENT_TYPE_PLAIN_TEXT, response);
	    dialogAction.setMessage(message);
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
	private static ArrayList<String> findMistakes(String phrase,String userResponse){
		ArrayList<String> errors = new ArrayList<String>();
		String[] userWords = userResponse.trim().split(" ");
		String[] phraseWords = phrase.trim().split(" ");
		
		for(int i = 0; i < phraseWords.length;i++){
			if(i>userWords.length-1){
				errors.add(phraseWords[i]);
				continue;
			}
			if(!userWords[i].equalsIgnoreCase(phraseWords[i])){
				errors.add(phraseWords[i]);
			}
		}
		return errors;
	}

}
