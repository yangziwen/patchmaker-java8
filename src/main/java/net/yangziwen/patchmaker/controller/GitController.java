package net.yangziwen.patchmaker.controller;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import spark.Spark;

public class GitController {

	public static void init() {
		
		Spark.get("/git/getVersion.do", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			resultMap.put("success", true);
			resultMap.put("version", "git version 3.7.1.201504261725-r.jgit");
			return resultMap;
		}, JSON::toJSONString);
		
	}
}
