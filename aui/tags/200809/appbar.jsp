<%!
/**
 * Copyright (C) 2008 ASCIA Srl
 */
%>
<script language="javascript">
const SERVICES = [<%
// Da array Java a Javascript.                  
for (int i = 0; i < apps.length; i++) {
	out.print("\"" + apps[i] + "\", ");
} %>];

appbar_num=<%= apps.length %>;
</script><%

String repeatedApps[];
if (!APPBAR_SIMPLE) {
	// ripete le prime 5 per creare l'effetto di circolarita'
	repeatedApps = new String[apps.length + 5];
	for (int i = 0; i < repeatedApps.length; i++) {
		repeatedApps[i] = apps[i % apps.length]; 
	}
} else {
	repeatedApps = apps;
}
%>

<div id="appbar" style="margin: 0px auto; width: 320px; height: 80px; overflow: hidden; position: absolute;">
  <div id="scroller" style="background-color: black; position: absolute; width: 1000px; height: 80px;">
<%
// Visualizza una icona per ogni applicazione
for (int k = 0; k < repeatedApps.length; k++) {
	int i = k + 1;
	String app = repeatedApps[k];
%>  
    <div id="<%= "app-" + i %>" style="float: left; background-color: black; width: 80px; height: 80px; margin-top: 0px; overflow: hidden;" service="<%= app %>">
      <img id="<%= "app-" + i + "-img" %>" 
      	title="<%= app + " " + i %>" alt="<%= app %>" 
      	width="80" height="80" border="0" 
      	src="images/<%= app %>.png" />
    </div>  
<% 
}
%>  
  </div>
</div>