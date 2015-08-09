require(['jquery', 'app/app'], function($, App){
	window.app = App.init();
	if(!location.hash) {
		location.hash = '/folderTree';
	}
	Spine.Route.setup();
});