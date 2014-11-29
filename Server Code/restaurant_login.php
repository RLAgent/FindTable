<?php
	include "lib/php/rsearchmysqli.php";
	
	$user = -1;
	$pass = -1;
	foreach ($_POST as $key => $value){
	    //if ($key == 'id')
	   //     $id = strval($value);
	    if ($key == 'username')
	    	$user = strval($value);
	    if ($key == 'password')
	    	$pass = strval($value);
	    
	}
	
	/*if ($id === -1)
	{
	    die("Failure! Invalid Restaurant ID");
	}*/
	
	if ($user === -1)
	{
		die("Failure! Invalid Username.");
	}
	if ($pass === -1)
	{
		die("Failure! Invalid Password.");
	}
	$a = new RSearch();
	$login = $a -> getRestaurantFromLogin($user,$pass);

	if (is_array($login) )
	{
		$id = $login['restaurant_id'];
		$token = $login['token'];
	}
	else
		die("Failure! " . $login);
	
	$results = $a -> getRestaurantFromID($id);
	if (is_array($results))
	{
		$name = $results['name'];
		$type = $results['type'];
	}
	else
		die("Failure! " . $results);
	// get address and format it
	$address = str_replace(';',', ',$results['location_address']) . ', ' . $results['location_city'] . ', ' . $results['location_state_code'] . ', ' . $results['location_postal_code'];
	
	while (strpos($address, ', , ')!== FALSE) {
		$address = str_replace(', , ',', ', $address); 
	}
	if (substr($address,-2) == ', ')
	{
	    $address = substr($address,0,-2);
	}
	$reservation_link = "No Reservation Link Provided.";
	
	// escape backslashes
	$id = str_replace('\\','\\\\',$id);
	$token = str_replace('\\','\\\\',$token);
	$name = str_replace('\\','\\\\',$name);
	$type = str_replace('\\','\\\\',$type);
	$address = str_replace('\\','\\\\',$address);
	$reservation_link =  str_replace('\\','\\\\',$reservation_link);
	$toreturn = $id . '\\/' . $token . '\\/' . $name . '\\/' . $type . '\\/' . $address . '\\/' . $reservation_link;
	$colnames = RSearch::getColNames();
	foreach ($colnames as $colname)
	{
		$value = $results[$colname];
		$value = str_replace('\\','\\\\',$value);
		$toreturn = $toreturn . '\\/' . $value;
	}
	die($toreturn);

?>