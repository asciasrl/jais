<%
String ua = request.getHeader( "User-Agent" );
boolean iPod = ( ua != null && ua.indexOf( "iPod" ) != -1 );
boolean iPhone = ( ua != null && ua.indexOf( "iPhone" ) != -1 );
%>
<html>
<% if (iPod | iPhone) { %>
<head>
<title>Ascia User Interface</title>
<link href="aui.css" rel="stylesheet" type="text/css"/>
<meta name = "viewport" content = "width=device-width, maximum-scale=4" />
<style type="text/css">
body {
	margin: 0px;
	-webkit-tap-highlight-color:rgba(1,1,1,0.5);
}
</style>
</head>
<body>
<%@ include file="aui.jsp" %>
</body>
<% } else { %>
<title>Ascia User Interface - iPod simulation</title>
</head>
<body>
<div style="margin: 0px auto; width: 598px; height: 743px;">
	<div style="z-index: 1; position: absolute;"><img src="images/touch-ipod_480x320_03_nero.png"></div>
	<div style="z-index: 2; position: absolute; margin-left: 139px; margin-top: 107px;">
		<!-- Barra di sistema dell iPod -->
		<div id="iPod-system" style="width: <%= IPOD_VIEWPORT_WIDTH %>px; height: 20px;">
			<div style="position: absolute;"><img src="images/barrastato_ipod.png" /></div>
			<div id="iPod-clock" style="position: absolute; margin: 0px 120px; margin-top: 1px; height: 20px; width: 80px; text-align: center; color: white; font-family: Arial; font-size: 14px;">--:--</div>
		</div>
		<div style="position:absolute; top: 20px;"><img src="images/safari-url_text_field.png" /></div>
  		<script language="javascript">
  			iPodClockObj = document.getElementById('iPod-clock');
			function iPodClock() {
  				var date = new Date();
   	 			iPodClockObj.innerHTML = date.getHours() + ":" + (date.getMinutes()< 10 ? "0" : "") + date.getMinutes(); 
				setTimeout("iPodClock()", 1000);
			}
			iPodClock();
		</script>
		<!-- Fine Barra di sistema dell iPod -->  
		<iframe width="<%= IPOD_VIEWPORT_WIDTH %>" 
			height="<%= IPOD_VIEWPORT_HEIGHT %>" 
			style="position: absolute; top: 80px;" frameborder="0" 
			marginwidth="0" marginheight="0" 
			style="position:absolute; background-color: black;" src='aui.jsp?nomobile=1'>
		</iframe>
		<div style="position:absolute; top: <%= IPOD_VIEWPORT_HEIGHT + 80 %>px;"><img src="images/safari-button_bar.png" /></div>
	</div>
	<div style="z-index: 2; position: absolute; margin-left: 259px; margin-top: 580px; width: 80px; height: 80px;" onClick="vai('screensaver');"></div>
</div>
</body>
<% } %>
</html>
  