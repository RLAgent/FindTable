<?php


// The purpose of this function is to escape MySQL special characters so that they are safe for
// the query, provided that the string is surrounded by quotation marks.
// PARAMETERS: $inp is a string or array of strings to escape
// RETURNS: an escaped version of the string.
// This is from the top comment in http://php.net/manual/en/function.mysql-real-escape-string.php
function mysql_escape_mimic($inp) {
	if(is_array($inp))
		return array_map(__METHOD__, $inp);

	if(!empty($inp) && is_string($inp)) {
		return str_replace(array('\\', "\0", "\n", "\r", "'", '"', "\x1a"), array('\\\\', '\\0', '\\n', '\\r', "\\'", '\\"', '\\Z'), $inp);
	}

	return $inp;
}

// The purpose of this function is to escape HTML special characters so that they are safe for
// displaying to the customer, provided that the string is surrounded by quotation marks.
// PARAMETERS: $inp is a string or array of strings to escape
// RETURNS: an escaped version of the string.
function html_escape_wrapper($inp){
	if (is_array($inp))
		return array_map(__METHOD__,$inp);
	
	if (!empty($inp) && is_string($inp)){
	     return htmlspecialchars($inp);
	}
	return $inp;
}

// This class contains all the search functions.
class RSearch {
	// These are the predetermined column names whose values we retrieve from the database.
    private static $colnames = array('waitlist_length',
    		'open_tables_X2','open_tables_X3','open_tables_X4','open_tables_X5',
    		'open_tables_X6','open_tables_X7','open_tables_X8','open_tables_X9',
    		'open_tables_X10','open_tables_X11','open_tables_X12','open_tables_X13',
    		'open_tables_X14','open_tables_X15','open_tables_X16','open_tables_X17',
    		'open_tables_X18','open_tables_X19','open_tables_X20',
    		'open_tables_Y2','open_tables_Y3','open_tables_Y4','open_tables_Y5',
    		'open_tables_Y6','open_tables_Y7','open_tables_Y8','open_tables_Y9',
    		'open_tables_Y10','open_tables_Y11','open_tables_Y12','open_tables_Y13',
    		'open_tables_Y14','open_tables_Y15','open_tables_Y16','open_tables_Y17',
    		'open_tables_Y18','open_tables_Y19','open_tables_Y20',
    		'open_tables_Z2','open_tables_Z3','open_tables_Z4','open_tables_Z5',
    		'open_tables_Z6','open_tables_Z7','open_tables_Z8','open_tables_Z9',
    		'open_tables_Z10','open_tables_Z11','open_tables_Z12','open_tables_Z13',
    		'open_tables_Z14','open_tables_Z15','open_tables_Z16','open_tables_Z17',
    		'open_tables_Z18','open_tables_Z19','open_tables_Z20','open_tables_BAR');
    
    // This is no longer useful
    public function __construct() {
    	// do nothing.
    }
    
    // gets the colnames array so that other subroutines can use them
    public static function getColNames() {
    	return RSearch::$colnames;
    }

    // This function, setTables, is used for updating table values of specific restaurants.
    // PARAMETERS: $restID, the unique restaurant ID, and
    // $tableInfo, which is an array of key-value
    // pairs with each key being an item in $colnames and value being the
    // information to be stored in the database.
    // RETURNS: 1 if successful, an error message if unsuccessful.
    public function setTables($restID, $tableInfo) {
    	$query = 'UPDATE restaurant_data SET ';
    	if (empty($tableInfo))
    		return "Nothing to update.";
    	$i = 0;
    	foreach ($tableInfo as $key => $value)
    	{
    		//$currkey = preg_replace("/[^a-zA-Z0-9_]/", "", $key);
    		if (in_array($key,RSearch::$colnames,TRUE))
    			$currkey = $key;
    		else
    			return "Invalid key name: " . $key;
    		$currval = mysql_escape_mimic($value);
    		//if (strcasecmp($currkey,"restaurant_id") == 0)
    		//	return "Invalid key: Cannot set restaurant_id. Operation aborted.";
    		$query = $query . $currkey . '="' . $currval . '",';
    		$i = $i + 1;
    	}
    	
    	$query = substr($query, 0, -1) . ' WHERE restaurant_id="' . mysql_escape_mimic($restID) . '"';
    	//echo $query . "<br>";
    	$link = new mysqli('localhost', 'query', 'antequ','findatable');
    	if (mysqli_connect_errno()) return 'Could not connect: '. mysqli_connect_error();
    	
    	
    	$result = $link->query($query);
    	// Performing SQL query
    	if (!$result)
    	{
    		return 'Query failed: ' . $link->error;
    		mysqli_close($link);
    	}
    	mysqli_close($link);
    	return 1;
    }
    
    // This function, yelpRestaurantInfo, is used to retrieve the restaurant information
    // from yelp AND the table information from our database. (Sorry about poor naming) The function
    // combines both sets of results and sanitizes it before outputting.
    // PARAMETERS: $lat, $long: Lattitude and longitude
    // $address: String for address query
    // $useGPS: a "yes" or "no" string for whether to use GPS query
    // $offset: The result number corresponding to the first result to be displayed. This is useful
    //  for when we want to see a result after the 20th result, since the Yelp api only returns
    //  20 results. We would set $offset=20 to see the beginning of the second page.
    // $categories: The categories to filter by, if we want to make an advanced search.
    //   See http://www.yelp.com/developers/documentation/category_list
    // RETURNS: an array in the same format as Yelp search results, or an error-message string.
    public function yelpRestaurantInfo($lat, $long, $address, $useGPS, $offset,$categories)
    {
    	require_once ('lib/OAuth.php');
    	
    	// For example, request business with id 'the-waterboy-sacramento'
    	//$unsigned_url = "http://api.yelp.com/v2/business/the-waterboy-sacramento";
    	
    	// For examaple, search for 'tacos' in 'sf'
    	//$unsigned_url = "http://api.yelp.com/v2/search?term=tacos&location=sf";
    	$unsigned_url = "http://api.yelp.com/v2/search";
    	
    	$term = "restaurant";
    	
    	$offset = strval($offset);
    	
    	$latitude = strval($lat);
    	$longitude = strval($long);
    	
    	// Set your keys here
    	$consumer_key = "_Zzp-P9R6Lr_w_GFPAwXCg";
    	$consumer_secret = "B-UlyC33_kZMuNLg4AWyvHNu6HE";
    	$token = "twuX2RYjyuzQqPHSzcwqBrWN_-p4135X";
    	$token_secret = "MsljTRN5pj6XUj59j2nkErA-Mh0";
    	
    	// Token object built using the OAuth library
    	$token = new OAuthToken($token, $token_secret);
    	
    	// Consumer object built using the OAuth library
    	$consumer = new OAuthConsumer($consumer_key, $consumer_secret);
    	
    	// Yelp uses HMAC SHA1 encoding
    	$signature_method = new OAuthSignatureMethod_HMAC_SHA1();
    	
    	// Build OAuth Request using the OAuth PHP library. Uses the consumer and token object created above.
    	//$oauthrequest = OAuthRequest::from_consumer_and_token($consumer, $token, 'GET', $unsigned_url);
    	//function search($term, $latitude, $longitude, $offset) {
    	$parameters = array();
    	$parameters["term"] = $term;
    	$parameters["limit"] = "20";
    	$parameters["sort"] = "1"; //sort by distance
    	$parameters["offset"] = $offset;
    	
    	if ($useGPS == "no")
    		$parameters["location"] =$address;
    	else
    		$parameters["ll"] = $latitude . "," . $longitude;
    	// optional category filter
    	if (!is_null($categories))
    		$parameters["category_filter"] = $categories;
    	$request = OAuthRequest::from_consumer_and_token($consumer, $token, 'GET', $unsigned_url, $parameters);
    	
    	$request->sign_request($signature_method, $consumer, $token);
    	
    	// Get the signed URL
    	$signed_url = $request->to_url();

    	// Send Yelp API Call
    	$ch = curl_init($signed_url);
    	curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    	curl_setopt($ch, CURLOPT_HEADER, 0);
    	$data = curl_exec($ch); // Yelp response
    	curl_close($ch);
    	//print "aftercurl!";
    	// Handle Yelp response data
    	$response = json_decode($data,true);
    	// make ID table

    	$ids = array();
    	$resultnumber = array();
    	foreach ($response["businesses"] as $i => $restaurant)
    	{
    		$ids[$i] = $restaurant["id"];
    		$resultnumber[$ids[$i]] = $i;
    		$response["businesses"][$i]["has_table_data"] = "False";
    		foreach(RSearch::$colnames as $column_name)
    			$response["businesses"][$i][$column_name] = "0";
    	}

    	$restaurant_info = $this -> getRestaurantInfo($ids);
 
    	if ($restaurant_info != 'NO RESULTS')
    	{
    		foreach ($restaurant_info as $restaurant)
    		{
    			$i = $resultnumber[$restaurant['id']];
    			$response["businesses"][$i]["has_table_data"] = $restaurant['has_table_data'];
    			foreach(RSearch::$colnames as $column_name)
    				$response["businesses"][$i][$column_name] = $restaurant[$column_name];
    		}
    	}
        // Sanitize the html output.
    	//return html_escape_wrapper($response);
    	// Apparently the display-side already html-escapes the output, so we don't need to.
    	// Double-escaping creates issues.
    	return $response;
    	
    }
    
    // This function, getRestaurantFromLogin, logs a restaurant app user in. It logs out any previous
    // sessions by replacing the old session token.
    // PARAMETERS: $user, $pass: the username, password pair
    // RETURNS: if successful, an array with 'restaurant_id' storing the restaurant ID
    //   and 'token' storing the session token. If failure, returns a string with the 
    //   error message
    public function getRestaurantFromLogin($user,$pass)
    {
    	$toReturn = array();
    	$TOKEN_LENGTH = 16; // number of bytes of security
    	$link = new mysqli('localhost', 'query', 'antequ','findatable');
    	if (mysqli_connect_errno()) return 'Could not connect: '. mysqli_connect_error();
    	

    	$curruser = mysql_escape_mimic($user); //sanitize
    	$currpass = mysql_escape_mimic(sha1($pass));
    	$wherestring = 'WHERE username="' . $curruser . '" AND password="' . $currpass . '"';
    	$query = 'SELECT restaurant_id FROM auth ' . $wherestring;	
    	$result = $link->query($query);
    	if (!$result) {
    		$toReturn = 'Query failed: ' . $link->error;
    		mysqli_close($link);
    		return $toReturn;
    	}
    	
    	$i = 0;
    	if ($result -> num_rows > 0) {
    		while ($line = $result->fetch_assoc()) {
    			
    			$toReturn['restaurant_id'] = $line['restaurant_id'];
    			// generate token!
    			$bytes = openssl_random_pseudo_bytes($TOKEN_LENGTH);
    			$token = bin2hex($bytes);

    			$toReturn['token'] = $token;
    			$query = 'UPDATE auth SET token="' . mysql_escape_mimic($token) . '" ' . $wherestring;
    			$result2 = $link->query($query);

    			if (!$result2) {
    				$toReturn = 'Query failed: ' . $link->error;
    				mysqli_close($link);
    				return $toReturn;
    				
    			}
    		   	
    		}
    	}
    	else {
    		$toReturn = 'Username and password combination not found.';
    	}
    	
    	
    	// Closing connection
    	mysqli_close($link);
    	return $toReturn;
    }
    
    // This function, checkToken, checks the validity of the Restaurant ID/Token pair.
    // PARAMETERS: $token: The session token
    //    $restID: The restaurant ID
    // RETURNS: "Success!" if successful, otherwise an error message.
    public function checkToken($token,$restID)
    {
    	$toReturn = array();
    	//$TOKEN_LENGTH = 16; // number of bytes of security
    	$link = new mysqli('localhost', 'query', 'antequ','findatable');
    	if (mysqli_connect_errno()) return 'Could not connect: '. mysqli_connect_error();
    	 
    
    	$currToken = mysql_escape_mimic($token); //sanitize
    	$currID = mysql_escape_mimic($restID);
    	$wherestring = 'WHERE token="' . $currToken . '" AND restaurant_id="' . $currID . '"';
    	$query = 'SELECT restaurant_id FROM auth ' . $wherestring;
    	
    	$result = $link->query($query);
    	if (!$result) {
    		$toReturn = 'Query failed: ' . $link->error;
    		mysqli_close($link);
    		return $toReturn;
    	}
    	 
    	//$i = 0;
    	if ($result -> num_rows > 0) {
    		while ($line = $result->fetch_assoc()) {
    			$toReturn = "Success!";
    				
    		}
    	}
    	else {
    		$toReturn = 'Login token not found.';
    	}
    	 
    	// Closing connection
    	mysqli_close($link);
    	return $toReturn;
    }
    
    // Gets table information of one restaurant.
    // PARAMETERS: $ID, a single restaurant ID
    // RETURNS: If successful, an array with each key being a column name. If unsuccessful,
    //   an error message.
    public function getRestaurantFromID($ID) {
    	$toReturn = array();
    
    	$link = new mysqli('localhost', 'query', 'antequ','findatable');
    	if (mysqli_connect_errno()) return 'Could not connect: '. mysqli_connect_error();
    
    	$query = 'SELECT * FROM restaurant_data WHERE `restaurant_id` IN (';
    		$currID = mysql_escape_mimic($ID); //sanitize
    		$query = $query . '"' . $currID . '")';
    
    	$result = $link->query($query);
    	if (!$result) {
    		$toReturn = 'Query failed: ' . $link->error;
    		mysqli_close($link);
    		return $toReturn;
    	}
    
    	$i = 0;
    	if ($result -> num_rows > 0) {
    		while ($line = $result->fetch_assoc()) {
    			$toReturn = $line;
    		}
    	}
    	else {
    		$toReturn = 'NO RESULTS';
    	}
    
    	// Closing connection
    	mysqli_close($link);
    	return $toReturn;
    }
    
    // Gets table information of many restaurants.
    // PARAMETERS: $IDs, an array of IDs
    // RETURNS: If successful, a 2D array, with the outermost array layer corresponding to
    //    each restaurant ID, and the inner layer containing keys such that
    //    each key corresponds to a column name. If unsuccessful, returns
    //    an error message.
    private function getRestaurantInfo($IDs) {
    	$toReturn = array();
    
        $link = new mysqli('localhost', 'query', 'antequ','findatable');
		  if (mysqli_connect_errno()) return 'Could not connect: '. mysqli_connect_error();
    
    	$query = 'SELECT * FROM restaurant_data WHERE `id` IN (';
    	foreach ($IDs as $value)
    	{
    		$currID = mysql_escape_mimic($value); //sanitize
    		$query = $query . '"' . $currID . '",';
    	}
    	$query = substr($query, 0, -1) . ')';
    
    	$result = $link->query($query);
    	if (!$result) {
    		$toReturn = 'Query failed: ' . $link->error;
    		mysqli_close($link);
    		return $toReturn;
    	}
    
    	$i = 0;
        if ($result -> num_rows > 0) {
        	while ($line = $result->fetch_assoc()) {
 				$toReturn[$i++] = $line;
        	}
        }
        else {
        	$toReturn = 'NO RESULTS';
        }
        
    	// Closing connection
    	mysqli_close($link);
    	return $toReturn;
    }
   
}

// Sums the total number of tables for a party size.
// PARAMETERS: $rest, the array of restaurant information, with each key being a column name, and
//     $party, the size of the party
// RETURNS: returns the total number of tables available to the party size
function sumTables($rest, $party) {
	$sum = 0;
	// the value stored is MAX*256 + CURRENT, where MAX is maximum number of
	// tables of that size, and CURRENT is the number of open tables of that size.
	for($i = $party; $i <= 20; $i+=1)
	{
		$sum += max($rest["open_tables_X".$i],0) % 256;
		$sum += max($rest["open_tables_Y".$i],0) % 256;
		$sum += max($rest["open_tables_Z".$i],0) % 256;
	}

	if ($party == 1)
		$sum += max($rest["open_tables_BAR"],0) % 256;

	return $sum;
}


/*
echo "<br>";


echo "creating searcher <br>";
$a = new RSearch();
var_dump($a->yelpRestaurantInfo("40.348839500000004", "-74.6503431", "","yes", 0,NULL));
echo "<br> STOPPP <br>";
var_dump($a->yelpRestaurantInfo("40.348839500000004", "-74.6503431", "","yes", 0,"chinese,korean"));
*/

 
/*
echo "testing displaylocation: <br>";
echo $a->displayLocation() . "<br>";

$restids = array();
$restids[0] = '6ix-bistro-atlantic-city-2';
$restids[1] = '6-star-tasty-restaurant-chino';
$restids[2] = '5-cs-louisiana-seafood-ontario';
$result = $a->getRestaurantInfo($restids);
var_dump($result);

$restids = array();
$restids[0] = 1234;
$restids[1] = 5678;
$restids[2] = 1324;
$restids[3] = 234;
echo $restids['email'];
$result = $a->addRestaurant($restids,$restids, $restids);

echo $result;
echo "<br>";
$restID = 2;
$tableInfo = array();
$tableInfo['open_tables_X2'] = 110;
$result = $a->setTables($restID, $tableInfo);
echo $result;
echo "<br>";

$result = $a->getRegionInfo();
die(var_dump($result));
echo $result['location'][0];
*/

?>
