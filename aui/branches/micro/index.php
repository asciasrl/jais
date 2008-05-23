<?php
require_once("config.php");
require_once("custom/config.php");

/**
 * Stiamo su un iPod/iPhone?
 */
$mobile = !((strpos($_SERVER['HTTP_USER_AGENT'], "iPod") === false) &&
	(strpos($_SERVER['HTTP_USER_AGENT'], "iPhone") === false));
// $mobile = true;
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
	   "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" lang="it_IT">
<head>
<title>Ascia User Interface</title>
<link href="aui.css" rel="stylesheet" type="text/css"/>
<?php
if ($mobile) {
?>
<meta name = "viewport" content = "width=device-width, maximum-scale=4" />
<style type="text/css">
body {
	margin: 0px;
	-webkit-tap-highlight-color:rgba(1,1,1,0.5);
}
</style>
<?php
}
?>
</head>
<body>
<?php
	include('aui.php');
?>
</body>
</html>
