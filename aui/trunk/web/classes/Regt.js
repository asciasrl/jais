if (!Regt) {
	
	Regt = {
	
		onMouseOver : function(event) {
				event.preventDefault();
				event.stopPropagation();
				console.log("x:"+event.clientX+" y:"+event.clientY);
		},
	}
}		
