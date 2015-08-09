define(['jquery', 'controllers/manage', 'controllers/folderTree'], function($, Manage, FolderTree){
	var App = Spine.Controller.create({
		el: $('#J_app'),
		elements: {
			'#J_folderTree': 'folderTreeEl',
			'#J_manage': 'manageEl'
		},
		init: function(){
			// 只显示当前页
			this.bind('hash:change', this.proxy(function(curElName){
				this.currentEl && (this.currentEl.hide());
				this.currentEl = this[curElName];
				this.currentEl.show();
			}));
			// url路由
			this.routes({
				'/folderTree': this.proxy(function(){
					if(!this.folderTree) {
						this.folderTree = FolderTree.init({el: this.folderTreeEl});
					}
					this.trigger('hash:change', 'folderTreeEl');
				}),
				'/manage?workspaceDir=:workspaceDir': this.proxy(function(workspaceDir){
					if(!this.manage) {
						this.manage = Manage.init({el: this.manageEl});
					} else {
						this.manage.refresh();
					}
					this.trigger('hash:change', 'manageEl');
				})
			});
		}
	});
	return App;
});