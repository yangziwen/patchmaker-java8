(function(){
	function GitGraph(options){
		this.init(options);
	}
	$.extend(GitGraph.prototype, {
		init: function(options){
			options = this.options = options || {};
			if(!options.commits) {
				throw new Error('commits should not be null!');
			}
			this.renderTo = $(options.renderTo);
			if(this.renderTo.size() == 0){
				throw new Error('renderTo is not valid!');
			}
			this.commits = options.commits;
			var commitMap = this.commitMap = {};
			this.pathList = [];			// 树形结构用多条曲线代替
			$.each(this.commits, function(i, commit){
				commitMap[commit.hashCode] = commit;
				commit.children = [];
				commit.idx = i;
			});
			this.processParent();
			this.assignTrack();
			this.buildPath();
			this.optimizePath();
			if(options.autoRender !== false) {
				this.render();
			}
		},
		// 将parents从字符串数组转换成commit对象数组
		processParent: function(){
			var commits = this.commits,
				commitMap = this.commitMap;
			$.each(commits, function(i, commit){
				var parents = commit.textParents = commit.parents;
				if(parents && parents[0] && typeof(parents[0]) == 'string') {
					commit.parents = [];
					for(var i=0; i<parents.length; i++) {
						commit.parents.push(commitMap[parents[i]]);
					}
				}
			});
		},
		assignTrack: function(){
			var commits = this.commits;
			var rawTrackInitNum = 0;
			var leadPoints = this.leadPoints = [];	// 对于翻页后的情形，可能有多个起点
			$.each(commits, function(i, commit){
				if(!commit.rawTrack) {
					commit.rawTrack= '' + rawTrackInitNum;
					rawTrackInitNum ++;
					leadPoints.push(commit);
				}
				var pLen = commit.parents.length,
					trackList = commit.rawTrack.split(';').sort();
				var track0 = trackList[0];
				for(var i=0; i<pLen; i++) {
					var parent = commit.parents[i];
					if(!parent) {
						continue;
					}
					parent.children.push(commit);
					if(parent.rawTrack) {
						parent.rawTrack += ';';
					} else {
						parent.rawTrack = '';
					}
					parent.rawTrack += pLen == 1? track0: track0 + '|' + i;
				}
			});
			var trackMap = {};
			$.each(commits, function(i, commit){
				commit.track = $.map(commit.rawTrack.split(';'), function(track, i){
					track = track.replace(/(?:\|0)+$/, '');
					trackMap[track] = true;
					return track;
				}).sort();
			});
			// 每个commit的children按track排序
			$.each(commits, function(i, commit) {
				commit.children.sort(function(c1, c2){
					var t1 = c1.track[0],
						t2 = c2.track[0];
					if(t1 == t2) {
						return 0;
					}
					return t1 > t2? 1: -1;
				});
			});
			var trackList = $.map(trackMap, function(value, key){
				return key;
			}).sort();
			$.each(trackList, function(i, track){
				trackMap[track] = i;
			});
			this.trackMap = trackMap;
			this.trackList = trackList;
		},
		// 建立pathList列表
		buildPath: function(){
			var commits = this.commits,
				trackMap = this.trackMap,
				maxY = 0;
			if(!commits || !commits.length) {
				return;
			}
			var todoList = [];
			$.each(this.leadPoints, function(i, latestCommit){
				$.each(latestCommit.parents, function(i, parent){
					todoList.push({
						child: latestCommit,
						parent: parent
					});
				});
			});
			while(todoList.length > 0) {
				var couple = todoList.shift();
				if(!couple || !couple.child || !couple.parent) {
					continue;
				}
				var path = [];
				var commit = couple.child, 
					prevCommit = couple.parent;
				while(true) {
					commit.x = commit.idx;
					commit.y = trackMap[commit.track[0]];
					path.push(commit);
					commit.y > maxY && (maxY = commit.y);
					if(!prevCommit) {
						break;
					}
					// 如果prevCommit是一个交汇点，则需要时情况中断曲线
					if(prevCommit.children[0] != commit) {
						prevCommit.x = prevCommit.idx;
						prevCommit.y = trackMap[prevCommit.track[0]];
						prevCommit.y > maxY && (maxY = prevCommit.y);
						path.push(prevCommit);
						break;
					}
					commit = prevCommit;
					if(commit.parents.length > 1) {
						todoList.push({
							child: commit,
							parent: commit.parents[1]
						});
					}
					prevCommit = commit.parents[0];
				}
				this.pathList.push(path);
			}
			// 对路径按位置进行排序
			this.pathList = this.pathList.sort(function(p1, p2){
				var y1 = p1[1]? p1[1].y: 0,
					y2 = p2[1]? p2[1].y: 0;
				return y1 - y2;
			});
			this.maxY = maxY;
			var posMap = this.posMap = {};
			$.each(commits, function(i, commit){
				posMap[commit.x + ',' + commit.y] = commit;
			});
		},
		// 优化路径，减少所需的列数
		optimizePath: function(){
			var commits = this.commits,
				pathList = this.pathList;
			
			if(!pathList || pathList.length == 1) {
				return;
			}
			// 从底列向高列逐列处理
			var prevPath = pathList[0];		// 其实是已经处理过的所有低列的commit点数组
			var curMaxY = 0;
			for(var i=1, l=pathList.length; i<l; i++){
				var path = pathList[i];
				if(!path){
					continue;
				}
				if(path.length <= 2) {
					if(path[0].y > curMaxY + 1) {
						path[0].y = ++curMaxY;
					}
					prevPath = prevPath.concat(path);
					continue;
				}
				var pLen = path.length;
				var firstCommit = path[0];
				var secondCommit = path[1];	// 视为当path的原始列数的基准
				var lastCommit = path[pLen - 1];
				var curY = secondCommit.y;
				var maxPrevY = 0;
				for(var j=firstCommit.idx + 1; j<lastCommit.idx; j++) {
					var commit = commits[j];
					if(prevPath.indexOf(commit) == -1) {
						continue;
					}
					commit.y > maxPrevY && (maxPrevY = commit.y);
				}
				curMaxY = Math.max(maxPrevY + 1, curMaxY);
				if(maxPrevY + 1 < curY){
					if(firstCommit.y == curY) {
						firstCommit.y = maxPrevY + 1;
					}
					var isParentOfLastCommitShown = false;
					for(var k=0; k<lastCommit.parents.length; k++) {
						if(lastCommit.parents[k] != null) {
							isParentOfLastCommitShown = true;
							break;
						}
					}
					if(!isParentOfLastCommitShown) {
						lastCommit.y = maxPrevY + 1;
					}
					if(lastCommit.y > maxPrevY + 1 && !isParentOfLastCommitShown) {
						if(lastCommit.idx < commits.length -1) {
							var extraConsiderCommit = commits[lastCommit.idx + 1];
							lastCommit.y = Math.max(maxPrevY, extraConsiderCommit.y) + 1;
						} else {
							lastCommit.y = maxPrevY + 1;
						};
					} else {
//						lastCommit.y = maxPrevY + 1;
					}
					curY = Math.max(maxPrevY + 1, firstCommit.y);
					for(var j=1; j<path.length-1; j++) {
						path[j].y = curY;
					}
				}
				prevPath = prevPath.concat(path);
			}
			var posMap = this.posMap = {};
			$.each(commits, function(i, commit){
				posMap[commit.x + ',' + commit.y] = commit;
			});
			this.maxY = curMaxY;
		},
		render: function(){
			drawLineChart(this);
		}
	});

	function drawLineChart(gitGraph) {
		var pathList = gitGraph.pathList,
			renderTo = gitGraph.renderTo[0],
			posMap = gitGraph.posMap;
		var	colors = ['#4572A7','#AA4643', '#89A54E', '#80699B', '#3D96AE', '#DB843D', '#92A8CD', '#A47D7C', '#B5CA92' ],
			series = [];
		for(var i=0, l=pathList.length; i<l; i++) {
			var path = pathList[i];
			var data = [];
			for(var j=0; j<path.length; j++) {
				var commit = path[j];
				data.push({
					x: commit.x,
					y: commit.y,
					commit: commit
				});
			}
			series.push({data: data});
		}
		new Highcharts.Chart({
			chart: {
				renderTo: renderTo,
				type: 'line',
				margin: [0, 0, 0, 0],
				animation: false,
				inverted: true
			},
			colors: colors,
			tooltip: {
				formatter: function() {
						var commit = posMap[this.x + ',' + this.y];
						if(!commit) {
							return;
						}
						var comment = commit.comment;
						var cnt = 0;
						for(var i=0; i<comment.length; i++) {
							if(comment.charCodeAt(i) > 255) {
								cnt += 2;
							} else {
								cnt ++;
							}
							if(cnt > 30) {
								comment = comment.substring(0, i) + '...';
								break;
							}
						}
						return [
						    '<span>commit: ' + commit.hashCode.substring(0, 7) + '</span>',
						    (function(commit){
						    	var result = '<span>parent: ' + commit.textParents[0].substring(0, 7) + '</span>';
						    	if(commit.textParents.length > 1) {
						    		result += '<br/><span>parent: ' + commit.textParents[1].substring(0, 7) + '</span>';
						    	}
						    	return result;
						    })(commit),
						    '<span>提交者: ' + commit.commiter + '</span>'/*,
						    '<span>提交时间: ' + commit.commitTimestamp + '</span>',
						    '<span>' + comment + '</span>'*/
						].join('<br/>') ;
				}
			},
			credits: {
				enabled: false
			},
			legend: {
				enabled: false
			},
			title: {
				floating: true,
				style: {
					display: 'none'
				}
			},
			plotOptions: {
				series: {
					cursor: 'pointer',
					marker: {
						symbol: 'circle'
					},
					animation: false
				}
			},
			xAxis: {
				labels: {
					enabled: false
				},
				title: {
					text: null
				},
				lineWidth: 0
			},
			yAxis: {
				reversed: !!gitGraph.options.reversed,
				labels: {
					enabled: false
				},
				gridLineWidth: 0,
				title: {
					text: null
				},
				min: gitGraph.maxY == 0? -0.1: -gitGraph.maxY * 0.1,
				startOnTick: false,
				max: gitGraph.maxY == 0? 2: null
			},
			series: series
			
		});
	}
	if(window.define) {
		define(['jquery', 'utils/highcharts'], function($, Highcharts){
			return GitGraph;
		});
	} else {
		window.GitGraph = GitGraph;
	}
})();