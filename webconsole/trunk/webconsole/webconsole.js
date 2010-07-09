var webconsole = {
	rpc : null,
	init : function() {
		this.rpc = new JSONRpcClient("/webconsole/rpc");
		this.load();
  	},
  	
  	load : function() {
  		this.rpc.Webconsole.getAllPorts(webconsole.loadCb);
  	},
  	
  	loadCb : function(result,err) {
  		if (err) {
  			alert(err.msg);
  		} else {
  			var ports = $("ports");
  			ports.empty();
  			
			var header = new Element('div', {'class': 'header'} );
			header.innerHTML = result.list.length + " porte.";
			ports.grab(header);
			
			for (var i = 0; i < result.list.length; i++) {
				var portElement = new Element('div', {'id': 'port-'+i, 'class': 'port'} );
				var map = result.list[i].map;
				
				var addressElement = new Element('div', {'class': 'address'});
				addressElement.innerHTML = map.Address;
				portElement.grab(addressElement);

				var valueElement = new Element('div', {'class': 'value'});
				valueElement.innerHTML = map.Value;
				portElement.grab(valueElement);
				
				buttonsElement = new Element('div', {'class': 'buttons'});

				if (map.Class == "it.ascia.ais.port.NullPort") {
					var bottone = new Element('button', {'class': 'azione'} );
					bottone.innerHTML = "Aziona";
					bottone.addEventListener('click',function(e) { webconsole.writePortValue(map.Address,'true'); },false);
					buttonsElement.grab(bottone);
				} else if (map.Class  == "it.ascia.ais.port.DigitalOutputPort") {
					var bottoneOn = new Element('button', {'class': 'azione'} );
					bottoneOn.innerHTML = "ON";
					bottoneOn.addEventListener('click',function(e) { webconsole.writePortValue(map.Address,'on'); },false);
					buttonsElement.grab(bottoneOn);
					var bottoneOff = new Element('button', {'class': 'azione'} );
					bottoneOff.innerHTML = "OFF";
					bottoneOff.addEventListener('click',function(e) { webconsole.writePortValue(map.Address,'off'); },false);
					buttonsElement.grab(bottoneOff);
				}

				portElement.grab(buttonsElement);				

				ports.grab(portElement);				
			}

  		}
  	},
  		
	writePortValue : function(address, value) {
  		this.rpc.Webconsole.writePortValue(webconsole.writePortValueCb, address, value);  			
	},
	
	writePortValueCb : function(result,err) {
  		webconsole.result = result;
  		webconsole.err = err;
  		if (err) {
  			alert(err.message);
  		}
  		if (result == false) {
  			alert("Non eseguito");
  		}
	}
	
}