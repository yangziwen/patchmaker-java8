define(['jquery', 'utils/messagebox'], function($, MessageBox){
	var GitOperation = Spine.Controller.create({
		elements: {
			'#J_openFolderBtn': 'openFolderBtn',		// 打开当前目录的文件夹
			'#J_openGitBashBtn': 'openGitBashBtn',		// 打开当前目录的Git命令行窗口
			'#J_openWinCmdBtn': 'openWinCmdBtn',		// 打开当前目录的Cmd命令行窗口
			'#J_branchSel': 'branchSel',				// 用户切换分支的select
			'#J_pageBarTop': 'pageBarTopEl'
		},
		events: {
			'click #J_openFolderBtn': 'openFolder',
			'click #J_openGitBashBtn': 'openGitBash',
			'click #J_openCmdWinBtn': 'openCmdWin',
			'change #J_branchSel': 'changeBranch'
		},
		proxied: [],
		init: function(){
			this.branchList = [];
			this.refresh();
		},
		refresh: function(){
			this.refreshBranch();
			this.refreshOpenFolderBtn();
			this.refreshPageBar();
		},
		refreshBranch: function(){
			var url = CTX_PATH + '/git/branch/list?workspaceDir=' + encodeURIComponent(this._getWorkspaceDir());
			$.getJSON(url, this.proxy(function(data){
				if(data.success === false) {
					return;
				}
				data.branchList && (this.branchList = data.branchList);
				var optionList = $(data.branchList).map(function(i, v){
					return '<option ' + (v.active === true? 'selected="selected"': '') + '>' + v.name + '</option>';
				}).toArray();
				this.branchSel.empty();
				this.branchSel.html(optionList.join(''));
			}));
		},
		refreshPageBar: function(){
			this.start = this.start || 0;
			this.limit = this.limit || 40;
			this.totalCount = 1;
			var workspaceDir = this._getWorkspaceDir();
			if(workspaceDir) {
				var url = CTX_PATH + '/git/commit/count';
				$.getJSON(url, {
					workspaceDir: this._getWorkspaceDir()
				} , this.proxy(function(data){
					this.totalCount = parseInt(data.totalCount);
					this._buildPageBar();
				}));
			} else {
				this._buildPageBar();
			}
		},
		_buildPageBar: function(){
			var curPage = Math.floor(this.start / this.limit) + 1;
			var totalPage = Math.floor(this.totalCount / this.limit) + (this.totalCount % this.limit > 0? 1: 0);
			this.pageBarTopEl.empty();
			this.pageBarTopEl.bootstrapPageBar({
				curPageNum: curPage,
				totalPageNum: totalPage,
				maxBtnNum: 10,
				pageSize: this.limit,
				siblingBtnNum: 2,
				paginationCls: 'pagination-right',
				click: this.proxy(function(i, pageNum){
					this.start = (pageNum - 1) * this.limit;
					Spine.App.trigger('page:change', {
						success: true,
						start: this.start,
						limit: this.limit
					});
				})
			});
		},
		refreshOpenFolderBtn: function(){
			var workspaceDir = this._getWorkspaceDir();
			if(workspaceDir) {
				this.openFolderBtn.html(decodeURIComponent(this._getWorkspaceDir()));
			}
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
		},
		// 打开目录文件夹
		openFolder: function(){
			var url = CTX_PATH + '/file/folder/open?folderPath=' + encodeURIComponent(this._getWorkspaceDir());
			$.getJSON(url, function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开文件夹!');
				}
			});
		},
		// 打开目录的Git命令行
		openGitBash: function(){
			var url = CTX_PATH + '/file/gitbash/open?folderPath=' + encodeURIComponent(this._getWorkspaceDir());
			$.getJSON(url, function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开Git命令行窗口!');
				}
			});
		},
		// 打开目录的Cmd命令行
		openCmdWin: function(){
			var url = CTX_PATH + '/file/cmdwin/open?folderPath=' + encodeURIComponent(this._getWorkspaceDir());
			$.getJSON(url, function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开命令窗口!');
				}
			});
		},
		changeBranch: function(){
			var url = CTX_PATH + '/git/branch/change';
			$.post(url, {
				workspaceDir: this._getWorkspaceDir(),
				branch: this.branchSel.val()
			}, this.proxy(function(data){
				//data = $.parseJSON(data);
				Spine.App.trigger('branch:change', data);
			}));
		}
	});
	return GitOperation;
});