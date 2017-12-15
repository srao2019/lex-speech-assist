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
EOBODY;
   $body = <<< EOBODY
      <form action = "{$_SERVER['PHP_SELF']}" method = "post">
         <strong>Username: </strong><input type="text" name = "username" value = ""/><br><br>
         <strong>Password: </strong><input type = "password" name = "password" value = ""/><br><br>
         <input type = "submit" name = "submit" value = "Submit" />
      </form>
      <form action ='{$_SERVER['PHP_SELF']}' method = "post">
         <input type = "submit" name = "createUser" value = "Create New Account" />
      </form>
EOBODY;
      if(isset($_POST['submit'])){
         //validate user credentials
         $response = $client->getItem(array(
         'ConsistentRead' => true,
         'TableName' => 'Users',
         'Key' => array(
               'username'   => array('S' => $_POST['username'])
           )
       ));
         if(!empty($response['Item'])){
            if($response['Item']['password']['S']==$_POST['password']){
               $_SESSION['uid'] = $response['Item']['uid']['N'];
               $body.= <<< EOBODY
               <p> Welcome {$_POST['username']}! Lets get started!</p>
               <form action = "speechMain.php" method = "post"/>
                  <input type  = "submit" name = "go" value = "Go to my page!" />
               </form>
EOBODY;
            }
         }else{
            $body.= <<<EOBODY
            <p> Invalid username or password. Please try again.</p>
EOBODY;
         }
      }
      if(isset($_POST['createUser'])){
         $body.=<<<EOBODY
            <p>Create an account!</p>
            <form action = "{$_SERVER['PHP_SELF']}" method = "post">
               <strong>Username: </strong><input type = "text" name = "newUsername" value = ""/><br><br>
               <strong>Password: </strong><input type = "password" name = "newPass" value = ""/><br><br>
               <input type = "submit" name = "newUser" value = "Create"/>
            </form>
EOBODY;
      }
      if(isset($_POST['newUser'])){
          $result = $client->getItem(array(
        'TableName' => 'Users',
        'Key' => array( 
            'username'   => array('S' => $_POST['newUsername'])
        )
    ));
         if(!empty($result['Item'])){
           $body.="<p> Username already exists! Please try again.</p>";
         }else{
            $uid = rand();
            $_SESSION['uid'] = $uid;
            $client->putItem(array(
           'TableName' => 'Users',
           'Item' => array( 
               'username'   => array('S' => $_POST['newUsername']),
               'password' => array('S' => $_POST['newPass']),
               'uid' => array('N' => (string)$uid)
           )
       ));
               $body.= <<< EOBODY
               <p> Welcome {$_POST['newUsername']}! Lets get started!</p>
               <form action = "speechMain.php" method = "post"/>
                  <input type  = "submit" name = "go" value = "Go to my page!" />
               </form>
EOBODY;
         }
      }
      
      echo $page.$body;
?>
