<?php
/**
 * @version		1.0
 * @package		Archibit
 * @subpackage	Corsi
 * @copyright	Copyright (C) 2010 Sergio Strampelli
 */

// Check to ensure this file is included in Joomla!
defined('_JEXEC') or die( 'Restricted access' );

jimport('joomla.application.component.model');

/**
 * Archibit Corsi Component Corso Model
 *
* @version		1.0
* @package		Archibit
* @subpackage	Corsi
 */
class CorsiModelCorso extends JModel
{
	/**
	 *  id corso
	 *
	 * @var int
	 */
	var $_id = null;

	/**
	 * dati del corso
	 *
	 * @var array
	 */
	var $_data = null;

	/**
	 * Constructor
	 *
	 * @since 1.5
	 */
	function __construct()
	{
		parent::__construct();

		$id = JRequest::getVar('id', 0, '', 'int');
		$this->setId((int)$id);
	}

	/**
	 * Method to set the corso identifier
	 *
	 * @access	public
	 * @param	int corso identifier
	 */
	function setId($id)
	{
		// Set corso id and wipe data
		$this->_id		= $id;
		$this->_data	= null;
	}

	/**
	 * Method to get a corso
	 *
	 * @since 1.5
	 */
	function &getData()
	{
		// Load the corso data
		if ($this->_loadData())
		{
			// Initialize some variables
			$user = &JFactory::getUser();

			// Make sure the corso is published
			if (!$this->_data->published) {
				JError::raiseError(404, JText::_("Resource Not Found"));
				return false;
			}

			// Check to see if the category is published
			if (!$this->_data->cat_pub) {
				JError::raiseError( 404, JText::_("Resource Not Found") );
				return;
			}

			// Check whether category access level allows access
			if ($this->_data->cat_access > $user->get('aid', 0)) {
				JError::raiseError( 403, JText::_('ALERTNOTAUTH') );
				return;
			}
		}
		else  $this->_initData();

		return $this->_data;
	}

	/**
	 * Method to increment the hit counter for the corso
	 *
	 * @access	public
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function hit()
	{
		if ($this->_id)
		{
			$corso = & $this->getTable();
			$corso->hit($this->_id);
			return true;
		}
		return false;
	}

	/**
	 * Tests if corso is checked out
	 *
	 * @access	public
	 * @param	int	A user id
	 * @return	boolean	True if checked out
	 * @since	1.5
	 */
	function isCheckedOut( $uid=0 )
	{
		if ($this->_loadData())
		{
			if ($uid) {
				return ($this->_data->checked_out && $this->_data->checked_out != $uid);
			} else {
				return $this->_data->checked_out;
			}
		}
	}

	/**
	 * Method to checkin/unlock the corso
	 *
	 * @access	public
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function checkin()
	{
		if ($this->_id)
		{
			$corso = & $this->getTable();
			if(! $corso->checkin($this->_id)) {
				$this->setError($this->_db->getErrorMsg());
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Method to checkout/lock the corso
	 *
	 * @access	public
	 * @param	int	$uid	User ID of the user checking the article out
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function checkout($uid = null)
	{
		if ($this->_id)
		{
			// Make sure we have a user id to checkout the article with
			if (is_null($uid)) {
				$user	=& JFactory::getUser();
				$uid	= $user->get('id');
			}
			// Lets get to it and checkout the thing...
			$corso = & $this->getTable();
			if(!$corso->checkout($uid, $this->_id)) {
				$this->setError($this->_db->getErrorMsg());
				return false;
			}

			return true;
		}
		return false;
	}

	/**
	 * Method to store the corso
	 *
	 * @access	public
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function store($data)
	{
		$row =& $this->getTable();

		// Bind the form fields to the web link table
		if (!$row->bind($data)) {
			$this->setError($this->_db->getErrorMsg());
			return false;
		}

		// Create the timestamp for the date
		$row->date = gmdate('Y-m-d H:i:s');

		// if new item, order last in appropriate group
		if (!$row->id) {
			$where = 'catid = ' . (int) $row->catid ;
			$row->ordering = $row->getNextOrder( $where );
		}
		// Make sure the corso table is valid
		if (!$row->check()) {
			$this->setError($this->_db->getErrorMsg());
			return false;
		}

		// Store the corso table to the database
		if (!$row->store()) {
			$this->setError($this->_db->getErrorMsg());
			return false;
		}

		return true;
	}

	/**
	 * Method to load content corso data
	 *
	 * @access	private
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function _loadData()
	{
		// Lets load the content if it doesn't already exist
		if (empty($this->_data))
		{
			$query = 'SELECT c.*, cc.title AS category,' .
					' cc.published AS cat_pub, cc.access AS cat_access'.
					' FROM #__corsi AS c' .
					' LEFT JOIN #__categories AS cc ON cc.id = c.catid' .
					' WHERE c.id = '. (int) $this->_id;
			$this->_db->setQuery($query);
			$this->_data = $this->_db->loadObject();
			return (boolean) $this->_data;
		}
		return true;
	}

	/**
	 * Method to initialise the corso data
	 *
	 * @access	private
	 * @return	boolean	True on success
	 * @since	1.5
	 */
	function _initData()
	{
		// Lets load the content if it doesn't already exist
		if (empty($this->_data))
		{
			$corso = new stdClass();
			$corso->id					= 0;
			$corso->catid				= 0;
			$corso->sid					= 0;
			$corso->title				= null;
			$corso->url					= null;
			$corso->description			= null;
			$corso->date				= null;
			$corso->hits				= 0;
			$corso->published			= 0;
			$corso->checked_out			= 0;
			$corso->checked_out_time 	= 0;
			$corso->ordering			= 0;
			$corso->archived			= 0;
			$corso->approved			= 0;
			$corso->params				= null;
			$corso->category			= null;
			$this->_data				= $corso;
			return (boolean) $this->_data;
		}
		return true;
	}
}