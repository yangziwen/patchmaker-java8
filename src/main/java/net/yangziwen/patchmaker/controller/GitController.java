package net.yangziwen.patchmaker.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;

import com.alibaba.fastjson.JSON;

import net.yangziwen.patchmaker.util.GitUtil;
import net.yangziwen.patchmaker.util.Util;
import spark.Spark;

public class GitController {
	
	private static final int DEFAULT_OFFSET = 0;
	private static final int DEFAULT_LIMIT = 40;

	public static void init() {
		
		/** 获取底层gitbash的版本号 **/
		Spark.get("/git/getVersion.do", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("version", GitUtil.getVersion());
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取分支列表 **/
		Spark.get("/git/listBranch.do", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) {
				return resultMap;
			}
			resultMap.put("branchList", GitUtil.getBranchList(dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取提交点列表 **/
		Spark.get("/git/listCommitRecord.do", (req, resp) -> {
			int offset = NumberUtils.toInt(req.queryParams("start"), DEFAULT_OFFSET);
			int limit = NumberUtils.toInt(req.queryParams("limit"), DEFAULT_LIMIT);
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			resultMap.put("commits", GitUtil.getCommitList(offset, limit, dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取提交点总数 **/
		Spark.get("/git/getCommitTotalCount.do", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			resultMap.put("totalCount", GitUtil.getCommitTotalCount(dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		Spark.get("/listBlobRecord.do", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) {
				return resultMap;
			}
			return resultMap;
		}, JSON::toJSONString);
		
	}
	
}
