<?php
/**
 * @package		Archibit
 * @subpackage	Corsi
 * @since 1.5
*/

// Check to ensure this file is included in Joomla!
defined( '_JEXEC' ) or die( 'Restricted access' );

jimport( 'joomla.application.component.view');

/**
 * HTML View class for the Archibit Corsi component
 *
 * @static
 * @package		Archibit
 * @subpackage	Corsi
 * @since 1.5
 */
class CorsiViewCorsi extends JView
{
	function display( $tpl = null)
	{
		global $mainframe;

		$document =& JFactory::getDocument();

		$corsi	=& $this->get('data');
		$total		=& $this->get('total');
		$state		=& $this->get('state');

		// Get the page/component configuration
		$params = &$mainframe->getParams();

		$menus	= &JSite::getMenu();
		$menu	= $menus->getActive();

		// because the application sets a default page title, we need to get it
		// right from the menu item itself
		if (is_object( $menu )) {
			$menu_params = new JParameter( $menu->params );
			if (!$menu_params->get( 'page_title')) {
				$params->set('page_title',	JText::_( 'Corsi Archibit' ));
			}
		} else {
			$params->set('page_title',	JText::_( 'Corsi Archibit' ));
		}

		$document->setTitle( $params->get( 'page_title' ) );

		// Set some defaults if not set for params
		$params->def('comp_description', JText::_('CORSI_DESC'));

		// Define image tag attributes
		if ($params->get('image') != -1)
		{
			if($params->get('image_align')!="")
				$attribs['align'] = $params->get('image_align');
			else
				$attribs['align'] = '';
			$attribs['hspace'] = 6;

			// Use the static HTML library to build the image tag
			$image = JHTML::_('image', 'images/stories/'.$params->get('image'), JText::_('Corsi Archibit'), $attribs);
		}

		for($i = 0; $i < count($corsi); $i++)
		{
			$corso =& $corsi[$i];
			$corso->link = JRoute::_('index.php?option=com_corsi&view=corso&id='. $corso->slug);

			// Prepare category description
			$corso->description = JHTML::_('content.prepare', $corso->description);
		}

		$this->assignRef('image',		$image);
		$this->assignRef('params',		$params);
		$this->assignRef('corsi',	$corsi);

		parent::display($tpl);
	}
}
?>
