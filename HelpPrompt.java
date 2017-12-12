import java.util.HashMap;
import java.util.Map;

import org.sample.aws.lex.request.Bot;
import org.sample.aws.lex.request.Intent;
import org.sample.aws.lex.request.LexRequest;
import org.sample.aws.lex.response.DialogAction;
import org.sample.aws.lex.response.LexResponse;
import org.sample.aws.lex.response.Message;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;


public class HelpPrompt  implements RequestHandler<Map<String,Object>, Object> {
	static Bot bot;
	public HelpPrompt() {
		 try {
				init();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	 
	 private static void init() throws Exception {
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
	    
	    DialogAction dialogAction = new DialogAction();
    	String response = "You can say start to start a session and quit at any time to stop. "
    			+ "You can also say skip or repeat for each phrase.";
		dialogAction.setType(DialogAction.ELICIT_INTENT_TYPE); 
	    
	    Message message = new Message(Message.CONTENT_TYPE_PLAIN_TEXT, response);
	    dialogAction.setMessage(message);
        return new LexResponse(dialogAction,lexRequest.getSessionAttributes());
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

}
