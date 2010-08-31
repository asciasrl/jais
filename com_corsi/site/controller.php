<?php
/**
 * @version		1.0
 * @package		Archibit
 * @subpackage	Corsi
 * @copyright	Copyright (C) 2010 Sergio Strampelli
*/

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die( 'Restricted access' );

jimport('joomla.application.component.controller');

/**
 * Archibit Corsi Component Controller
 *
 * @package		Archibit
 * @subpackage	Corsi
 * @since 1.5
 */
class CorsiController extends JController
{
	/**
	 * Method to show a corsi view
	 *
	 * @access	public
	 * @since	1.5
	 */
	function display()
	{
		// Set a default view if none exists
		if ( ! JRequest::getCmd( 'view' ) ) {
			JRequest::setVar('view', 'corsi' );
		}

		//update the hit count for the weblink
		if(JRequest::getCmd('view') == 'corso')
		{
			$model =& $this->getModel('corso');
			$model->hit();
		}
		
		// View caching logic -- simple... are we logged in?
		$user = &JFactory::getUser();
		$view = JRequest::getVar('view');
		$viewcache = JRequest::getVar('viewcache', '1', 'POST', 'INT');
		if ($user->get('id') || ($view == 'category' && $viewcache == 0)) {
			parent::display(false);
		} else {
			parent::display(true);
		}
	}
}
