package net.yangziwen.patchmaker.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;

import net.yangziwen.patchmaker.model.Blob;
import net.yangziwen.patchmaker.util.GitUtil;
import net.yangziwen.patchmaker.util.Util;
import spark.Spark;

public class GitController {
	
	private static final int DEFAULT_OFFSET = 0;
	private static final int DEFAULT_LIMIT = 40;
	
	private GitController() {}

	public static void init() {
		
		/** 获取底层gitbash的版本号 **/
		Spark.get("/git/version", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("version", GitUtil.getVersion());
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取分支列表 **/
		Spark.get("/git/branch/list", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("branchList", GitUtil.getBranchList(dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 切换分支 **/
		Spark.post("/git/branch/change", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("success", GitUtil.changeBranch(req.queryParams("branch"), dir));
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取提交点列表 **/
		Spark.get("/git/commit/list", (req, resp) -> {
			int offset = NumberUtils.toInt(req.queryParams("start"), DEFAULT_OFFSET);
			int limit = NumberUtils.toInt(req.queryParams("limit"), DEFAULT_LIMIT);
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("commits", GitUtil.getCommitList(offset, limit, dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取提交点总数 **/
		Spark.get("/git/commit/count", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("totalCount", GitUtil.getCommitTotalCount(dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		Spark.post("/git/blob/list", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			
			resp.type("application/json");
			
			String commitHashCode = req.queryParams("commitHashCode");
			Map<String, Blob> blobMap = new HashMap<>();
			List<String> commitHashCodeList = Arrays.asList(commitHashCode.split(","));
			for(int i = commitHashCodeList.size() - 1; i>=0; i--){
				List<Blob> blobList = GitUtil.getWhatChangedList(commitHashCodeList.get(i), dir);
				for(Blob blob: blobList) {
					blobMap.put(blob.getRelativeFilePath(), blob);
				}
			}
			Set<String> filePathSet = new TreeSet<>(blobMap.keySet());
			List<Blob> blobRecordList = new ArrayList<>(filePathSet.size());
			for(String filePath: filePathSet) {
				blobRecordList.add(blobMap.get(filePath));
			}
			resultMap.put("success", true);
			resultMap.put("blobRecordList", blobRecordList);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取两次提交之间发生变化了的文件列表 **/
		Spark.get("/git/blob/diffs", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			String sinceCommitHashCode = req.queryParams("sinceCommitHashCode");
			String untilCommitHashCode = req.queryParams("untilCommitHashCode");
			resultMap.put("blobRecordList", GitUtil.getRawDiffListBetween(sinceCommitHashCode, untilCommitHashCode, dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
	}
	
}
