<?php
include "rsearchmysqli.php";


echo "<br>";


echo "creating searcher. Testing Advanced Category Search <br>";
$a = new RSearch();
var_dump($a->yelpRestaurantInfo("40.348839500000004", "-74.6503431", "","yes", 0,NULL));
echo "<br><br> Now with Chinese, Korean: <br><br>";
var_dump($a->yelpRestaurantInfo("40.348839500000004", "-74.6503431", "","yes", 0,"chinese,korean"));

 
// Test for getRestaurantInfo. Commented out because it's now a private method
/*
echo "<br><br>Testing getRestaurantInfo -- DEFUNCT because it's now private!<br><br>";
$restids = array();
$restids[0] = '6ix-bistro-atlantic-city-2';
$restids[1] = '6-star-tasty-restaurant-chino';
$restids[2] = '5-cs-louisiana-seafood-ontario';
$result = $a->getRestaurantInfo($restids);
var_dump($result);
*/

echo "<br><br>Testing getRestaurantFromID!<br><br>";
$restids = '6ix-bistro-atlantic-city-2';
$result = $a->getRestaurantFromID($restids);
var_dump($result);

echo "<br><br>Testing getRestaurantFromID Failure!<br><br>";
$restids = '6ix-bistro-atlantic-city-2 no restaurant';
$result = $a->getRestaurantFromID($restids);
var_dump($result);

echo "<br><br>Testing setTables! <br><br>";
$restID = 66441;
$tableInfo = array();
$tableInfo['open_tables_X2'] = 2567;
$result = $a->setTables($restID, $tableInfo);
echo $result;
echo "<br>";

echo "<br><br>Testing setTables failure! <br><br>";
$restID = "a342318";
$tableInfo = array();
$tableInfo['open_tables_X2'] = 2564;
var_dump($a->setTables($restID, $tableInfo));
echo "<br>";

echo "<br><br>Testing MySQL escaping: <br>";
echo mysql_escape_mimic("'hi'");

echo "<br><br>Testing login.<br><br>";
$results = $a->getRestaurantFromLogin("user@blue","point");
var_dump($results);

echo "<br><br>Testing login failure.<br><br>";
var_dump($a->getRestaurantFromLogin("user@blueee","pointee"));

echo "<br><br>Testing login check.<br><br>";
$result = $a->checkToken($results['token'],$results['restaurant_id']);
var_dump($result);

echo "<br><br>Testing login check failure.<br><br>";
$result = $a->checkToken($results['token'] . "fail",$results['restaurant_id']);
var_dump($result);

?>
