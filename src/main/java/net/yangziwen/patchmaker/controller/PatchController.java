package net.yangziwen.patchmaker.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;

import com.alibaba.fastjson.JSON;

import net.yangziwen.patchmaker.patch.PatchMaker;
import net.yangziwen.patchmaker.patch.registry.dynamic.DynamicPatchMakerBuilder;
import net.yangziwen.patchmaker.patch.registry.mvn.MvnPatchMakerBuilder;
import net.yangziwen.patchmaker.util.Util;
import net.yangziwen.patchmaker.util.ZipUtil;
import spark.Spark;

public class PatchController {
	
	private static final int PROJECT_TYPE_DYNAMIC = 1;
	
	private static final int PROJECT_TYPE_MAVEN = 2;
	
	private PatchController() {}
	
	public static void init() {
		
		Spark.post("/patch/patch/create", (req, resp) -> {
			resp.type("application/json");
			String gitRootDir = req.queryParams("gitRootDir");
			String patchDir = req.queryParams("patchDir").trim();
			String filePaths = req.queryParams("filePaths");
			boolean useOptimized = BooleanUtils.toBoolean(req.queryParams("useOptimized"));
			Map<String, Object> resultMap = new HashMap<>();
			// 检查下目标文件夹
			patchDir = patchDir.trim();
			File targetDir = new File(patchDir);
			if(!targetDir.exists()) {
				targetDir.mkdirs();
			}
			if(!targetDir.isDirectory()) {
				resultMap.put("success", false);
				resultMap.put("message", "目标文件夹已存在且不是文件目录!");
				return resultMap;
			}
			// 检查下补丁文件
			String[]  filePathArr = filePaths.split(",");
			if(filePathArr == null || filePathArr.length == 0) {
				resultMap.put("success", false);
				resultMap.put("message", "补丁文件列表为空!");
				return resultMap;
			}
			List<File> fileList = new ArrayList<> (filePathArr.length);
			File file = null;
			for(String filePath: filePathArr) {
				file = new File(gitRootDir + "/" + filePath);
				if(!file.exists()) {
					resultMap.put("success", false);
					resultMap.put("message", "补丁文件[" + file.getAbsolutePath() + "]不存在!");
					return resultMap;
				}
				fileList.add(file);
			}
			
			File gitRootDirectory = new File(gitRootDir);
			
			PatchMaker maker = createPatchMaker(recognizeProjectType(gitRootDirectory), targetDir, useOptimized);
			
			try {
				// 生成补丁文件夹
				maker.makePatch(fileList);
				// 将补丁文件夹压缩成zip
				ZipUtil.zip(new String[]{patchDir}, patchDir + ".zip");
				resultMap.put("success", true);
				return resultMap;
			} catch (Exception e) {
				e.printStackTrace();
				resultMap.put("success", false);
				resultMap.put("message", e.getMessage());
				return resultMap;
			}
		}, JSON::toJSONString);
	}
	
	private static PatchMaker createPatchMaker(int projectType, File patchRootDir, boolean useOptimized) {
		switch(projectType) {
			case PROJECT_TYPE_MAVEN:
				return new MvnPatchMakerBuilder().buildPatchMaker(patchRootDir, useOptimized);
			case PROJECT_TYPE_DYNAMIC:
				return new DynamicPatchMakerBuilder().buildPatchMaker(patchRootDir, useOptimized);
		}
		return null;
	}
	
	/**
	 * 判断当前工程类型
	 * @param rootDir
	 * @return
	 */
	private static int recognizeProjectType (File rootDir) {
		if(Util.isMavenProject(rootDir)) {
			return PROJECT_TYPE_MAVEN;
		} else {
			return PROJECT_TYPE_DYNAMIC;
		}
	}

}
