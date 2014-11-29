<!DOCTYPE html>
    <html>
    <head>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet" href="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.css">
    <link href="css/custom.css" rel="stylesheet">
    <script src="http://code.jquery.com/jquery-1.10.2.min.js"></script>
    <script src="http://maps.google.com/maps/api/js?sensor=false"></script>
    <script src="http://code.jquery.com/mobile/1.4.2/jquery.mobile-1.4.2.min.js"></script>
    
    <style type="text/css">
                input[type=checkbox]
                {
                 /* Double-sized Checkboxes */
                 -ms-transform: scale(2); /* IE */
                 -moz-transform: scale(2); /* FF */
                 -webkit-transform: scale(2); /* Safari and Chrome */
                 -o-transform: scale(2); /* Opera */
                }
    </style>
    </head>
    <body>

<!-- grab the GPS coordinates, if possible -->
<script type = text/javascript>
    var onSuccess = function(position) {
        document.getElementById("hiddengps").value +=
                position.coords.latitude + ', ' +
                position.coords.longitude;
    };
    function onError(error) {}
    navigator.geolocation.getCurrentPosition(onSuccess, onError);
</script>

<div data-role="page" id="home">
    <div data-role="header">
        
        <a href="#" class="ui-btn ui-btn-inline" id="back-button" data-rel="back">Back</a>  <!-- remove on index.php -->        
        <a href="#popupAbout" data-position-to="#back-button" data-rel="popup" data-transition="pop" data-role="button" class="ui-btn ui-btn-inline" >About</a>
        <!--about popup. will not be displayed unless the button is clicked-->
        <div data-role="popup" id="popupAbout" style="left: 0;" class="ui-content infoPopup">
            <a href='#' data-rel='back' class='ui-btn ui-corner-all ui-shadow ui-btn-a ui-icon-delete ui-btn-icon-notext ui-btn-right'>Close</a>
                <h3>About</h3>
                <p>This project was created for Princeton COS 333 by Ante Qu,
                David Kong, Lisa Lee, and Ming-Yee Tsang.</p>
        </div>
        
    </div>
    
    <div data-role="main" class="ui-content">
        <a class="ui-link"> <img id="logo" src="/img/ft_logo_small.png"> </a>
        
        <div id='form'>
            <form action="results.php" method="post" data-ajax="false">
                <label> <h3> Use GPS? </h3> </label>
                <input type="checkbox" class="style1" checked="checked" name="gps" id="hiddengps" value="" onChange="document.getElementById('city').value = ''"> <h3> Or tell us where you are: </h3>
                <input type="text" name="town" id="city" placeholder="Type city/state, or address" onkeydown="document.getElementById('hiddengps').checked = false">
                
                <h3> What's your party size? </h3>
                <label for="slider-0"></label>
                <input type="range" name="party" id="slider-0" value="4" min="1" max="12">
                
                <div id="submit" class="btn-wrapper">
                    <input type="submit" data-transition="slidedown" value="Find Table!">                    
                </div>
            </form>
        </div>
    </div> <!-- end main content -->
    
    <div data-role="footer">
        <h3>Created by Ante, David, Lisa, and Ming-Yee.</h3>
    </div>
</div>

</body>
</html>
