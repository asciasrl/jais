<?php
/**
 * 
 */
defined( '_JEXEC' ) or die( 'Restricted access' );

?>
<?php if ( $this->params->get( 'show_page_title', 1 )) : ?>
	<h2 class="componentheading<?php echo $this->escape($this->params->get('pageclass_sfx')); ?>">
		<?php echo $this->params->get( 'page_title' ); ?>
	</h2>
<?php endif; ?>
Titolo: <?php echo $this->corso->title; ?><br />
Descrizione: <?php echo $this->corso->description; ?>