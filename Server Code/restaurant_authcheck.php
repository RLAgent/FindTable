<?php
	include "lib/php/rsearchmysqli.php";
	
	$id = -1;
	$token = -1;
	foreach ($_POST as $key => $value){
	    if ($key == 'id')
	        $id = strval($value);
	    if ($key == 'token')
	    	$token = strval($value);
	}
	
	/*if ($id === -1)
	{
	    die("Failure! Invalid Restaurant ID");
	}*/
	
	if ($id === -1)
	{
		die("Failure! Invalid Username.");
	}
	if ($token === -1)
	{
		die("Failure! Invalid Password.");
	}
	$a = new RSearch();
	$login = $a -> checkToken($token,$id);
    die($login);
	
?>