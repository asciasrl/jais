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
if ( $my->id ) {
  initEditor();
}
$images = $mosConfig_live_site . '/templates/' . $mainframe->getTemplate() . '/images/';

$right = false;

if ((mosCountModules('right'))) {
  $right = true;
} else {
	for ($i = 1; $i <= 7; $i++) {
	  if (mosCountModules('user'.$i)) {
	    $right = true;
	    break;
	  }    
	}
}

?>
<meta http-equiv="Content-Type" content="text/html; <?php echo _ISO; ?>" />
<link href="<?php echo $mosConfig_live_site . '/templates/' . $mainframe->getTemplate();?>/css/template_css.css" rel="stylesheet" type="text/css"/>
</head>
<body>
<div id="main">
  <div id="mainbox">
    <table border="0" cellpadding="0" cellspacing="0">
      <tr id="top_row">
        <td width="135"><a href="<?php echo sefRelToAbs("index.php"); ?>"><img alt="ASCIA Home Page" width="135" height="75" border="0" src="<?php echo $images; ?>logo_top_sx.gif" /></a></td>
        <td width="460" align="center" valign="bottom"><?php mosLoadModules('topmenu', -1); ?></td>
        <td width="135" valign="bottom"><?php mosLoadModules('search', -1); ?><br/></td>
      </tr>
    </table>
        
    <div class="banner"><?php mosLoadModules('banner', -1); ?></div>
    
    <table class="body" cellpadding="0" cellspacing="0" border="0">
    <tr><td width="135" valign="top" class="left">
      <!-- left -->      
  
      <?php mosLoadModules('left', -1); ?>
  
      <!-- /left -->
      
      <p>
      <a href="index.php?option=com_content&amp;task=view&amp;id=5&amp;Itemid=20"><img src="<?php echo $images; ?>contattaci.jpg" alt="Contattaci" width="135" height="103" border="0" /></a>
      </p>           

      <p>
      <a target="_blank" href="images/stories/ascia/ascia_brochure.pdf"><img src="images/stories/ascia/scarica_brochure.jpg" alt="Scarica la nostra brochure di presentazione" width="135" height="103" border="0" /></a>
      </p>           

    </td>
    <?php if ($right) : ?>
    <td width="450" valign="top" class="bodysx">
    <?php else: ?>
    <td width="585" valign="top" class="bodyfull">
    <?php endif;?>
    
      <div id="pathway"><?php mosPathWay(); ?></div>
      
      <!-- mainBody -->

      <?php mosMainBody(); ?>

      <?php if (mosCountModules('related')) mosLoadModules('related', 0); ?>

      <!-- /mainBody -->
    </td>
    <?php if ($right) : ?>
	<!-- right -->

    <td width="135" valign="top" class="right">

     <?php mosLoadModules('right', 0); ?>

     <table width="135" border="0" cellpadding="0" cellspacing="0" class="news">
     <?php for ($iuser = 1; $iuser <= 7; $iuser++):
        if (mosCountModules('user'.$iuser)): ?>    
	      <tr><td valign="top">
	        <div class="news_up"></div>
	        <div class="news_central">
		      <!-- user -->

		      <?php mosLoadModules('user'.$iuser, 0); ?>
		
		      <!-- /user -->
	        </div>
	        <div class="news_down"></div>
	      </td></tr>
      <?php endif; endfor; ?>
      </table>
    </td>
    <!-- /right -->
    <?php endif; ?> 
    </tr>
    </table>

	<div class="footer">
		<div style="text-align: center;">&copy; <?php echo mosCurrentDate( '%Y' ) . ' ' . $GLOBALS['mosConfig_sitename'];?> - C.F. e P.IVA 09506451005</div>
		<div style="text-align: center;">  <?php mosLoadModules( 'footer', -1 );?></div>
		<p>
		    <a href="http://validator.w3.org/check?uri=referer">		    
		    <img src="<?php echo $images; ?>valid-xhtml10-blue.png" alt="Valid XHTML 1.0 Transitional" height="31" width="88" border="0"/>		        
            <img alt="Valid CSS2" src="<?php echo $images; ?>valid-css2-blue.png"  height="31" width="88" border="0"/>            
            </a>		        
		</p>
	</div>
	
  <?php mosLoadModules( 'debug', -1 );?>

  </div>
</div>

<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">
</script>
<script type="text/javascript">
_uacct = "UA-2212913-2";
urchinTracker();
</script>



</body>
</html>