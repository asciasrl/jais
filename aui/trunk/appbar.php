<?php 
$apps = array('Audio','Clima','Energia','Illuminazione','Serramenti','Sicurezza','Video'); 

echo('<script language="javascript">appbar_num='.count($apps).';</script>');

// ripete le prime 5 per creare l'effetto di circolarità
for ($i = 0; $i < 5; $i++) {
  $apps[] = $apps[$i]; 
}
?>

<div id="status" style="height: 200px;">Stato</div>

<div style="margin: 0px auto; width: 320px; height: 80px;">
<div id="appbar" style="margin: 0px auto; width: 320px; height: 80px; overflow: hidden; position: absolute;">
  <div id="scroller" style="background-color: black; position: absolute; width: 1000px; height: 80px;">
<?php
// Visualizza una icona per ogni applicazione 
foreach ($apps as $k => $app):
  $i = $k + 1;
?>  
    <div id="<?php echo("app-".$i); ?>" style="float: left; background-color: black; width: 80px; height: 80px; margin-top: 0px; overflow: hidden;">
      <img id="<?php echo("app-".$i."-img"); ?>" title="<?php echo("$app $i"); ?>" alt="<?php echo($app); ?>" width="80" height="80" border="0" src="images/<?php echo($app); ?>.png" />
    </div>  
<?php 
endforeach;    
?>  
  </div>
</div>
</div>

<script type="" language="javascript" src="appbar.js"></script>