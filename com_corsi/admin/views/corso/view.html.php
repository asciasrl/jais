<?php
/**
*/

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die( 'Restricted access' );

jimport( 'joomla.application.component.view');

/**
 * HTML View class for the Corso component
 *
 * @static
 * @package		Joomla
 * @subpackage	Corsi
 * @since 1.0
 */
class CorsiViewCorso extends JView
{
	function display($tpl = null)
	{
		global $mainframe;

		if($this->getLayout() == 'form') {
			$this->_displayForm($tpl);
			return;
		}

		//get the corso
		$corso =& $this->get('data');

		parent::display($tpl);
	}

	function _displayForm($tpl)
	{
		global $mainframe, $option;

		$db		=& JFactory::getDBO();
		$uri 	=& JFactory::getURI();
		$user 	=& JFactory::getUser();
		$model	=& $this->getModel();


		$lists = array();

		//get the corso
		$corso	=& $this->get('data');
		$isNew		= ($corso->id < 1);

		// fail if checked out not by 'me'
		if ($model->isCheckedOut( $user->get('id') )) {
			$msg = JText::sprintf( 'DESCBEINGEDITTED', JText::_( 'Il Corso' ), $corso->title );
			$mainframe->redirect( 'index.php?option='. $option, $msg );
		}

		// Edit or Create?
		if (!$isNew)
		{
			$model->checkout( $user->get('id') );
		}
		else
		{
			// initialise new record
			$corso->published = 1;
			$corso->approved 	= 1;
			$corso->order 	= 0;
			$corso->catid 	= JRequest::getVar( 'catid', 0, 'post', 'int' );
		}

		// build the html select list for ordering
		$query = 'SELECT ordering AS value, title AS text'
			. ' FROM #__corsi'
			. ' WHERE catid = ' . (int) $corso->catid
			. ' ORDER BY ordering';

		$lists['ordering'] 			= JHTML::_('list.specificordering',  $corso, $corso->id, $query );

		// build list of categories
		$lists['catid'] 			= JHTML::_('list.category',  'catid', $option, intval( $corso->catid ) );
		// build the html select list
		$lists['published'] 		= JHTML::_('select.booleanlist',  'published', 'class="inputbox"', $corso->published );

		//clean corso data
		JFilterOutput::objectHTMLSafe( $corso, ENT_QUOTES, 'description' );

		$file 	= JPATH_COMPONENT.DS.'models'.DS.'corso.xml';
		$params = new JParameter( $corso->params, $file );

		$this->assignRef('lists',		$lists);
		$this->assignRef('corso',		$corso);
		$this->assignRef('params',		$params);
		
		parent::display($tpl);
	}
}
