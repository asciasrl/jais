<?php
defined( '_VALID_MOS' ) or die( 'Restricted access' );
// needed to seperate the ISO number from the language file constant _ISO
$iso = explode( '=', _ISO );
// xml prolog
echo '<?xml version="1.0" encoding="'. $iso[1] .'"?' .'>';
?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">

<!-- Copyright 2007 Ascia S.r.l. design Gianluca Balzarini programming Sergio Strampelli -->

<head>
<?php mosShowHead(); ?>
<?php
$images = $mosConfig_live_site . '/templates/' . $mainframe->getTemplate() . '/images/';
?>
<meta http-equiv="Content-Type" content="text/html; <?php echo _ISO; ?>" />
<link href="<?php echo $mosConfig_live_site . '/templates/' . $mainframe->getTemplate();?>/css/template_css.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<div style="height: 80px; border-bottom: 1px solid rgb(28,78,125);">
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td><img alt="ASCIA Home Page" width="135" height="75" border="0" src="<?php echo $images; ?>logo_top_sx.gif" /></td>
    <td><span class="contentheading"><?php echo $GLOBALS['mosConfig_sitename']; ?></span>
    	<br /><span style="font-size: 10px;"><?php echo $_SERVER['HTTP_REFERER'];?>
    	<br/><?php echo mosCurrentDate( '%d/%m/%Y %H:%M:%S' ); ?></span></td>
  </tr>
</table>
</div>
    
<?php mosMainBody(); ?>

<div style="border-top: 1px solid rgb(28,78,125);">
	<div style="text-align: center;">&copy; <?php echo mosCurrentDate( '%Y' ) . ' ' . $GLOBALS['mosConfig_sitename'];?> - C.F. e P.IVA 09506451005</div>
</div>


<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-2212913-2";
urchinTracker();
</script>

</body>
</html>