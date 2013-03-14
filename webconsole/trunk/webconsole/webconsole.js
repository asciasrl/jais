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
				
				var addressElement = new Element('div', {'class': 'address' });
				addressElement.innerHTML = map.Address;
				portElement.grab(addressElement);

				var typeElement = new Element('div', {'class': 'type' });
				typeElement.innerHTML = map.SimpleClassName;
				portElement.grab(typeElement);

				var descriptionElement = new Element('div', {'class': 'description' });
				descriptionElement.innerHTML = map.Description;
				portElement.grab(descriptionElement);

				var valueElement = new Element('div', {'class': 'value'});
				valueElement.innerHTML = map.Value;
				portElement.grab(valueElement);
				
				buttonsElement = new Element('div', {'class': 'buttons'});
				var addr = map.Address;

				if (map.ClassName == "it.ascia.ais.port.ScenePort" || map.ClassName == "it.ascia.ais.port.NullPort") {
					var bottone = new Element('button', {'class': 'azione', 'onclick': "webconsole.writePortValue('"+addr+"','true');"} );
					bottone.innerHTML = "Aziona";
					buttonsElement.grab(bottone);
				} else if (map.ClassName  == "it.ascia.ais.port.DigitalOutputPort") {
					var bottoneOn = new Element('button', {'class': 'azione', 'id': 'port-' + i + '-on', 'onclick': "webconsole.writePortValue('"+addr+"','on');"} );
					bottoneOn.innerHTML = "ON";
					buttonsElement.grab(bottoneOn);
					var bottoneOff = new Element('button', {'class': 'azione', 'id': 'port-' + i + '-off', 'onclick': "webconsole.writePortValue('"+addr+"','off');"} );
					bottoneOff.innerHTML = "OFF";
					buttonsElement.grab(bottoneOff);
				} else if (map.ClassName  == "it.ascia.ais.port.StatePort") {
					var tags = map.Tags.split(";");
					for (var j = 0; j < tags.length; j++) {
						var tag = tags[j];
						var bottone = new Element('button', {'class': 'azione', 'onclick': "webconsole.writePortValue('"+addr+"','"+tag+"');"} );
						bottone.innerHTML = tag;
						buttonsElement.grab(bottone);
						//alert(bottone);
					}
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
  			if (err.message) {
  				alert(err.message);
  			}
  			if (err.msg) {
  				alert(err.msg);
  			}  			
  		}
  		if (result == false) {
  			alert("Non eseguito");
  		}
	}
	
}