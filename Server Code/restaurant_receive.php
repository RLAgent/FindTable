<?php
include "lib/php/rsearchmysqli.php";

$searcher = new RSearch();
$colnames = RSearch::getColNames();
//echo "Test";
$restID = "No ID";
$restToken = "No Token";
$tableInfo = array();
foreach ($_POST as $key => $value) {
	
	if (in_array($key,$colnames,TRUE))
	{
		$tableInfo[$key] = $value;
	}
	else 
	{
		if (strcmp($key,"id") == 0)
		{
			$restID = $value;
			// disallow "No ID" as a value because it's what we use to test if it's no ID.
			if ($value == "No ID")
				die('Invalid restaurant ID: "' . $value . '"');
		}
		elseif (strcmp($key, "token") == 0)
		{
			$restToken = $value;
			if ($value == "No Token")
				die('Invalid restaurant Token: "' . $value . '"');
		}
		else
		{
			die('Error: "' . $key . '" is an invalid key.');
		}
	}

}
//if ($region == -1)
//  die('Region not received.');
if ($restID == "No ID")
    die('Restaurant ID not received.');
if ($restToken == "No ID")
	die('Restaurant Token not received.');

$result = $searcher->checkToken($restToken, $restID);

if ($result != "Success!")
	die("Logged out!");

$result = $searcher->setTables($restID, $tableInfo);
if ($result === 1)
    die('Success!');
else
	die('Error in setTables: ' . $result);
/*
// Connecting, selecting database
$link = mysql_connect('localhost','query', 'antequ');
if (!$link) die('Could not connect: ' . mysql_error());
echo 'Connected successfully';
mysql_select_db('findatable') or die('Could not select database');

// Performing SQL query
$query = 'SELECT * FROM table_test';
$result = mysql_query($query) or die('Query failed: ' . mysql_error());
$line1 = mysql_fetch_array($result, MYSQL_ASSOC);
$line2 = mysql_fetch_array($result, MYSQL_ASSOC);
foreach ($line2 as $key => $value)
{
	echo "$key $value";
}
$line3 = mysql_fetch_array($result, MYSQL_ASSOC);
$abc = "ab cd";
$line3 = preg_replace("/[^a-zA-Z0-9_\s]/", "", $abc);
$line3 = 'SELECT * FROM ' . $line3;
$line = array($line1, $line2, $line3, "hi9" => "test");
die(var_dump($line));

// Printing results in HTML
echo "<table>\n";
while ($line = mysql_fetch_array($result, MYSQL_ASSOC)) {
    echo "\t<tr>\n";
    foreach ($line as $col_value) {
        echo "\t\t<td>$col_value</td>\n";
    }
    echo "\t</tr>\n";
}
echo "</table>\n";

// Free resultset
mysql_free_result($result);

// Closing connection
mysql_close($link);*/
?>