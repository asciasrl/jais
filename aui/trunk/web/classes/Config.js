if (!AUI.Config) {
	
	AUI.Config = {
			
		pagesElement : null,
		pagesEmptyMessage : "Nessuna pagina. Aggiungere una nuova pagina dal menu.",
		newPageTitle : "Nuova pagina",
		newControlTitle : "Nuovo controllo",
		skinConfigImages : "skins/20090330/config.images/",
		skin : "",
		skinImage : "",
		newPageSrc : "aui_icon.png",
		pages : new Array(),		
		currentPageIndex : null,
		onPageClickHandler: null,
		
		snapGrid: 10,
		
		layers: ['all'],

		onMaskClickHandler: null,

		pageElement : null,
		pageEmptyMessage : "Selezionare la pagina da modificare.",
		currentControlIndex : null,

		/**
		 * Inizializza la procedura di configurazione
		 * Se l'utente e' collegato, carica dal server la configurazione
		 */
		init : function() {
			var self = this;
			
			this.pagesElement = document.getElementById("pages");

			this.pageElement = document.getElementById("page");

			try {
				this.jsonrpc = new JSONRpcClient("/aui/rpc");
				
				// test
				if (!this.jsonrpc.AUI.isLogged()) {
					//if (typeof(console) != 'undefined') this.login("utente","demo");					
				}

				if (this.jsonrpc.AUI.isLogged()) {
					this.load();
				} else {
					this.clear();			
					this.loginShow();
				}
			} catch(e) {
				alert(e.msg || e);
			}		
		},

		/**
		 * Pulisce l'interfaccia e svuota le variabili con i dati della configurazione
		 */
		clear : function() {
			this.pagesElement.innerHTML = this.pagesEmptyMessage;			
			this.pages = new Array(),		
			this.pageElement.innerHTML = this.pageEmptyMessage;
			this.currentPageIndex = null;
		},
		
		/**
		 * Carica la configurazione dal server
		 */
		load : function() {
			this.clear();
			
			this.skin = this.jsonrpc.AUIConfig.getSkin();
			this.skinImages =  this.skin + 'images/';
			
			this.loadPages();
			
			if (this.pages.length == 0) {
				this.newPage();
			}
		
			this.statusMessage("Configurazione caricata dal server");
		},
		
		/**
		 * Mostra la finestra di login
		 */
		loginShow : function() {
			var mask = document.getElementById("mask");
			mask.style.width = window.innerWidth + 'px';
			mask.style.height = window.innerHeight + 'px';
			mask.style.visibility = 'visible';
			
			var self = this;
			this.onMaskClickHandler = function(e) { self.loginHide(); };
			mask.addEventListener('click', this.onMaskClickHandler, false);
			
			var el = document.getElementById("login");
			el.style.visibility = 'visible';
		},
		
		/**
		 * Nasconde la finestra di login
		 */
		loginHide : function() {
			var err = document.getElementById("loginError");
			err.innerHTML = "";

			var el = document.getElementById("login");
			el.style.visibility = 'hidden';

			var mask = document.getElementById("mask");
			mask.removeEventListener('click', this.onMaskClickHandler, false);

			mask.style.visibility = 'hidden';
			
		},

		/**
		 * gestisce l'evento submit della form di login
		 */
		loginSubmit : function(loginForm) {
			this.loginForm = loginForm;
			var username = loginForm.username.value;
			var password = loginForm.password.value;
			var err = document.getElementById("loginError");
			err.innerHTML = "";
			try {
				var res = this.login(username,password);
				if (res) {
					this.loginHide();
				} else {
					err.innerHTML = "Nome utente o password non corretti";
				}
			} catch(e) {
				if (typeof(console) != 'undefined') console.error(e);
				err.innerHTML = e;
			}
			return false;
		},
		
		/**
		 * Fa il login al server
		 */
		login : function(username,password) {
			// TODO cifratura della password
			try {
				if (this.jsonrpc.AUI.login(username,password)) {
					this.statusMessage("Login effettuato da: "+username);
					this.jsonrpc = new JSONRpcClient("/aui/rpc");
					this.load();
					return true;
				} else {
					return false;
				}
			} catch(e) {
				alert(e.msg || e);
			}
		},
		
		logout : function() {
			try {
				if (this.jsonrpc.AUI.isLogged()) {
					this.jsonrpc.AUI.logout();
				}
				this.clear();
			} catch(e) {
				alert(e.msg || e);
			}
		},
		
		showMask : function(handler) {
			var mask = document.getElementById("mask");
			mask.style.width = window.innerWidth + 'px';
			mask.style.height = window.innerHeight + 'px';
			mask.style.visibility = 'visible';
			this.onMaskClickHandler = handler;
			mask.addEventListener('click', AUI.Config.hideMask, false);
			//return mask.getStyle("z-index").toInt();
		},
		
		hideMask : function() {
			var mask = document.getElementById("mask");
			mask.removeEventListener('click', AUI.Config.hideMask, false);
			mask.style.visibility = 'hidden';
			if (typeof(AUI.Config.onMaskClickHandler) == 'function') {
				AUI.Config.onMaskClickHandler();
				AUI.Config.onMaskClickHandler = null;
			}			
		},
		
		changeBackgroundShow : function() {
			var self = this;
			this.showMask(self.changeBackgroundHide);
			var popup = document.getElementById("changeBackground"); 
			popup.style.visibility = 'visible';
			
			var images = this.jsonrpc.AUIConfig.getImagesList().list;
			var div = popup.getElement('div');
			div.empty();
			if (typeof(console) != 'undefined') console.log("Immagini:"+images);
			for (var i=0; i < images.length; i++) {
				var image = new Element('img',{
					src: images[i], 
					title: images[i],
					events: {
						click: function(e){AUI.Config.doChangeBackground(e);} 
					}
				});
				div.grab(image);
			}
		},
		
		doChangeBackground : function(e) {
			AUI.Config.setPageSrc(e.target.title);
			AUI.Config.hideMask();
		},
		
		changeBackgroundHide : function() {
			var el = document.getElementById("changeBackground");
			el.style.visibility = 'hidden';
		},		
		
		setPageSrc : function(src) {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina da modificare");
				return;
			}
			var i = this.currentPageIndex;
			var p = this.pages[this.currentPageIndex];
			try {
				this.jsonrpc.AUIConfig.setPageSrc(p.id,src);
				p.src = src;
				this.refreshPages();
				this.refreshPage();
			} catch(e) {
				alert(e.msg || e);
			}
		},
		
		uploadShow : function() {
			var mask = document.getElementById("mask");
			mask.style.width = window.innerWidth + 'px';
			mask.style.height = window.innerHeight + 'px';
			mask.style.visibility = 'visible';
			
			var self = this;
			this.onMaskClickHandler = function(e) { self.uploadHide(); };
			mask.addEventListener('click', this.onMaskClickHandler, false);

			var err = document.getElementById("uploadError");
			err.innerHTML = "";

			var el = document.getElementById("upload");
			el.style.visibility = 'visible';
		},

		uploadHide : function() {
			var err = document.getElementById("uploadError");
			err.innerHTML = "";

			var el = document.getElementById("upload");
			el.style.visibility = 'hidden';

			var mask = document.getElementById("mask");
			mask.removeEventListener('click', this.onMaskClickHandler, false);

			mask.style.visibility = 'hidden';			
		},

		completeUploadCallback : function(response) {
			var err = document.getElementById("uploadError");
			var res = JSON.decode(response,true);
			if (res == null) {
				err.innerHTML = "Errore";
				alert(response);
			} else {
				if (res.status == "OK") {
					AUI.Config.setPageSrc(res.files[0]);
					AUI.Config.uploadHide();
				} else if (res.status == "ERROR") {
					err.innerHTML = res.errors[0];					
				}
			}
		},
		
		editPageShow : function() {
			this.showMask(this.editPageHide);
			var popup = document.getElementById("editPage"); 
			popup.style.visibility = 'visible';
			
			var page = this.pages[this.currentPageIndex];
			var inputs = popup.getElements('input');
			if (typeof(console) != 'undefined') console.log(inputs);
			for (var i=0; i < inputs.length; i++) {
				var input = inputs[i];
				if (typeof(console) != 'undefined') console.log(input);
				var name = input.getProperty("name");
				input.setProperty('value',page[name]);
			}
		
		},
		
		editPageHide : function() {
			var popup = document.getElementById("editPage"); 
			popup.style.visibility = 'hidden';			
		},
		
		editPageSubmit : function(form) {
			try {
				var p = this.pages[this.currentPageIndex];
				var title = form.title.value;
				if (title != p.title) {
					var res = this.jsonrpc.AUIConfig.setPageTitle(p.id,title);
					this.pages[this.currentPageIndex].title = title;
				}
				var newId = form.id.value;
				if (newId != p.id) {
					this.jsonrpc.AUIConfig.changePageId(p.id,newId);
					this.pages[this.currentPageIndex].id = newId;
				}
				this.hideMask();
				this.refreshPages();
			} catch(e) {
				if (typeof(console) != 'undefined') console.error(e);
				alert(e.msg || e);
			}
			
		},
		
		statusMessage : function(s) {
			if (typeof(console) != 'undefined') console.info(s);
			document.getElementById("statusbar").innerHTML = s;
		},
		
		onPageClick : function(event) {
			if (typeof(console) != 'undefined') console.log(this);			
			event.preventDefault();
			event.stopPropagation();
			var i = this.id.substring(5);
			AUI.Config.activatePage(i);
		},
		
		getPageIndex : function(pageId) {
			for (var i = 0; i < this.pages.length; i++) {
				if (this.pages[i].id == pageId) {
					return i;
				}
			}
			return undefined;
		},
		
		activatePage : function(i) {
			this.statusMessage("Selezionata pagina ["+this.pages[i].id+"] :"+this.pages[i].title);
			if (this.currentPageIndex != null) {
				this.deactivatePage(this.currentPageIndex);
			}
			// TODO usare metodo specifico di mootools
			var item = $("page-"+i);
			item.addClass("pageItemSelected");
			var pos = item.getPosition(this.pagesElement);
			this.pagesElement.scrollTo(0,this.pagesElement.getScroll().y + pos.y - 20);
			this.currentPageIndex = i;
			this.loadPage(i);
			this.drawPage(i);
		},
		
		loadPage : function(i) {	
			try {
				var pageId = this.pages[i].id;
				var controls = this.jsonrpc.AUIConfig.getPageControls(pageId);
				this.pages[i].controls = controls.list;
				var areas = this.jsonrpc.AUIConfig.getPageAreas(pageId);
				this.pages[i].areas = areas.list;
			} catch(e) {
				alert(e.msg || e);
			}
		},

		refreshPage : function() {
			this.drawPage(this.currentPageIndex);
		},
		
		reloadPage : function() {
			this.loadPage(this.currentPageIndex);
			this.drawPage(this.currentPageIndex);
		},
		
		/**
		 * Disegna la pagina con i controlli 
		 */
		drawPage : function(i) {		
			var page = this.pages[i];
			var content = new Element('div', {'id': 'page-'+page.id, 'class': 'page'});		
			content.grab(new Element('img', {'class': 'page', src: page.src} ));
			content.setStyle('display','block');
			var container = document.getElementById("page");	
			container.empty();
			container.grab(content);
			this.currentControlIndex = null;
			for (var j=0; j < page.controls.length; j++) {
				var c = page.controls[j];
				var layer = c.map['layer'];
				//var id = 'control-' + page.id + '-' + c.map['[@id]'];
				var id = 'control-' + j;
								
				// if (typeof(console) != 'undefined') console.log(c);

				var control = new Element('div', {'id': id});
				control.addClass('control');
				control.addClass('control-'+c.map['type']);
				control.setStyle('top',c.map['top']+'px');
				control.setStyle('left',c.map['left']+'px');
				if (typeof(console) != 'undefined') console.log("Control"+id+" Layer="+layer);
				if (this.layers.contains('all') || this.layers.contains(layer) || typeof(layer) == 'undefined' || layer == undefined || layer == 'undefined') {
					control.setStyle('display','block');
				}
				
				var icon = new Element('div', {'class': 'control-icon'} );
				icon.grab(new Element('img', {src: this.skin + c.map['default']} ));
				control.grab(icon);

				var caption = new Element('div', {'class': 'caption'} );
				caption.innerHTML = c.map['title'];
				var self = this;
				control.grab(caption);

				var info = new Element('div', {'class': 'info'} );
				info.addEventListener('click',function(e) { self.editControlShow(e); },false);
				
				info.grab(new Element('label').appendText("Identifier: "));
				info.appendText(c.map['[@id]']);
				info.grab(new Element('br'));
				info.grab(new Element('label').appendText("Type: "));
				info.appendText(c.map['type']);
				info.grab(new Element('br'));
				info.grab(new Element('label').appendText("Address: "));
				info.appendText(c.map['address']);
				info.grab(new Element('br'));
				info.grab(new Element('label').appendText("Layer: "));
				info.appendText(c.map['layer']);
				control.grab(info);

				control.makeDraggable({'onDrop': AUI.Config.onDropControl});
				control.addEvent('click',AUI.Config.onClickControl);
								
				content.grab(control);				
			}
		},
		
		onClickControl : function(event) {
			if (typeof(console) != 'undefined') console.log("click control "+this.id);
			var i = this.id.substring(8);
			AUI.Config.activateControl(i);
		},
		
		activateControl : function(i) {
			if (this.currentControlIndex) {
				this.deactivateControl(this.currentControlIndex);
				this.currentControlIndex = null;
			}
			try {
				$('control-'+i).addClass('controlSelected');
			} catch(e) {
				alert("Unable to activate control "+i+":" + e);
			}			
			this.currentControlIndex = i;			
			if (typeof(console) != 'undefined') console.log("activated control "+i);
		},
		
		deactivateControl : function(i) {
			try {
				$('control-'+i).removeClass('controlSelected');
				if (typeof(console) != 'undefined') console.log("deactivated control "+i);
			} catch(e) {
				alert("Unable to deactivate control "+i+":" + e);
			}
		},
		
		getNewControlId : function(type) {
			var page = this.pages[this.currentPageIndex];
			var controls = page.controls; 
			if (controls.length == 0) {
				return type + '1';
			}
			var id = controls.length+1;
			var ok = true;
			do {
				ok = true;
				for (var i=0; i < controls.length; i++) {					
					if (controls[i] == null) {
						continue;
					}
					if (controls[i].map['[@id]'] == type + id) {
						ok = false;
						break;						
					}
				}
				if (ok == false) {
					id += 1;
				}
			} while (! ok);			
			return type + id;
		},
		
		newControl : function(type) {
			var pageId = this.pages[this.currentPageIndex].id;
			var id = this.getNewControlId(type);
			var title = this.newControlTitle+" "+id;
			try {
				this.jsonrpc.AUIConfig.newPageControl(pageId,id,type,title);
				if (typeof(console) != 'undefined') console.log("added control "+id+" on page "+pageId);
				this.reloadPage();
				this.activateControl(this.pages[this.currentPageIndex].controls.length - 1);
				this.editControlShow();
			} catch(e) {
				alert(e.msg || e);
			}
		},

		setControlLayer : function(layerName) {
			try {
				var page = this.pages[this.currentPageIndex];
				var control = page.controls[this.currentControlIndex];
				var id = 'control-' + page.id + '-' + control.map['[@id]'];
				// FIXME Usare pageId e controlId separati
				this.jsonrpc.AUIConfig.setPageControlProperty(id,"layer",layerName);
				this.reloadPage();
			} catch(e) {
				alert(e.msg || e);
			}			
		},

		deleteControl : function(iPage,iControl) {
			if (typeof(console) != 'undefined') console.log("delete control "+iControl+" on page "+iPage);
			var page = this.pages[iPage];
			var control = page.controls[iControl];
			var id = 'control-' + page.id + '-' + control.map['[@id]'];
			var title = control.map['title'];
			if (title == undefined || title == "") {
				title = control.map['address'];
			}
			if (title == undefined || title == "") {
				title = control.map['type'];
			}				
			var descr = "["+page.id+"/" + control.map['[@id]'] + "] "+title;
			if (window.confirm("Rimuovere il controllo "+descr+" ?")) {
				try {
					// FIXME Usare pageId e controlId separati
					this.jsonrpc.AUIConfig.deletePageControl(id);
				} catch(e) {
					alert(e.msg || e);
					return;
				}
				this.reloadPage();
			}
		},

		onTouchControl : function() {
			if (typeof(console) != 'undefined') console.info('Touch: ' + this.id);
		},
		
		onReleaseControl : function() {
			if (typeof(console) != 'undefined') console.info('Release: ' + this.id);
		},

		onDropControl : function(element, droppable, event) {
			var top = element.getStyle('top').toInt();
			var left = element.getStyle('left').toInt();
			var i = element.id.substring(8);
			AUI.Config.moveControl(i,left,top);					
		},
		
		moveControl : function(i,left,top) {
			left = Math.round(left / this.snapGrid) * this.snapGrid;
			top = Math.round(top / this.snapGrid) * this.snapGrid;
			$('control-'+i).setStyles({'left': left + 'px', 'top': top + 'px'});
			var page = this.pages[this.currentPageIndex];
			var control = page.controls[i];
			var id = 'control-' + page.id + '-' + control.map['[@id]'];
			if (typeof(console) != 'undefined') console.log(id+" left="+left+" top="+top);			
			try {
				// FIXME Usare pageId e controlId separati
				this.jsonrpc.AUIConfig.setPageControlProperties(id,{"javaClass":"java.util.Map","map":{"top": top, "left": left}});
			} catch(e) {
				alert(e.msg || e);
			}
		},
		
		editControlShow : function(event) {
			/*
			if (typeof(console) != 'undefined') console.log("editControl: " + event);
			event.preventDefault();
			event.stopPropagation();
			*/
			this.showMask(this.editControlHide);
			var popup = document.getElementById("editControl"); 
			popup.style.visibility = 'visible';
						
			var controlMap = this.pages[this.currentPageIndex].controls[this.currentControlIndex];
			var inputs = popup.getElements('input');
			if (typeof(console) != 'undefined') console.log(inputs);
			for (var i=0; i < inputs.length; i++) {
				var input = inputs[i];
				if (typeof(console) != 'undefined') console.log(input);
				var name = input.getProperty("name");
				if (name == 'id') {
					name = '[@id]';
				}
				input.setProperty('value',controlMap.map[name]);
			}
		},
		
		editControlHide : function() {
			var popup = document.getElementById("editControl"); 
			popup.style.visibility = 'hidden';			
		},

		editControlSubmit : function(form) {
			try {				
				var page = this.pages[this.currentPageIndex];
				var control = page.controls[this.currentControlIndex];
				var id = 'control-' + page.id + '-' + control.map['[@id]'];
				
				var props = new Hash();
				//var map = new Hash();
				
				//map.set('title',form.title.value);
				props['title'] = form.title.value;
				//props['id'] = form.id.value;
				props['type'] = form.type.value;
				props['address'] = form.address.value;
				props['page'] = form.page.value;
				props['layer'] = form.layer.value;

				//delete(data.$family);
				
				props['$family'] = null;
				
				//this.jsonrpc.AUIConfig.setPageControls(id,props);
				// FIXME Usare pageId e controlId separati
				this.jsonrpc.AUIConfig.setPageControlProperties(id,{"javaClass":"java.util.Map","map":props});
				
				var newId = form.id.value;
				if (id != newId) {
					// FIXME Usare pageId e controlId separati
					this.jsonrpc.AUIConfig.changePageControlId(id,newId);
				}

				this.hideMask();
				this.reloadPage();
			} catch(e) {
				if (typeof(console) != 'undefined') console.error(e);
				alert(e.msg || e);
			}
			
		},

		deactivatePage : function(i) {
			var el = document.getElementById("page-"+i);
			el.removeClass("pageItemSelected");
			this.currentPageIndex = null;
			$('page').empty();
			$('page').appendText = this.pageEmptyMessage;
		},

		getNewPageId : function() {
			if (this.pages.length == 0) {
				return "p"+1;
			}
			var id = this.pages.length+1;
			var ok = true;
			do {
				ok = true;
				for (var i=0; i < this.pages.length; i++) {					
					if (this.pages[i] == null) {
						continue;
					}
					if (this.pages[i].id == "p" + id) {
						ok = false;
						break;						
					}
				}
				if (ok == false) {
					id += 1;
				}
			} while (! ok);			
			return "p" + id;
		},
		
		newPage : function() {			
			var id = this.getNewPageId();
			var src = null;
			var title = this.newPageTitle + " " + id;
			try {
				this.jsonrpc.AUIConfig.newPage(id,src,title);		
				this.addPage(id,src,title);
				this.refreshPages();
				this.activatePage(this.pages.length-1);
				this.editPageShow();
			} catch(e) {
				alert(e.msg || e);
			}
		},
			
		loadPages : function() {
			try {
				var pages = this.jsonrpc.AUIConfig.getPages().list;
			} catch(e) {
				this.statusMessage(e.msg);
				alert(e.msg || e);
				return;
			}
			for (var i=0; i < pages.length; i++) {
				var id = pages[i].map.id;
				var src = pages[i].map.src;
				var title = pages[i].map.title;
				this.addPage(id,src,title);
			}
			this.refreshPages();
			if (this.pages.length > 0) {
				this.activatePage(0);
			}
		},
		
		addPage : function(id,src,title) {
			var i = this.pages.length;			
			var page = new Array();
			page['id'] = id;
			page['src'] = src;
			page['title'] = title;
			this.pages.include(page);
			this.statusMessage("Aggiunta pagina ["+id+"] :"+title);
		},
		
		refreshPages : function() {
			this.pagesElement.empty();
			this.pages = this.pages.clean();
			for (var i=0; i < this.pages.length; i++) {
				var pageElement = new Element('div', {'id': "page-"+i, 'class': 'pageItem'});
				pageElement.grab(new Element('img', {'src': this.pages[i].src, 'alt': "Pagina senza sfondo"}));
				pageElement.grab((new Element('p')).appendText(this.pages[i].title));			
				pageElement.addEvent('click',this.onPageClick);
				this.pagesElement.grab(pageElement);
			}
			if (this.currentPageIndex != null) {
				$("page-"+this.currentPageIndex).addClass("pageItemSelected");
			}
		},

		deletePage : function(i) {
			var p = this.pages[i];
			var descr = "["+p.id+"] "+p.title;
			if (window.confirm("Rimuovere la pagina "+descr+" ?")) {
				try {
					this.jsonrpc.AUIConfig.deletePage(p.id);
				} catch(e) {
					alert(e.msg || e);
					return;
				}
				this.deactivatePage(i);
				//this.pagesElement.removeChild($("page-" + i));
				this.pages[i] = null;
				this.refreshPages();
				this.statusMessage("Eliminata pagina "+descr);
			}
		},
		
		showAllLayers : function() {
			this.layers = ['all'];
			this.refreshPage();
		},
		
		showLayerToggle : function(layerName) {
			if (typeof(console) != 'undefined') console.info(layerName);
			if (this.layers.contains('all')) {
				this.layers.empty();
			}
			if (this.layers.contains(layerName)) {
				this.layers.erase(layerName);				
			} else {
				this.layers.include(layerName);
			}
			this.refreshPage();
		},

		save : function() {
			try {
				this.jsonrpc.AUIConfig.save();
			} catch(e) {
				alert(e.msg || e);
			}
		},
		
		/**
		 * ********************** COMANDI ***********************
		 */
		
		cmdOpen : function() {
			this.loginShow();
		},

		cmdSave : function() {
			this.save();
		},

		cmdSaveAs : function() {
			alert("Non implementato");
		},

		cmdClose : function() {
			this.logout();
		},
		
		cmdExit : function() {
			this.logout();
			window.location.href="/";
		},

		cmdNewPage : function() {
			this.newPage();
		},

		cmdUploadBackground : function() {
			// TODO Gestire upload di piu' tipi
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina da eliminare");
			} else {
				this.uploadShow();
			}
		},

		cmdChangeBackground : function() {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina di cui si vuole modificare lo sfondo");
			} else {
				this.changeBackgroundShow();
			}
		},
		
		cmdRenamePage : function() {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina di cui si vuole modificare il nome");
			} else {
				this.editPageShow();
			}
		},
		
		cmdDeletePage : function() {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina che si vuole eliminare");
			} else {
				this.deletePage(this.currentPageIndex);
			}
		},
		
		cmdNewControl : function(type) {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina su cui aggiungere il controllo");
			} else {
				this.newControl(type);
			}
		},
		
		cmdChangeControlLayer : function(layerName) {
			if (this.currentControlIndex) {
				this.setControlLayer(layerName);
			} else {
				alert("Selezionare prima il controllo da modificare");				
			}
		}, 
		
		cmdChangeControlType : function() {
			if (this.currentControlIndex) {
				this.editControlShow();
			} else {
				alert("Selezionare prima il controllo da modificare");				
			}
		},
		
		cmdChangeControlIcon : function() {
			alert("Non implementato");
		},
		
		cmdChangeControlAddress : function() {
			if (this.currentControlIndex) {
				this.editControlShow();
			} else {
				alert("Selezionare prima il controllo da modificare");				
			}
		},		
		
		cmdRenameControl : function() {
			if (this.currentControlIndex) {
				this.editControlShow();
			} else {
				alert("Selezionare prima il controllo da modificare");				
			}
		},
		
		cmdDeleteControl : function() {
			if (this.currentPageIndex == null) {
				alert("Selezionare prima la pagina di cui si vuole eliminare");
			} else {
				this.deleteControl(this.currentPageIndex, this.currentControlIndex);
			}
		},
				
		cmdShowAllLayers : function() {
			$('checkLayer-all').empty();
			this.layers.each(function(item){
				$('checkLayer-'+item).empty();
			});
			this.showAllLayers();
			new Element('img',{src: this.skinConfigImages + 'tick.png'}).inject('checkLayer-all');
		},
		
		cmdShowLayerToggle : function(layerName) {
			$('checkLayer-all').empty();
			$('checkLayer-'+layerName).empty();
			this.showLayerToggle(layerName);
			if (this.layers.length == 0) {
				this.cmdShowAllLayers();
			} else if (this.layers.contains(layerName)) {
				new Element('img',{src: this.skinConfigImages + 'tick.png'}).inject('checkLayer-'+layerName);
			}
		},
		
		cmdChangePassword : function() {
			alert("Non implementato");
		}

	};	
}
