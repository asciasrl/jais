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

