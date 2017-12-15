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
   <h2>Progress</h2>
   <p>The table below provides phrases that you have successfully mastered! Great work!</p>
   <table>
      <tr>
         <th>Phrase</th>
         <th>Successful Attempts</th>
      </tr>
EOBODY;
   $iterator = $client->getIterator('Scan', array(
    'TableName'     => "PhraseScores",
    'KeyConditions' => array(
        'uid' => array(
            'AttributeValueList' => array(
                array('N' => $_SESSION['uid'])
            ),
            'ComparisonOperator' => 'EQ'
        ),
        'pid'=> array(
                      'AttributeValueList' => array(),
                      'ComparisonOperator' => 'NOT_NULL')
        
        
    )
));
   foreach($iterator as $item){
      $pid = $item['pid']['N'];
      $response = $client->getItem(array(
     'ConsistentRead' => true,
    'TableName' => 'Phrases',
    'Key' => array(
        'pid'   => array('N' => $pid)
    )
));
      $count = $item['num']['N'];
      if($count > 0 && $item['uid']['N'] == $_SESSION['uid']){
         $phrase = $response['Item']['phrase']['S'];
         $body.= <<<EOBODY
         <tr>
            <td>{$phrase}</td>
            <td>{$count}</td>
         </tr>
EOBODY;
      }
   }

   $body.= "</table></body></html>";
   
   echo $page.$body;
    
?>