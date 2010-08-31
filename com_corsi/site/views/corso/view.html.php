<?php
/**
 */

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die( 'Restricted access' );

jimport( 'joomla.application.component.view');

/**
 * HTML View class for the Corsi component
 *
 * @static
 * @package		Joomla
 * @subpackage	Corsi
 * @since 1.0
 */
class CorsiViewCorso extends JView
{

	function __construct()       {
		$config = array();
		$config['layout'] = 'corso';
		parent::__construct($config);
	}

	function display($tpl = null)
	{
		global $mainframe;

		// Get the page/component configuration
		$params = &$mainframe->getParams();

		$document	=& JFactory::getDocument();

		$corso	=& $this->get('data');
		$this->assignRef('corso',		$corso);

		// Set page title
		$menus	= &JSite::getMenu();
		$menu	= $menus->getActive();

		// because the application sets a default page title, we need to get it
		// right from the menu item itself
		if (is_object( $menu )) {
			$menu_params = new JParameter( $menu->params );
			if (!$menu_params->get( 'page_title')) {
				$params->set('page_title',	JText::_( 'Corso') .' - '.JText::_($corso->title) );
			}
		} else {
			$params->set('page_title',	JText::_( 'Corso') .' - '.JText::_($corso->title) );
		}

		$document->setTitle( $params->get( 'page_title' ) );

		$this->assignRef('params',		$params);

		parent::display($tpl);
	}

}
?>
