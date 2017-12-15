<?php
    session_start();
    require 'C:\xampp\vendor\autoload.php';
    use Aws\DynamoDb\DynamoDbClient;

    $client = new DynamoDbClient([
    'region'  => 'us-east-1',
    'version' => '2012-08-10',
    'scheme'  => 'http'
]);
    
    $page = <<< EOBODY
    
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
                <li><a href='viewProgress.php'>View Progress</a></li>
                <li><a href="addPhrases.php">Add Phrases</a></li>
            </ul>
        </div>
EOBODY;

   $body = <<< EOBODY
   <body>
   <h2>Add Phrases</h2>
   <p>Add custom phrases to practice here! Lex generates a random phrase based on the phrases you haven't completed yet or have been having trouble with.
   By adding a phrase here, it gives Lex access to add this phrase to the set of phrases to ask you!</p>
   
   <form action = "{$_SERVER['PHP_SELF']}" method = "post">
        <strong>Phrase: </strong><input type ="text" name = "customPhrase" value = ""/><br><br>
        <input type = "submit" name = "submit" value = "Submit"/>
   </form>
    
EOBODY;

    if(isset($_POST['submit'])){
        $pid = rand();
         $client->putItem(array(
           'TableName' => 'Phrases',
           'Item' => array( 
               'pid'   => array('N' => (string)$pid),
               'phrase' => array('S' => $_POST['customPhrase'])
           )
       ));
         
         $body.= <<<EOBODY
            <br>
            <p> Phrase "{$_POST['customPhrase']}" added! </p>
EOBODY;
    }
    
    echo $page.$body;
    
?>