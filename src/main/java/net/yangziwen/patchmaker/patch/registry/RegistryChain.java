package net.yangziwen.patchmaker.patch.registry;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

import spark.utils.CollectionUtils;

public class RegistryChain {
	
	private File patchRootDir;
	
	private Map<String, Registry> chainMap = new LinkedHashMap<>();
	
	public RegistryChain(File patchRootDir){
		if(patchRootDir == null) {
			throw new IllegalArgumentException("Neither git root dir nor patch root dir should be null!");
		}
		this.patchRootDir = patchRootDir;
	}
	
	public File getPatchRootDir() {
		return patchRootDir;
	}

	public RegistryChain addRegistry(Registry registry){
		if(registry == null) {
			throw new IllegalArgumentException("Register should not be null!");
		}
		chainMap.put(registry.getClass().getName(), registry);
		return this;
	}
	
	public Registry getRegistry(String key) {
		return chainMap.get(key);
	}
	
	public Registry getRegistry(Class<? extends Registry> clazz) {
		return chainMap.get(clazz.getName());
	}
	
	public Registry removeRegistry(Registry registry) {
		if(registry == null) {
			return null;
		}
		return chainMap.remove(registry.getClass().getName());
	}
	
	public Registry removeRegistry(Class<Registry> clazz) {
		if(clazz == null) {
			return null;
		}
		return chainMap.remove(clazz.getName());
	}
	
	public Map<File, File> mappingFiles(List<File> fileList){
		int cnt = fileList.size();
		String patchRootPath = FilenameUtils.normalize(patchRootDir.getAbsolutePath(), true);
		if(!patchRootPath.endsWith("/")) {
			patchRootPath += "/";
		}
		Map<File, File> fileMapping = new HashMap<>();
		for(Registry registry: chainMap.values()) {
			cnt -= registry.registerDestinations(fileList, patchRootPath, fileMapping);
			if(cnt <= 0) {
				break;
			}
		}
		afterMapping(fileMapping);
		return fileMapping;
	}
	
	private void afterMapping(Map<File, File> fileMapping) {
		// 收集并填充内部类
		collectAndFillInnerClassFileMapping(fileMapping);
	}
	
	/**
	 * 寻找并填充内部类
	 */
	private static void collectAndFillInnerClassFileMapping(Map<File, File> fileMapping) {
		distributeClassFilesToParentFolderMap(fileMapping)
			.entrySet().stream()
			.filter(entry -> !CollectionUtils.isEmpty(entry.getValue()))
			.map(entry -> collectInnerClassFilesInTheSameFolder(entry.getKey(), entry.getValue()))
			.forEach(classFileList -> fillInnerClassFileMapping(fileMapping, classFileList));
	}
	
	/**
	 * 向补丁路径填充内部类
	 */
	private static void fillInnerClassFileMapping(Map<File, File> fileMapping, List<File> innerClassFileList) {
		if(CollectionUtils.isEmpty(innerClassFileList)) {
			return;
		}
		// 下面这段仅仅是为了找出补丁文件夹的路径
		String innerClassFilename = FilenameUtils.normalize(innerClassFileList.get(0).getAbsolutePath());
		File patchFile = fileMapping.get(new File(innerClassFilename.replaceFirst("\\$.+\\.class$", ".class")));
		File parentFolder = patchFile.getParentFile();
		innerClassFileList.forEach(inner -> fileMapping.put(inner, new File(parentFolder, inner.getName())));
	}
	
	private static List<File> getOrCreateList(File key, Map<File, List<File>> map) {
		List<File> list = map.get(key);
		if(list == null) {
			map.put(key, list = new ArrayList<>());
		}
		return list;
	}
	
	/**
	 * 按父目录对class文件进行分组
	 */
	private static Map<File, List<File>> distributeClassFilesToParentFolderMap(Map<File, File> fileMapping) {
		Map<File, List<File>> classFileParentFolderMap = new HashMap<>();
		fileMapping.keySet().stream()
			.filter(Registry.CLASS_FILTER::accept)
			.forEach(file -> getOrCreateList(file.getParentFile(), classFileParentFolderMap).add(file));
		return classFileParentFolderMap;
	}
	
	private static List<File> collectInnerClassFilesInTheSameFolder(File parentFolder, List<File> classFileList) {
		List<String> prefixList = classFileList.stream()
			.map(classFile -> classFile.getName().replace(".class", "$"))
			.collect(Collectors.toList());
		FilenameFilter innerClassPrefixFilter = new PrefixFileFilter(prefixList);
		return Arrays.asList(parentFolder.listFiles(innerClassPrefixFilter));
	}
}
