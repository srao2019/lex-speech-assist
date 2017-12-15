<?php
    session_start();
?>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lex Speech Assistant</title>
    <link rel="icon" href="favicon.ico" sizes="32x32"/>
    <link rel="stylesheet" href="mainstyle.css">
</head>
    <h1>Lex Speech Assistant</h1>
    <div id = "navBar">
        <ul>
            <li><a href='speechMain.php'>Homepage</a></li>
            <li><a href="viewProgress.php">View Progress</a></li>
            <li><a href="addPhrases.php">Add Phrases</a></li>
        </ul>
    </div>
    <body class = "wrapper">
    <p>Lex Speech Assistant is designed to provide users a method to practice and improve their speech at anytime. During a session, Lex detects common
    speech abnormalities including pronunciation, articulation, and memory retention. Progress on phrases is recorded and can be viewed in the view progress tab.
    Lex is equipped with hundreds of commonly used phrases as well as the option to add user defined phrases!</p>
    <p><strong>Directions: </strong>Click on Lex and say "Start" to begin a session. Say "help" at any time for more instructions, and "quit" to end a session.</p>
    <div class="audio-control">
        <img id = "audio-control" src="robot.png" width = "100px" height = "100px">
        <canvas class="visualizer"></canvas>
    </div>
    <p><span id="message"></span></p>
    <br>
    <section id = "convo">
        <div id = "user">
            
        </div>
        <div id = "bot">
            
        </div>
       
    </section>
    <script src="https://sdk.amazonaws.com/js/aws-sdk-2.48.0.min.js"></script>
    <script src="dist/aws-lex-audio.js" type="text/javascript"></script>
    <script src="renderer.js" type="text/javascript"></script>
    <script src="aws-sdk-2.169.0.min.js" type = "text/javascript"></script>
    <script type="text/javascript">
    var access_key_id = "ACCESS_KEY_ID";
    var secret_access = "SECRET_ACCESS_KEY";
    var bot_name = 'SpeechTherapyBot';
    var uid = "<?php echo $_SESSION['uid']?>";
    var message = document.getElementById('message');
    var userMessages = document.getElementById('user');
    var botMessages = document.getElementById('bot');
    var newInput = document.createElement('p');
    var newResponse = document.createElement('p');
    var config, conversation;
    message.textContent = 'I am ready when you are!';

    document.getElementById('audio-control').onclick = function () {

         AWS.config.credentials = new AWS.Credentials(access_key_id,secret_access, null);
        AWS.config.region = 'us-east-1';
        
        config = {
            lexConfig: { botName: bot_name,
            botAlias: 'version_one',
            userId: uid
            }
        };


        conversation = new LexAudio.conversation(config, function (state) {
            message.textContent = state + '...';
        }, function (data) {
           console.log('Transcript: '+data.inputTranscript+', Response: '+data.message);
           newResponse.innerHTML = 'Response: '+data.message;
           newInput.innerHTML = 'You Said: ' + data.inputTranscript;
           userMessages.appendChild(newInput.firstChild);
           userMessages.appendChild(document.createElement('br'));
           userMessages.appendChild(document.createElement('br'));
           botMessages.appendChild(newResponse.firstChild);
           botMessages.appendChild(document.createElement('br'));
           botMessages.appendChild(document.createElement('br'));
        }, function (error) {
            message.textContent = error;
        });
        conversation.advanceConversation();
    };

</script>
</body>

</html>