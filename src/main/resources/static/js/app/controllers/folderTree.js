define(['jquery', 'ztree/jquery.ztree', 'utils/messagebox', 'jquery/jquery.cookie'], function($, ztree, MessageBox){
	var FolderTree = Spine.Controller.create({
		elements: {
			'#J_workspacePathTxt': 'workspacePathTxt'
		},
		events: {
			'click #J_openFolderBtn': 'openFolder',
			'click #J_toRepoBtn': 'goToRepo'
		},
		proxied: [],
		init: function(){
			var folderPath = decodeURIComponent($.cookie('lastTimeWorkspace') || '');
			folderPath && this.workspacePathTxt.val(folderPath);
			this.buildTree(folderPath);
			$.getJSON(CTX_PATH + '/git/getVersion.do', function(data){
				if(!data.version) {
					MessageBox.alertMsg('请先正确安装git bash，并将其bin目录配置到环境变量中!');
				}
			});
		},
		refresh: function(){},
		buildTree: function(folderPath){
			initFileTree(folderPath);
		},
		openFolder: function(){
			var workspacePath = this.workspacePathTxt.val();
			if(!workspacePath) {
				MessageBox.alertMsg('请选中目录!');
				return;
			}
			var url = CTX_PATH + '/file/openFolder.do?folderPath=' + encodeURIComponent(workspacePath);
			$.getJSON(url, function(data){
				if(data.success === false) {
					MessageBox.alertMsg(data.message || '未能成功打开文件夹!');
				}
			});
		},
		goToRepo: function(){
			var workspacePath = this.workspacePathTxt.val();
			workspacePath = decodeURIComponent(workspacePath);
			if(!workspacePath) {
				MessageBox.alertMsg('请选中目录!');
				return;
			}
			var url = CTX_PATH + '/file/findGitRootDir.do';
			$.getJSON(url, {
				workspaceDir: workspacePath
			}, function(data){
				if(data.success === false || !data.gitRootDir) {
					MessageBox.alertMsg(data.message);
					return;
				}
				$.cookie('lastTimeWorkspace', workspacePath || '', {expires: 365});
				location.hash = '/manage?workspaceDir=' + encodeURIComponent(data.gitRootDir);
			});
		}
	});
	
	function getFileNameFromPath(filePath) {
		if(!filePath) {
			return '';
		}
		return filePath.substring(filePath.lastIndexOf('/', filePath.length - 2) + 1);
	}
	
	function initFileTree(fullFolderPath) {
		var zTreeObj = null;
		var folderNameArr = fullFolderPath
			? $.grep(fullFolderPath.split(/\/|\\/), function(folderName){
				return folderName;
			})
			: [];
		var setting = {
			async: {
				enable: true,
				url: function(treeId, treeNode) {
					var url = CTX_PATH + '/file/getFileNodes.do';
					var param = {
						nodeId: treeNode.id,
						filePath: encodeURIComponent(treeNode.filePath)
					};
					var paramArr = [];
					for(var key in param) {
						paramArr.push(key + "=" + param[key]);
					}
					return url + (paramArr.length > 0? "?" + paramArr.join('&'): '');
				},
				view: {
					expandSpeed: ''
				},
				data: {
					simpleData: {
						enable: true
					}
				}
			},
			callback: {
				onClick: function(event, treeId, treeNode){
					$('#J_workspacePathTxt').val(treeNode.filePath);
				},
				onExpand: function(event, treeId, curNode){
					if(!zTreeObj) {
						return;
					}
					if(!folderNameArr || !folderNameArr.length) {
						return;
					}
					doExpandNode(zTreeObj, curNode, folderNameArr);
				}
			}	
		};
		
		$.getJSON(CTX_PATH + '/file/getFolderInfo.do?folderPath=root', function(data){
			if(!data || data.isValid !== true || !data.subFolderPaths) {
				MessageBox.alertMsg('目录树初始化失败!');
				return;
			}
			var zNodes = [];
			var subFolderPaths = data.subFolderPaths;
			for(var i=0, l=subFolderPaths.length; i<l; i++) {
				var folderPath = subFolderPaths[i];
				zNodes.push({
					id: '' + i,
					name: getFileNameFromPath(folderPath),
					isParent: true,
					filePath: folderPath
				});
			}
			zTreeObj = $.fn.zTree.init($("#J_fileTree"), setting, zNodes);
			doExpandNode(zTreeObj, null, folderNameArr);
		});
	}
	
	function doExpandNode(zTreeObj, curNode, folderNameArr) {
		if(!folderNameArr || !folderNameArr.length) {
			return;
		}
		var children = curNode? curNode.children: zTreeObj.getNodes();
		if(!children || !children.length) {
			return;
		}
		var folderName = folderNameArr.shift().toLowerCase();
		!curNode && (folderName += '/');
		var filteredNodes = $.grep(children, function(node){
			return node.name.toLowerCase() == folderName;
		});
		if(!filteredNodes || !filteredNodes.length) {
			return;
		}
		var nodeToExpand = filteredNodes[0];
		if(folderNameArr.length > 0) {
			zTreeObj.expandNode(nodeToExpand, true, false, true, true);
		} else {
			zTreeObj.selectNode(nodeToExpand, false);
		}
	}
	return FolderTree;
});