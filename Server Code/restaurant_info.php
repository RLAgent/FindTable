<?php
	include "lib/php/rsearchmysqli.php";
	$a = new RSearch();
	$id = -1;
	$token = -1;
	foreach ($_POST as $key => $value){
	    if ($key == 'id')
	        $id = strval($value);
	    if ($key == 'token')
	    	$token = strval($value); 
	}

	if ($id === -1)
	{
	    die("Invalid Restaurant ID");
	}
	if ($token === -1)
	{
		die("Invalid Token");
	}
	$result = $a->checkToken($token, $id);

	if ($result != "Success!")
		die("Logged out!");
	

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
	$name = str_replace('\\','\\\\',$name);
	$type = str_replace('\\','\\\\',$type);
	$address = str_replace('\\','\\\\',$address);
	$reservation_link =  str_replace('\\','\\\\',$reservation_link);
	$toreturn = $name . '\\/' . $type . '\\/' . $address . '\\/' . $reservation_link;
	$colnames = RSearch::getColNames();
	foreach ($colnames as $colname)
	{
		$value = $results[$colname];
		$value = str_replace('\\','\\\\',$value);
		$toreturn = $toreturn . '\\/' . $value;
	}
	die($toreturn);

?>