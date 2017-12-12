import java.util.ArrayList;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;


public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		GetFromPhrases f = new GetFromPhrases();
		ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider("src/main/java/credentials","default");
		System.out.println(credentialsProvider.getCredentials().toString());
		VerifyPhrase v = new VerifyPhrase();
		HelpPrompt h = new HelpPrompt();
		ArrayList<String> mistakes = findMistakes("how are you ","how are");
		System.out.println(mistakes);
		
		
		
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
