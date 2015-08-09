(function(global){
	
	function initMessageBox($) {
		
		var idSuffix = '_' + $.now();
		
		function alertMsg(msg) {
			var deferred = $.Deferred();
			var opt = {width: 350};
			if($.isPlainObject(msg)) {
				$.extend(opt, msg);
			} else {
				opt.message = msg;
			}
			var $modal = $('#J_alertModal' + idSuffix);
			if($modal.size() == 0) {
				$modal = initAlertMsg();
			}
			msg = ('' + opt.message).replace(/\n/g, '<br/>');
			$modal.find('.modal-body p').html(msg);
			$modal.modal().css({
				width: opt.width,
				'margin-left': function() {
					return - $(this).width() / 2;
				},
				'margin-top': function() {
					return ( $(window).height() - $(this).height() ) / 3;	 // 乱诌的一句，完全没有道理，太神奇了
				}
			});
			$modal.on('hidden', function(){
				$(this).off('hidden');
				deferred.resolve();
			});
			return deferred.promise();
		}
		
		function initAlertMsg() {
			return $([
				'<div id="J_alertModal' + idSuffix + '" class="modal hide" tabindex="-1" data-backdrop="static">',
					'<div class="modal-header" style="font-weight: bold; font-size: 16px;">',
						'<button type="button" class="close" data-dismiss="modal">×</button>',
						'提示',
					'</div>',
					'<div class="modal-body" style="font-weight: bold; font-size: 16px; text-align: center;">',
						'<p></p>',
					'</div>',
					'<div class="modal-footer">',
						'<button class="btn btn-primary" data-dismiss="modal">确定</button>',
					'</div>',
				'</div>'
			].join('')).appendTo(document.body);
		}
		
		function confirmMsg(msg) {
			var deferred = $.Deferred();
			var opt = {width: 350};
			if($.isPlainObject(msg)) {
				$.extend(opt, msg);
			} else {
				opt.message = msg;
			}
			var $modal = $('#J_confirmModal' + idSuffix);
			if($modal.size() == 0) {
				$modal = initConfirmMsg();
			}
			msg = ('' + opt.message).replace(/\n/g, '<br/>');
			$modal.find('.modal-body p').html(msg);
			$modal.modal().css({
				width: opt.width,
				'margin-left': function() {
					return - $(this).width() / 2;
				},
				'margin-top': function() {
					return ( $(window).height() - $(this).height() ) / 3;	 // 乱诌的一句，完全没有道理，太神奇了
				}
			});
			$modal.on('click', '.modal-footer .confirm', function(){
				$modal.off('click');
				$modal.modal('hide');
				deferred.resolve(true);
			});
			$modal.on('click', '.modal-footer .cancel, .modal-header .close', function(){
				$modal.off('click');
				$modal.modal('hide');
				deferred.resolve(false);
			});
			return deferred.promise();
		}
		
		function initConfirmMsg() {
			return $([
				'<div id="J_confirmModal' + idSuffix + '" class="modal hide" tabindex="-1" data-backdrop="static">',
					'<div class="modal-header" style="font-weight: bold; font-size: 16px;">',
						'<button type="button" class="close" data-dismiss="modal">×</button>',
						'确认',
					'</div>',
					'<div class="modal-body" style="font-weight: bold; font-size: 16px; text-align: center;">',
						'<p></p>',
					'</div>',
					'<div class="modal-footer">',
						'<button class="btn btn-primary confirm" data-dismiss="modal">确定</button>',
						'<button class="btn cancel" data-dismiss="modal">取消</button>',
					'</div>',
				'</div>'
			].join()).appendTo(document.body);
		}
		
		return {
			alertMsg: alertMsg,
			confirmMsg: confirmMsg
		};
	}
	
	if(global.define) {
		global.define(['jquery'], function($){
			return initMessageBox($);
		});
	} else {
		global.MessageBox = initMessageBox(global.jQuery);
	}
	
})(window);