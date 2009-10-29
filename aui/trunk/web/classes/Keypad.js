
AUI.Keypad = {
	rpc : null,
	pin : "",
	skin : "skins/20090330/"
};

AUI.Keypad.draw = function(id) {
	var keypad = new Element('div',{'id': id});
	keypad.addClass("keypad");
	
	var screen = new Element('div');
	screen.addClass("keypadScreen");
	screen.innerHTML = "&nbsp;";
	keypad.grab(screen);
	
	var keys = new Element('div');
	keys.addClass("keypadKeys");
	var keysList = ['1','2','3','on','4','5','6','x','7','8','9','back','asterisk','0','sharp','ok'];
	this.kk = keysList;
	for (var i=0; i < keysList.length; i++) {
		var k = keysList[i];
		AUI.Logger.log("k="+k);
		//var key = new Element('div');
		var img = new Element('img',{'src': this.skin+'images/key_'+k+'.png'});
		img.setProperty('key',k);
		//key.grab(img);
		keys.grab(img);
	}
	keys.addEventListener('click', function(ev) { AUI.Keypad.key(ev); }, false);
	keypad.grab(keys);
	
	document.body.appendChild(keypad);	
};

AUI.Keypad.show = function() {
	if (document.getElementById('keypad') == null) {
		AUI.Keypad.draw('keypad');	
	}
	if ($('keypad').getStyle('display') == 'block') {		
		return;
	}
	this.pin = "";
	this.update();
	$("keypad").setStyles({'left': (AUI.getScrollX() + (AUI.getInnerWidth() - 320) / 2),
    'top' :	(window.innerHeight - 356) / 2, 'display': 'block'});
	//$("keypad").style.display = 'block';		
	/*
	var myEffect = new Fx.Morph('keypad', {duration: 'normal', transition: Fx.Transitions.Sine.easeOut});
	 
	myEffect.start({
	    'height': [0, 356],
	    'width': 320,
	    'opacity': [0,1],
	    'display': 'block',
	    'left': (AUI.getScrollX() + (AUI.getInnerWidth() - 320) / 2),
	    'top' :	(window.innerHeight - 356) / 2
	});
	*/

};
	
AUI.Keypad.hide = function() {
	/*
	var myEffect = new Fx.Morph('keypad', {duration: 'normal', transition: Fx.Transitions.Sine.easeOut});
	 
	myEffect.complete = function() { $("keypad").style.display = 'none'; };
	myEffect.start({
	    'height': 0,
	    'opacity': [1,0],	    
	});
	*/
	$("keypad").style.display = 'none';		
};
	
AUI.Keypad.key = function(ev) {
	this.lastEvent = ev;
	var key = ev.target.getProperty('key');
	if (this.rpc == null) {
		this.rpc = new JSONRpcClient("/aui/rpc");
	}
	AUI.Logger.info("key="+key);
	switch(key) {
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case '0':
			this.pin += key;
			break;
		case '*':
			this.pin += '*';
			break;
		case 'sharp':
			this.pin += '#';
			break;
		case 'back':
			var l = this.pin.length;
			if (l > 1) {
				this.pin = this.pin.slice(0, l - 1);
			} else {
				this.pin = "";
			}
			break;
		case 'x':
			this.hide();
			break;
		case 'ok':
			if (this.rpc.Alarm.pin(this.pin)) {
				this.rpc.Alarm.toggle();
				/*
				if (this.rpc.Alarm.isArmed()) {
					alert("Alarm enabled. Hurry out!");
				} else {
					alert("Alarm disarmed. Welcome!");
				}
				*/
				this.hide();
			} else {
				alert("Invalid PIN");
			}
			this.pin = "";
			break;
		default: // FIXME: che cosa devono fare gli altri tasti?
			break;
	}
	this.update();
};
	
AUI.Keypad.update = function() {
	AUI.Logger.info("pin="+this.pin);
	var s = "";
	for (var i =0; i < this.pin.length; i++) {
		s += "*";
	}
	if (s == "") {
		s = "Pin + ok";
	}
	$("keypad").getChildren()[0].innerHTML = s;		
};
