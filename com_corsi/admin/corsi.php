<?php
/**
 * @version		1.0
 * @package		Archibit
 * @subpackage	Corsi
 * @copyright	Copyright (C) 2010 Sergio Strampelli
 */

// no direct access
defined( '_JEXEC' ) or die( 'Restricted access' );

/*
 * Make sure the user is authorized to view this page
 */
$user = & JFactory::getUser();
/*
if (!$user->authorize( 'com_corsi', 'manage' )) {
	$mainframe->redirect( 'index.php', JText::_('ALERTNOTAUTH') );
}
*/

// Require the base controller
require_once (JPATH_COMPONENT.DS.'controller.php');

$controller	= new CorsiController( );

// Perform the Request task
$controller->execute( JRequest::getCmd('task'));
$controller->redirect();