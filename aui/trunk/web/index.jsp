<%
String ua = request.getHeader( "User-Agent" );
boolean iPod = ( ua != null && ua.indexOf( "iPod" ) != -1 );
boolean iPad = ( ua != null && ua.indexOf( "iPad" ) != -1 );
boolean iPhone = ( ua != null && ua.indexOf( "iPhone" ) != -1 );
boolean SymbianOS = ( ua != null && ua.indexOf( "SymbianOS" ) != -1 );
boolean Android = ( ua != null && ua.indexOf( "Android" ) != -1 );
%>
<% if (iPod | iPhone | iPad | SymbianOS | Android) { %>
<%@ include file="aui.jsp" %>
<% } else { %>
<html>
<head>
<title>Ascia User Interface - iPod simulation</title>
</head>
<body>
<div style="margin: 0px auto; width: 598px; height: 743px;">
	<div style="z-index: 1; position: absolute;"><img src="itouch/touch-ipod_480x320_03_nero.png"></div>
	<div style="z-index: 2; position: absolute; margin-left: 139px; margin-top: 107px;">
		<!-- Barra di sistema dell iPod -->
		<div id="iPod-system" style="width: 320px; height: 20px;">
			<div style="position: absolute;"><img src="itouch/barrastato_ipod.png" /></div>
			<div id="iPod-clock" style="position: absolute; margin: 0px 120px; margin-top: 1px; height: 20px; width: 80px; text-align: center; color: white; font-family: Arial; font-size: 14px;">--:--</div>
		</div>
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
		<iframe width="320" 
			height="460" 
			style="position: absolute; top: 20px;" frameborder="0" 
			marginwidth="0" marginheight="0"
			scrolling="no" 
			style="position:absolute; background-color: black;" src='aui.jsp?nomobile=1'>
		</iframe>
	</div>
</div>
</body>
</html>
<% } %>
  