package net.yangziwen.patchmaker.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSON;

import net.yangziwen.patchmaker.util.CmdUtil;
import net.yangziwen.patchmaker.util.GitUtil;
import net.yangziwen.patchmaker.util.Util;
import net.yangziwen.patchmaker.util.Util.Counter;
import spark.Spark;

public class FileController {
	
	private static final String ROOT_PATH = "ROOT";
	
	private static final String PATCH_FOLDER_PATH = "c:/my_patches/";
	
	private FileController() {}
	
	@SuppressWarnings("serial")
	private static final Map<String, String> PROJECT_NAME_MAPPING = new HashMap<String, String>(){{
		put("ajaxablesky", "website");
		put("ableskystatics", "website");
	}};

	public static void init() {
		
		/** 打开指定目录的浏览窗口 **/
		Spark.get("/file/folder/open", (req, resp) -> {
			String folderPath = req.queryParams("folderPath");
			Map<String, Object> resultMap = new HashMap<>();
			File folder = null;
			if(StringUtils.isBlank(folderPath) || !(folder = new File(folderPath.trim())).isDirectory()) {
				resultMap.put("success", false);
				resultMap.put("message", "文件夹不存在或路径无效!");
				return resultMap;
			}
			String cmd = "explorer \"" + folder.getCanonicalPath() + "\"";
			CmdUtil.waitForProcess(Runtime.getRuntime().exec(cmd));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 用windows命令行打开指定目录 **/
		Spark.get("/file/cmdwin/open", (req, resp) -> {
			String folderPath = req.queryParams("folderPath");
			Map<String, Object> resultMap = new HashMap<>();
			File folder = null;
			if(StringUtils.isBlank(folderPath) || !(folder = new File(folderPath.trim())).isDirectory()) {
				resultMap.put("success", false);
				resultMap.put("message", "文件夹不存在或路径无效!");
				return resultMap;
			}
			String cmd = "cmd /c start";
			CmdUtil.waitForProcess(Runtime.getRuntime().exec(cmd, null, folder));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 用gitbash打开指定目录 **/
		Spark.get("/file/gitbash/open", (req, resp) -> {
			String folderPath = req.queryParams("folderPath");
			Map<String, Object> resultMap = new HashMap<>();
			File folder = null;
			if(StringUtils.isBlank(folderPath) || !(folder = new File(folderPath.trim())).isDirectory()) {
				resultMap.put("success", false);
				resultMap.put("message", "文件夹不存在或路径无效!");
				return resultMap;
			}
			String cmd = "cmd /c start sh --login -i";
			CmdUtil.waitForProcess(Runtime.getRuntime().exec(cmd, null, folder));
			resultMap.put("success", true);
			return resultMap;
		});
		
		/** 获取目录信息 **/
		Spark.get("/file/folder/info", (req, resp) -> {
			String folderPath = req.queryParams("folderPath");
			Map<String, Object> resultMap = new HashMap<>();
			
			File folder = new File(folderPath);
			if(!isRootPath(folderPath) && !folder.canRead()) {
				resultMap.put("isValid", false);
				return resultMap;
			}
			List<String> subFolderPathList = (isRootPath(folderPath) ? getRootList() : getSubFolder(folder))
				.stream()
				.map(file -> FilenameUtils.normalize(file.getAbsolutePath(), true))
				.collect(Collectors.toList());
			resultMap.put("folderPath", isRootPath(folderPath) ? ROOT_PATH : FilenameUtils.normalize(folderPath, true));
			resultMap.put("subFolderPaths", subFolderPathList);
			resultMap.put("isValid", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取子目录节点 **/
		Spark.post("/file/nodes", (req, resp) -> {
			String nodeId = req.queryParams("nodeId");
			String filePath = req.queryParams("filePath");
			return getFileNodeList(nodeId, filePath);
		}, JSON::toJSONString);
		
		/** 验证指定的工作空间是否为有效的git目录 **/
		Spark.get("/file/gitroot", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<String, Object>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("gitRootDir", dir.getCanonicalPath());
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
		/** 获取推荐的补丁路径 **/
		Spark.get("/file/patchpath/recommend", (req, resp) -> {
			Map<String, Object> resultMap = new HashMap<>();
			File dir = Util.checkWorkspaceDir(req.queryParams("workspaceDir"), resultMap);
			if(dir == null) return resultMap;
			resultMap.put("recommendedPatchPath", getRecommendedPatchPath(dir));
			resultMap.put("success", true);
			return resultMap;
		}, JSON::toJSONString);
		
	}
	
	private static List<Map<String, Object>> getFileNodeList(String parentNodeId, String parentFolderPath) {
		if(StringUtils.isBlank(parentNodeId) || StringUtils.isBlank(parentFolderPath)) {
			return Collections.emptyList();
		}
		File parentFolder = new File(parentFolderPath);
		if(!parentFolder.canRead()) {
			return Collections.emptyList();
		}
		Counter counter = Util.counter();
		return Arrays.asList(parentFolder.listFiles()).stream()
			.filter(File::isDirectory)
			.filter(File::canRead)
			.filter(file -> !file.getName().startsWith("."))
			.filter(file -> !file.isHidden())
			.map(file -> buildFileInfo(parentNodeId + "_" + counter.getAndIncr(), file))
			.collect(Collectors.toList());
	}
	
	private static Map<String, Object> buildFileInfo(String nodeId, File file) {
		Map<String, Object> fileInfo = new HashMap<>();
		fileInfo.put("id", nodeId);
		fileInfo.put("name", file.getName());
		fileInfo.put("isParent", file.isDirectory());
		fileInfo.put("filePath", file.getAbsolutePath());
		return fileInfo;
	}
	
	private static boolean isRootPath(String filePath) {
		return ROOT_PATH.equalsIgnoreCase(filePath);
	}
	
	private static List<File> getRootList() {
		return getRootList(false);
	}
	
	private static List<File> getRootList(boolean canReadOnly) {
		List<File> rootList = Arrays.asList(File.listRoots());
		if(!canReadOnly) {
			return rootList;
		}
		return rootList.stream().filter(File::canRead).collect(Collectors.toList());
	}
	
	private static List<File> getSubFolder(File parentFolder) {
		if(parentFolder == null || !parentFolder.canRead()) {
			return Collections.emptyList();
		}
		return Arrays.asList(parentFolder.listFiles()).stream()
			.filter(File::isDirectory)
			.filter(file -> !file.isHidden())
			.filter(file -> !file.getName().startsWith("."))
			.collect(Collectors.toList());
	}
	
	private static String getRecommendedPatchPath(File workspaceDir) { 
		String folderName = workspaceDir.getName();
		String username = GitUtil.getUsername(workspaceDir);
		String projectName = PROJECT_NAME_MAPPING.get(folderName);
		if(StringUtils.isBlank(projectName)) {
			projectName = folderName;
		}
		if(projectName.startsWith("as-")) {
			projectName = projectName.substring(3);
		}
		String dateStr = DateFormatUtils.format(new Date(), "yyyyMMdd");
		String patchName = dateStr + "-" + projectName + "-patch-" + username + "-todo";
		String patchPath = FilenameUtils.concat(PATCH_FOLDER_PATH, patchName);
		return patchPath;
	}
	
}
