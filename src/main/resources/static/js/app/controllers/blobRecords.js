define(['jquery', 'utils/messagebox'], function($, MessageBox){
	var BlobRecords = Spine.Controller.create({
		pickedCommitHashCode: '',
		pickCommitMenu: $('#J_pickCommitPointMenu'),
		elements: {
			'#J_queryBlobRecordBtn': 'queryBlobRecordBtn',
			'#J_queryDiffBlobRecordBtn': 'queryDiffBlobRecordBtn',
			'#J_queryNumStatBtn': 'queryNumStatBtn',
			//'#J_pickCommitPointMenu': 'pickCommitMenu',
			'#J_sinceCommitPoint': 'sinceCommitPoint',
			'#J_untilCommitPoint': 'untilCommitPoint',
			'.selectAll': 'selectAllFilesCheckBox',
			'tbody': 'tbodyEl'
		},
		events: {
			'click .selectAll': 'changeSelectAll',
			'click tbody input.selectSingle': 'changeSelectSingle',
			'click #J_queryBlobRecordBtn': 'clickQueryBlobRecordBtn',
			'click #J_queryDiffBlobRecordBtn': 'clickQueryDiffBlobRecordBtn',
			'click #J_queryNumStatBtn': 'clickQueryNumStatBtn'
		},
		proxied: ['render', 'queryBlobRecords', 'changeSelectAll', 'changeSelectSingle', 'clickPickCommitBtnForDiff'],
		init: function(){
			this.initGlobalEvents();
			this.initLocalEvents();
		},
		initGlobalEvents: function(){
			var _this = this;
			Spine.App.bind('pickCommitMenu:show', this.proxy(function(obj){
				if(!obj || !obj.commitHashCode || !obj.posX || !obj.posY) {
					return;
				}
				this.showPickCommitMenu(obj.commitHashCode, obj.posX, obj.posY);
			}));
			Spine.App.bind('pickCommitMenu:hide', this.proxy(function(){
				this.hidePickCommitMenu();
			}));
		},
		initLocalEvents: function(){
			// 这是一个例外的情形
			this.pickCommitMenu.find('a').on('click', this.clickPickCommitBtnForDiff);
		},
		refresh: function(blobRecordList){
			blobRecordList && (this.blobRecordList = blobRecordList);
			this.render();
		},
		render: function(){
			this.clear();
			this.tbodyEl.append($('#J_blobRecordTmpl').tmpl(this.blobRecordList));
			this.changeSelectSingle();
		},
		clear: function(){
			this.tbodyEl.empty();
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
			this.tbodyEl.find('input[type=checkbox]').attr('checked', !!this.selectAllFilesCheckBox.attr('checked'));
		},
		changeSelectSingle: function(ev){
			var uncheckedSize = this.tbodyEl.find('input[type=checkbox]:not(:checked)').size();
			this.selectAllFilesCheckBox.attr('checked', !uncheckedSize);
		},
		clickQueryBlobRecordBtn: function(){
			this.queryBlobRecordBtn.attr('disabled', true);
			Spine.App.trigger('commitHashCode:collect', {
				callback: this.queryBlobRecords
			});
		},
		queryBlobRecords: function(commitHashCodeList){
			if(!commitHashCodeList || commitHashCodeList.length == 0) {
				this.queryBlobRecordBtn.attr('disabled', false);
				MessageBox.alertMsg('请先选中一次或多次提交!');
				return;
			}
			$.post(CTX_PATH + '/git/listBlobRecord.do', {
				workspaceDir: this._getWorkspaceDir(),
				commitHashCode: commitHashCodeList.join(',')
			}, this.proxy(function(data){
				this.queryBlobRecordBtn.attr('disabled', false);
				//data = $.parseJSON(data);
				if(!data || data.success === false) {
					return;
				}
				this.refresh(data.blobRecordList);
			}));
		},
		clickQueryDiffBlobRecordBtn: function(){
			if(!this.sinceCommitPoint.val() || !this.untilCommitPoint.val()) {
				MessageBox.alertMsg('请正确填写提交点信息!');
				return;
			}
			this.queryDiffBlobRecordBtn.attr('disabled', true);
			$.getJSON(CTX_PATH + "/git/listDiffBlobRecord.do", {
				sinceCommitHashCode: this.sinceCommitPoint.val(),
				untilCommitHashCode: this.untilCommitPoint.val(),
				workspaceDir: this._getWorkspaceDir()
			}, this.proxy(function(data){
				this.queryDiffBlobRecordBtn.attr('disabled', false);
				if(!data || data.success === false) {
					return;
				}
				this.refresh(data.blobRecordList);
			}));
		},
		clickQueryNumStatBtn: function() {
			if(!this.sinceCommitPoint.val() || !this.untilCommitPoint.val()) {
				MessageBox.alertMsg('请正确填写提交点信息!');
				return;
			}
			var params = {
				sinceCommitHashCode: this.sinceCommitPoint.val(),
				untilCommitHashCode: this.untilCommitPoint.val(),
				workspaceDir: this._getWorkspaceDir()
			};
			var paramArr = [];
			for(var key in params) {
				paramArr.push(key + '=' + encodeURIComponent(params[key]));
			}
			var url = CTX_PATH + '/git/listNumStat.do?' + paramArr.join('&');
			window.open(url, '_blank');
			/*this.queryNumStatBtn.attr('disabled', true);
			$.getJSON(CTX_PATH + "/git/listNumStat.do", {
				sinceCommitHashCode: this.sinceCommitPoint.val(),
				untilCommitHashCode: this.untilCommitPoint.val(),
				workspaceDir: this._getWorkspaceDir()
			}, this.proxy(function(data){
				this.queryNumStatBtn.attr('disabled', false);
				if(!data || data.success === false) {
					return;
				}
				console.dir(data);
			}));*/
		},
		showPickCommitMenu: function(commitHashCode, posX, posY) {
			this.pickCommitMenu.css({
				left: posX,
				top: posY
			}).show();
			this.pickedCommitHashCode = commitHashCode;
			this.pickCommitMenu.show();
		},
		hidePickCommitMenu: function(){
			this.pickCommitMenu.hide();
		},
		clickPickCommitBtnForDiff: function(ev){
			var $target = $(ev.target);
			$($target.attr('data-commit-input')).val(this.pickedCommitHashCode);
			$target.blur();
			this.hidePickCommitMenu();
		}
	});
	return BlobRecords;
});