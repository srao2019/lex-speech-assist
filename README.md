# Lex Speech Assistant
Lex Speech Assistant is a web application with an integrated Amazon Lex chatbot to provide individuals with speech disorders and impairments a means to practice and improve their speech. It provides an interface for users to practice various phrases and view their progress over time. 

## Deliverables
[Project Proposal](https://github.com/srao2019/lex-speech-assist/blob/master/Virtual%20Speech%20Therapy%20-%20CMSC389L%20Proposal.pdf)

[Checkpoint Report](https://github.com/srao2019/lex-speech-assist/blob/master/AWS%20Final%20Project%20Checkpoint.pdf)

[Video Demo](https://youtu.be/J5Ccq4rBJW8)

## Inspiration
*“Nearly 5 percent of U.S. children ages 3-17 have had a speech disorder in the past 12 months.”* (NIDCD, 2016)
Children and adults all over the world suffer from speech disorders. These can be related to muscle weakness or connections between the brain and the organs that control speech. Speech disorders can commonly occur in association with other genetic diseases, or after a traumatic injury. Speech disorders are most commonly treated with speech therapy, where a speech therapist listens for articulation and pronunciation issues in order to help patients learn to say common phrases. While it is available, speech therapy can be very expensive, and without insurance coverage, this cost increases even more. Furthermore, patients could often use more practice than they can afford to pay therapy for, creating the need for a simple, portable way to practice and improve speech.

## Product Design
Lex Speech Assistant is designed to provide individuals with a means to practice phrases and improve their speech on their own and anywhere. It is not intended to replace speech therapy, but merely provide an additional means for individuals to improve speech. It is currently built in with 100 simple, commonly used phrases, as well as contains the feature to add user-defined phrases. The web application has a login page which then takes users to their secure account page to practice. Here, users are able to interact with the Lex bot as it asks them phrases and then listens for their responses. Lex responds by providing the number of mistakes it heard in the user's input message. The user can also see the text of the phrase to say if for any reason they're unable to understand the bot. In a session with the bot, users have the option to skip or repeat a phrase, ask for help, or stop the session at anytime. On the "View Progress" page, users can see which phrases they have successfully completed and how many times. On the "Add Phrases" page, users have the ability to add custom phrases to Lex's database of phrases. Lex randomly retrieves a phrase based on the one ones that the user has not done successfully yet or has minimum success counts with. Addition of a phrase on this page will add it to the database here to be retrieved in the same random method.

![Image of Webapp](https://github.com/srao2019/lex-speech-assist/blob/master/homescreen.JPG)

## How to Run
With xampp, and the AWS SDK for PHP installed with the vendor/autoloader.php file configured in the root of "C://xampp/", the webapp can be run by running the login.php page. This opens the login page which after successful access, a user will be navigated to the chatbot homepage. 

## Architecture
Lex Speech Assistant is implemented through a combination of AWS services. The chatbot was created using Amazon Lex with functionality defined through AWS Lambda. The database of phrases and progress scores is managed through Amazon DynamoDB. 
![Image of Architecture](https://github.com/srao2019/lex-speech-assist/blob/master/architecture.png)

## Bot Implementation
The Lex bot was built on the AWS Lex console following these steps. 
1. Login to Amazon Lex Console: https://console.aws.amazon.com/lex. Click Create Bot, and follow the prompts to create a custom bot
2. Intents are defined as certain phrases that can trigger the bot to respond with an action.
3. Lex SpeechTherapyBot was defined with 6 intents(StartTherapy,StopTherapy,NextPhrase,SkipPhrase,VerifyPhrase,Help)
4. These intents all contain utterances which are editable on the console. AWS Lex is defined in a way that it learns similar utterances on its own, so not all need to be entered. 
5. Slot types are specific types of information that are the bot can detect. In order to verify phrases, a catchAll slot type was created with a variety of utterances to detect random phrases.
6. Lambda functions were written using Java after integrating AWS with the Eclipse IDE environment. An open source SDK for writing Lex Lambda functions was used from here: https://github.com/arun-gupta/lex-java
7. The functions were then connected as responses to the appropriate intents for the Lex chatbot.

## Database Implementation
The tables in DynamoDB were configured on the AWS DynamoDB console following these steps.
1. Login to AWS DynamoDB console: https://console.aws.amazon.com/dynamodb. Click create table, and follow prompts to create tables.
2. Three tables were created with the following schemas:

Phrases
* pid (partition key)
* phrase


PhraseScores
* uid (partition key)
* pid (sort key)
* num


Users
* username (partition key)
* uid
* password
 
 3. The Phrase table was loaded with phrases through a Java application written with DynamoDB Java SDK. 
 4. The updates through the webapp including, creating a user, updating scores, viewing scores, and updating phrases was done in PHP with the DynamoDB PHP SDK. 
 
## WebApp Implementation
1. The web app was built using HTML, Javascript, PHP, and CSS, with the base code for running a lex chatbot in Javascript from here https://github.com/awslabs/aws-lex-browser-audio-capture
2. The remaining features were added, and integration with DynamoDB to access and edit the tables was written in PHP with the DynamoDB PHP SDK. 

## Challenges
The most challenging part of this was building and testing the Lex chatbot. As it was defined by the Lambda functions, it was difficult to detect on the console where exactly the functions were failing or if the Lex bot itself was just not understanding the input correctly. This was also challenging to test, as Lex would succeed and fail inconsistently when speaking to it and respond differently when typing to it. Another challenge I had was getting it to run remotely on an EC2 server, rather than just locally on my computer. As it relies on sending HTTP requests to Amazon Lex and DynamoDB, it is affected by the network connection of the local machine and can therefore affect its performance.

## What I learned
I had never used AWS services intensely before this nor had I ever worked with chatbots of any sort. I learned a lot about how the various services can be integrated together, and be able to work with the various SDKs to communicate with the services. I also learned about how chatbots work with various configured intents, utterances, and slots. 

## Future Directions and Areas for Improvement
1. In order to make the app more portable, it would be better suited as a mobile app. This could be created through development of an Android app that allows users to interact with the chatbot on their phone. 
2. Lex does not always here the user very clearly. As this is standard defined by AWS Lex, the only way to improve this would be to provide more data for it to learn from.
3. Lex only listens for a short period of time, and cannot accept more than a certain amount of sound input. For users that have a speech impairment and talk slowly or very incoherently, it might not be able to detect their speech very well. Would be great if the chatbot could be adjusted to accomodate for this.  

