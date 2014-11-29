<?php include "lib/php/rsearchmysqli.php";?>

<!--TODO: fix desktop stylesheet rules-->
<!--TODO: add content filtering-->
<!--TODO: iPhone 4 with 6.1.3 map still not working correctly -->
<!--NY ny on phonegap Android 4.1.2 with 1 person breaks the map.-->
<!--Make a note about the api limitation.-->
<!--Can we fix the double-tap issue on older Android versions?-->
<!--Known bug: if you click on the "google" logo on the map, you can't get back to the app--> 


<!--Andrew:-->
<!--I added "or an address" to the main bar-->
<!--I added an error to handle searches that don't return restaurants (like Seoul)-->

<!--Marisa:-->
<!--Found a bug in certain kinds of addresses which contain parenthetical remarks-->

<!--Richard:-->
<!--Found a bug when you search for a number-->

<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1">
<link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.css">
<link href="css/custom.css" rel="stylesheet">
<script src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
<script src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script src="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.js"></script>  
</head>
<body>
    
<?php

// numbers for pin color array entries
$green = 0;
$grey = 2;
$red = 1;

// if we got no post variables, display an error message and die
    $hasVars = FALSE;
    if ($_POST['town'] == '' AND $_POST['gps'] == '') {
        echo '<div id="NoVars" class="ui-content queryFailure">
            <h3>No Results</h3>
            <p>Whoops, it looks like you didn\'t enter a town, and you also have your GPS disabled. 
                Please enable GPS or input your location.</p>
            <a href="#" onClick="history.go(-1);" class="ui-btn ui-btn-inline">Try Again</a>
        </div>';
        die();
    } else {
        $hasVars = TRUE;
    }
    
    // run the search
    $search = new RSearch();
    if ($_POST['town'] == ''){
        // split the latitude and longitude by the comma
        $coord = explode(",", $_POST['gps']);
        $results = $search->yelpRestaurantInfo($coord[0], $coord[1], "", "yes", 0);
    }
    else {
        $results = $search->yelpRestaurantInfo("", "", $_POST['town'], "no", 0);
    }
    $restaurants = $results['businesses'];
    
    // if the search returned nothing, display an error message and die
    // also handles the situation when a user inputs a number as search term
    if (($restaurants == NULL && $hasVars) || is_numeric($_POST['town'])) {
        echo '<div id="Empty" class="ui-content queryFailure">
            <h3>No Results</h3>
            <p>Whoops, it looks like your search didn\'t return any results.<br>
                You can either input a city and state, or else an address in the US.<br>
                We regret that we are currently only able to serve the US.</p>
            <a href="#" onClick="history.go(-1);" class="ui-btn ui-btn-inline">Try Again</a>
        </div>';
        die();
    }
    ?>

<!--starting the main page content-->
<div data-role="page" id="map_div">
    <div data-role="header">
        <h1>FindTable</h1>
        <a href="#" class="ui-btn ui-btn-inline" id="back-button" data-rel="back">Back</a> 
        <a href="#popupAbout" data-position-to="#back-button" data-rel="popup" data-transition="pop" data-role="button" class="ui-btn ui-btn-inline" >About</a>
            
        <!-- this pop-up is not displayed unless called--> 
        <div data-role="popup" id="popupAbout" class="ui-content infoPopup">
            <a href='#' data-rel='back' class='ui-btn ui-corner-all ui-shadow ui-btn-a ui-icon-delete ui-btn-icon-notext ui-btn-right'>Close</a>
            <h3>About</h3>
            <p>This project was created for Princeton COS 333 by Ante Qu,
                David Kong, Lisa Lee, and Ming-Yee Tsang.</p>
        </div>
    </div> <!-- end header -->
    
    <div data-role="main" class="ui-content">
        <!--button that toggles between the list and map views--> 
        <button class="ui-btn" id="toggle_switch" onclick="
            if (document.getElementById('toggle_switch').innerHTML == 'Show List')
                document.getElementById('toggle_switch').innerHTML = 'Show Map';
            else if (document.getElementById('toggle_switch').innerHTML == 'Show Map')
                document.getElementById('toggle_switch').innerHTML = 'Show List';
            
            document.getElementById('list_info').hidden = !document.getElementById('list_info').hidden;
            document.getElementById('content').hidden = !document.getElementById('content').hidden;
           ">Show Map</button>

        <!--this gets hidden/shown by toggle-->
        <div id="list_info" hidden>
            <ul data-role="listview" data-inset="true">
                <?php
                // this PHP builds the pop-up windows and also builds an array
                // which is later passed into the JS that prints the google map,
                // containing information about the pins
                
                // start the javascript array
                $jsArray = "[ "; 
                
                // loop through each restaurant
                foreach($restaurants as $rest) {
                    
                    // create the string "N open tables" for the list-view
                    if ($rest['has_table_data'] == "True") {
                        $tableinfo = sumTables($rest, $_POST['party']) . " open tables";
                    } else {
                        $tableinfo = "";
                    }
                    
                    // create the string "N open tables..." for the pop-up
                    $tablesMessage = "";
                    $openTables = sumTables($rest, $_POST['party']);        // TODO: Simplify
                    if ($rest['has_table_data'] == 'True') {
                       if ($openTables > 1)
                            $tablesMessage = "<p> $openTables open tables for your party size. </p>";
                       else if ($openTables == 1)
                           $tablesMessage = "<p> 1 open table for your party size. </p>";
                       else if ($openTables == 0)
                           $tablesMessage = "<p> Sorry, no open tables for your party size. </p>";
                    } 
                    
                    // build the popup HTML. One popup for each restaurant, but the reference
                    // gets used twice: once on the list-view link and once on the map pin link.
                     
                    // I use phone number as a simple unique identifier for the restaurant div
                    // each pop-up div ID looks like #1234567890 etc.
                    $popList = '<a href="#pop' . $rest[phone] . '" data-rel="popup" class="ui-btn ui-corner-all ui-shadow" data-transition="pop">';
                    $popMap = '<a href="#pop' . $rest[phone] . '" data-rel="popup" data-transition="pop">' . htmlentities($rest[name], ENT_QUOTES, "UTF-8") . '</a>';
                    $divName = "'#pop" . $rest[phone] . "'";
                    
                    // turn the address into nice strings, one of which will be printed, and the
                    // other of which will be used to plot the pin onto the map
                    $print_address = "";
                    $pin_address = "";
                    foreach ($rest['location']['display_address'] as $index => $line) {
                            // strip out instructions in parentheses, which can throw off the geocode
                           if (strpos($line, '(') == FALSE && strpos($line, ')') == FALSE) {
                                $print_address .= "<p>$line</p>";
                                $pin_address .= $line . " ";
                           }
                    }
                    
                    // decide pin color
                    if ($rest['has_table_data'] != 'True')
                        $pinColor = $grey;
                    else if ($openTables > 0)
                        $pinColor = $green;
                    else 
                        $pinColor = $red;
                    
                    // print the values we've determined into the Javascript array
                    $jsArray .= "['', '$pin_address', $pinColor, $divName],\n"; 
                    
                    // write the list item and the pop-up div for this restaurant.
                    // the pop-up div will be hidden unless its link is called
                    // by tapping on a list item or pin
                    echo "
                    <li> $popList
                            <img src='${rest[image_url]}'/> 
                            <h2> " . htmlentities($rest[name], ENT_QUOTES, "UTF-8") . " </h2> 
                            <p> ${tableinfo}  </p> 
                            <img src='${rest[rating_img_url]}'/> 
                        </a>

                        <div data-role='popup' class='ui-content' id='pop${rest[phone]}'>
                            <a href='#' data-rel='back' class='ui-btn ui-corner-all ui-shadow ui-btn-a ui-icon-delete ui-btn-icon-notext ui-btn-right'>Close</a>

                            <div class='ui-grid-a' id='restau_infos'>	
                                <div class='ui-block-a'>
                                    <h1>" . htmlentities($rest[name], ENT_QUOTES, "UTF-8") . "</h1>
                                    <p><strong> {$rest['categories'][0][0]}  </strong></p>	
                                    $tablesMessage

                                </div>		
                                <div class='ui-block-b'>
                                    <p><img src='${rest['image_url']}' alt=' photo'/></p>
                                   <!-- <p><a href=' ${rest['mobile_url']}' rel='external' data-role='button'> View on Yelp </a></p>  -->
                                </div>
                            </div><!-- /grid-a -->
                            <hr/>

                            <div class='ui-grid-a' id='contact_infos'>	
                                <div class='ui-block-a'>
                                    <h2> Located at:</h2>
                                    $print_address
                                </div>		
                            </div><!-- /grid-a -->
                            <div id='contact_buttons'>
                                <div><button onclick='window.open(\"tel:${rest[phone]}\");'>Call us at " . substr($rest['display_phone'],3) . "</button></div>
                            </div>
                            <hr/>

                            <div id='notation'>	
                                <h3> Rating </h3>
                                <img src='${rest['rating_img_url_large']}' alt='Rating'/> 
                                <p> ${rest['review_count']} reviews </p>
                            </div>

                        </div> <!-- end restaurant popup -->
                    </li> <!-- end list item -->";
                } // end loop through each restaurant 
                
                $jsArray .= ']'; // finish the JS array.
                ?>
                
            </ul> <!-- close the list -->
        </div> <!-- close list_info -->
    </div> <!-- end main content -->
            
    <!--this contains the map and gets hidden/shown by toggle-->
    <div data-role="content" id="content">
        <div id="map_canvas" style="height:100%"></div>
    </div>  
    
    <div data-role="footer">
        <h3>Created by Ante, David, Lisa, and Ming-Yee.</h3>
    </div>
</div>

<script type="text/javascript">
        $(document).on('pageshow', '#map_div', function(e,data){  // TODO: remove arguments???
            $('#content').height(getRealContentHeight());
            
            var minZoomLevel = 12;
            
            // echo in the PHP-generated JS array which contains each restaurant address,
            // its pin color, and the name of the popup div to link to
            var locations = <?php echo $jsArray?>;
                  
          // Setup the different icons and shadows
          var iconURLPrefix = 'http://54.186.80.240/img/';
          var icons = [
            iconURLPrefix + 'green-dot.png',
            iconURLPrefix + 'red-dot.png',
            iconURLPrefix + 'grey-dot.png', 
          ]
          
          // cosmetics
          var shadow = {
            anchor: new google.maps.Point(15,33),
            url: iconURLPrefix + 'msmarker.shadow.png'
          };

          // create the map, variables self-explanatory
          var map = new google.maps.Map(document.getElementById('map_canvas'), {
            zoom: 13,
            mapTypeId: google.maps.MapTypeId.ROADMAP,
            mapTypeControl: false,
            streetViewControl: false,
            panControl: false,
            zoomControlOptions: {
               position: google.maps.ControlPosition.LEFT_BOTTOM
            }
          });


          
          // the number of pins to put on the map. too many, and Google's
          // geocode api will shut you down
          var pinsDisplayed = 9;
          
          var markers = new Array();
          
            // loop through markers and add themto the map. this is more complicated 
            // than it looks because of the ways that the maps api works. two
            // separate closures needed
            for (var i = 0; i < pinsDisplayed; i++) {
                
                // geocode each pin
                geocoder = new google.maps.Geocoder();
                geocoder.geocode( {'address': locations[i][1] }, (function() {

                    var restaurant = locations[i];
                    return function(results, status) {
                        if (status == google.maps.GeocoderStatus.OK) {

                            var latlon = results[0].geometry.location;

                            var marker = new google.maps.Marker({
                                position: latlon,
                                map: map,
                                icon : icons[restaurant[2]],            
                                shadow: shadow
                            });

                            markers.push(marker);
                            AutoCenter();
                            // add an event listener for the marker
                            google.maps.event.addListener(marker,"click",function(){ 
                                $(restaurant[3]).popup('open', {y: 0 });
                            }); 

                        } else {
                           // uncomment to debug
                           // alert("Google is throttling geocode requests.");
                        }
                    }
                })() );
            } // end for loop

            function AutoCenter() {
                // conditional avoids flashing the empty map for a moment at default
                if (markers.length == 1 || markers.length == pinsDisplayed) {
                    //  create a new viewpoint bound
                    var bounds = new google.maps.LatLngBounds();
                    //  go through each marker and add
                    $.each(markers, function (index, marker) {
                        bounds.extend(marker.position);
                    });
                    //  fit these bounds to the map
                    map.fitBounds(bounds);
                }
                
                // essentially this code hits the toggle switch from map to list view
                // it's necessary to start out the JS with the map visible, in order for
                // the getRealContentHeight() function to make an accurate calculation.
                // as soon as that's done, we can switch back to the list view
                document.getElementById('list_info').hidden = !document.getElementById('list_info').hidden;
                document.getElementById('content').hidden = !document.getElementById('content').hidden;
            }
        });
        
        // this calculates the height of the map in order to make it fit the screen
        // size perfectly, no matter what type of device you're using.
        function getRealContentHeight() {
            var header = $.mobile.activePage.find("div[data-role='header']:visible");
            var footer = $.mobile.activePage.find("div[data-role='footer']:visible");
            var content = $.mobile.activePage.find("div[data-role='content']:visible:visible");
            var viewport_height = $(window).height();
            
            var content_height = viewport_height - header.outerHeight() - footer.outerHeight();
            if((content.outerHeight() - header.outerHeight() - footer.outerHeight()) <= viewport_height) {
                content_height -= (content.outerHeight() - content.height());
            }
            content_height -= 68;       // offset because we added the toggle button
            return content_height;
        }
    </script>

</body>
</html>
