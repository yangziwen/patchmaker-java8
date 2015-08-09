define(['jquery', 'controllers/commits', 'controllers/blobRecords', 'controllers/gitOperation', 'controllers/patchOperation'], 
		function($, Commits, BlobRecords, GitOperation, PatchOperation){
	var Manage = Spine.Controller.create({
		elements: {
			'#J_gitOperation': 'gitOperationEl',
			'#J_commits': 'commitsEl',
			'#J_blobRecords': 'blobRecordsEl',
			'#J_patchOperation': 'patchOperationEl'
		},
		init: function(){
			this.initGlobalEvent();
			this.gitOperation = GitOperation.init({el: this.gitOperationEl});
			this.commits = Commits.init({el: this.commitsEl});
			this.blobRecords = BlobRecords.init({el: this.blobRecordsEl});
			this.patchOperation = PatchOperation.init({el: this.patchOperationEl});
		},
		refresh: function(){
			this.gitOperation.refresh();
			this.commits.reload();
			this.blobRecords.clear();
			this.patchOperation.refresh();
		},
		initGlobalEvent: function(){
			Spine.App.bind('branch:change page:change', this.proxy(function(obj){
				if(this.gitOperation){
					this.gitOperation.start = obj.start;
					this.gitOperation.limit = obj.limit;
					this.gitOperation.refresh();
				}
				if(obj.success === false) {
					return;
				}
				this.commits && this.commits.reload(obj.start, obj.limit);
				this.blobRecords && this.blobRecords.clear();
			}));
		}
	});
	return Manage;
});