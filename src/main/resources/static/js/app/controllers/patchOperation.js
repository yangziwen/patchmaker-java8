define(['jquery', 'utils/messagebox'], function($, MessageBox){
	
	var PatchOperation = Spine.Controller.create({
		elements: {
			'#J_patchRootPathTxt': 'patchRootPathTxt',
			'#J_createPatchBtn': 'createPatchBtn',
			'#J_openPatchFolderBtn': 'openPatchFolderBtn',
			'#J_openWebreleaseFolderBtn': 'openWebreleaseFolderBtn',
			'#J_useOptimizedSel': 'useOptimizedSel'
		},
		events: {
			'click #J_openPatchFolderBtn': 'openPatchFolder',
			'click #J_openWebreleaseFolderBtn': 'openWebreleaseFolder',
			'click #J_createPatchBtn': 'createPatch'
		},
		init: function(){
			this.refresh();
		},
		refresh: function(){
			this.refreshPatchRootPath();
		},
		refreshPatchRootPath: function(){
			var workspaceDir = this._getWorkspaceDir();
			if(this.workspaceDir == workspaceDir) {
				return;
			}
			this.workspaceDir = workspaceDir;
			var url = CTX_PATH + '/file/patchpath/recommend';
			$.getJSON(url, {
				workspaceDir: this._getWorkspaceDir()
			}, this.proxy(function(data){
				if(data.success === false) {
					return;
				}
				this.patchRootPathTxt.val(data.recommendedPatchPath);
			}));
		},
		openPatchFolder: function(){
			var patchFolder = this._getPatchFolder();
			if(!patchFolder) {
				return;
			}
			var url = CTX_PATH + '/file/folder/open?folderPath=' + encodeURIComponent(patchFolder);
			$.getJSON(url, function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开文件夹!');
				}
			});
		},
		openWebreleaseFolder: function(){
			var folderPath = 'W:/public/WebDev/Web release';
			$.getJSON(CTX_PATH + '/file/folder/open?folderPath=' + encodeURIComponent(folderPath), function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开文件夹!');
				}
			});
		},
		createPatch: function(){
			this.createPatchBtn.attr('disabled', true);
			var srcFiles = $('#J_blobRecords tbody tr').map(function(i, v){
				var $tdList = $(v).children();
				var $checkbox = $tdList.first().children(':checked');
				if($checkbox.size() == 0){
					return;
				}
				return $tdList.last().html();
			}).toArray();
			if(!srcFiles || srcFiles.length == 0) {
				MessageBox.alertMsg('未选中任何文件!');
				this.createPatchBtn.attr('disabled', false);
				return;
			}
			if(!this.patchRootPathTxt.val()) {
				MessageBox.alertMsg('补丁输出路径有误!');
				return;
			}
			$.post(CTX_PATH + '/patch/patch/create', {
				filePaths: srcFiles.join(','),
				patchDir: this.patchRootPathTxt.val(),
				gitRootDir: this._getWorkspaceDir(),
				useOptimized: this.useOptimizedSel.val() == 1? true: false
			}, this.proxy(function(data){
				//var data = $.parseJSON(json);
				if(data.success === true) {
					MessageBox.alertMsg('补丁创建成功!');
				} else {
					MessageBox.alertMsg(data.message || '补丁创建失败!');
				}
				this.createPatchBtn.attr('disabled', false);
			}));
		},
		_getPatchFolder: function(){
			var folderPath = this.patchRootPathTxt.val();
			if(!folderPath) {
				MessageBox.alertMsg('请先输入补丁输出目录!');
				return null;
			}
			return getParentFolderPath(folderPath);
		},
		_getWorkspaceDir: function(){
			return decodeURIComponent(this._getRequestParam()['workspaceDir']);
		},
		_getRequestParam: function(){
			var hash = window.location.hash;
			if(!hash) {
				return '';
			}
			var hashPrefix = '?';
			var idx = hash.indexOf(hashPrefix) + hashPrefix.length;
			var paramStr = hash.substring(idx);
			var paramArr = paramStr.split('&');
			var param = {};
			for(var i=0, l=paramArr.length; i<l; i++) {
				var entry = paramArr[i].split('=');
				if(entry.length == 2) {
					param[entry[0]] = entry[1];
				}
			}
			return param;
		}
	});
	
	function getParentFolderPath(filePath) {
		if(!filePath) {
			return '';
		}
		filePath = filePath.replace(/\\/g, '/');
		return filePath.substring(0, filePath.lastIndexOf('/', filePath.length - 2));
	}
	
	return PatchOperation;
	
});