<?php
/**
 * @version		1.0
 * @package		Archibit
 * @subpackage	Corsi
 * @copyright	Copyright (C) 2010 Sergio Strampelli
 */

// Check to ensure this file is included in Joomla!
defined( '_JEXEC' ) or die( 'Restricted access' );

jimport('joomla.application.component.model');

/**
 * Corsi Component Categories Model
 *
 * @package		Archibit
 * @subpackage	Corsi
 * @since 1.0
 */
class CorsiModelCorsi extends JModel
{
	/**
	 * Categories data array
	 *
	 * @var array
	 */
	var $_data = null;

	/**
	 * Categories total
	 *
	 * @var integer
	 */
	var $_total = null;

	/**
	 * Constructor
	 *
	 * @since 1.5
	 */

	function __construct()
	{
		parent::__construct();

	}

	/**
	 * Method to get corso item data for the category
	 *
	 * @access public
	 * @return array
	 */
	function getData()
	{
		// Lets load the content if it doesn't already exist
		if (empty($this->_data))
		{
			$query = $this->_buildQuery();
			$this->_data = $this->_getList($query);
		}

		return $this->_data;
	}

	/**
	 * Method to get the total number of corsi items for the category
	 *
	 * @access public
	 * @return integer
	 */
	function getTotal()
	{
		// Lets load the content if it doesn't already exist
		if (empty($this->_total))
		{
			$query = $this->_buildQuery();
			$this->_total = $this->_getListCount($query);
		}

		return $this->_total;
	}

	function _buildQuery()
	{
		$user =& JFactory::getUser();
		$aid = $user->get('aid', 0);

		//Query to retrieve all categories that belong under the corsi section and that are published.
		$query = 'SELECT cc.title as category, a.*, '
			.' CASE WHEN CHAR_LENGTH(a.alias) THEN CONCAT_WS(\':\', a.id, a.alias) ELSE a.id END as slug'
			.' FROM #__corsi AS a'
			.' LEFT JOIN #__categories AS cc ON a.catid = cc.id'
			.' WHERE a.published = 1'
			.' AND section = \'com_corsi\''
			.' AND cc.published = 1'
			.' AND cc.access <= '.(int) $aid
			.' ORDER BY cc.ordering, a.ordering';

		return $query;
	}
}
?>
