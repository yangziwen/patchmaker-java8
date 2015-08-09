define(['jquery'], function($){
	var Commits = Spine.Controller.create({
		elements: {
			'.selectAll': 'selectAllCheckBox',
			'tbody': 'tbodyEl',
			'#J_graphWrapper': 'graphWrapper'
		},
		events: {
			'click .selectAll': 'changeSelectAll',
			'click tbody input.selectSingle': 'changeSelectSingle',
			'mousedown tbody tr': 'callPickCommitMenu',
			'contextmenu tbody': 'disableContextMenu'
		},
		proxied: ['render', 'renderGraph', 'changeSelectAll', 'changeSelectSingle'],
		init: function(){
			this.initGlobalEvents();
			this.reload(0, 40);
		},
		initGlobalEvents: function(){
			var _this = this;
			Spine.App.bind('commitHashCode:collect', function(obj){
				if(!obj || !obj.callback) {
					return;
				}
				obj.callback(_this.collectCommitHashCode());
			});
		},
		refresh: function(commits){
			commits && (this.commits = commits);
			this.render();
			this.renderGraph();
		},
		render: function(){
			this.clear();
			this.tbodyEl.append($('#J_commitTmpl').tmpl(this.commits));
		},
		renderGraph: function(){
			this.graphWrapper.css({height: 28.5 * this.commits.length});
			require(['utils/gitgraph'], $.proxy(function(GitGraph){
				new GitGraph({
					commits: this.commits,
					renderTo: this.graphWrapper,
					reversed: true
				});
			}, this));
		},
		clear: function(){
			this.tbodyEl.empty();
		},
		reload: function(start, limit){
			var _this = this;
			var workspaceDir = this._getWorkspaceDir();
			if(!workspaceDir) {
				return;
			}
			$.getJSON(CTX_PATH + '/git/listCommitRecord.do', {
				workspaceDir: workspaceDir,
				start: start,
				limit: limit
			}, function(data){
				if(!data || data.success === false) {
					return;
				}
				var commits = data.commits;
				for(var i=0, l=commits.length; i<l; i++) {
					var commit = commits[i];
				}
				_this.refresh(data.commits);
			});
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
		changeSelectAll: function(ev){
			this.tbodyEl.find('input[type=checkbox]').attr('checked', !!this.selectAllCheckBox.attr('checked'));
		},
		changeSelectSingle: function(ev){
			var uncheckedSize = this.tbodyEl.find('input[type=checkbox]:not(:checked)').size();
			this.selectAllCheckBox.attr('checked', !uncheckedSize);
		},
		collectCommitHashCode: function(){
			return commitHashCodeList = this.tbodyEl.find('input[type=checkbox]:checked')
				.map(function(i, v){return v.value;})
				.toArray();
		},
		callPickCommitMenu: function(ev){
			if(ev.button != 2) {
				Spine.App.trigger('pickCommitMenu:hide');
				return;
			}
			var $tr = $(ev.currentTarget);
			var commitHashCode = $tr.children(':nth(1)').html();
			Spine.App.trigger('pickCommitMenu:show', {
				commitHashCode: commitHashCode,
				posX: ev.pageX,
				posY: ev.pageY
			});
		},
		disableContextMenu: function(ev){
			ev.preventDefault();
		}
	});
	return Commits;
});