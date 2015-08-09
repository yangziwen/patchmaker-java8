package net.yangziwen.patchmaker.controller;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import net.yangziwen.patchmaker.util.Util;
import net.yangziwen.patchmaker.util.Util.Counter;
import spark.Spark;

public class FileController {
	
	private static final String ROOT_PATH = "ROOT";

	public static void init() {
		
		Spark.get("/file/getFolderInfo.do", (req, resp) -> {
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
					
			resultMap.put("isValid", true);
			resultMap.put("folderPath", isRootPath(folderPath) ? ROOT_PATH : FilenameUtils.normalize(folderPath, true));
			resultMap.put("subFolderPaths", subFolderPathList);
			return resultMap;
			
		}, JSON::toJSONString);
		
		Spark.post("/file/getFileNodes.do", (req, resp) -> {
			String nodeId = req.queryParams("nodeId");
			String filePath = req.queryParams("filePath");
			return getFileNodeList(nodeId, filePath);
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
				.map(file -> {
					Map<String, Object> fileInfo = Maps.newHashMap();
					fileInfo.put("id", parentNodeId + "_" + counter.getAndIncr());
					fileInfo.put("name", file.getName());
					fileInfo.put("isParent", file.isDirectory());
					fileInfo.put("filePath", file.getAbsolutePath());
					return fileInfo;
				}).collect(Collectors.toList());
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
}
