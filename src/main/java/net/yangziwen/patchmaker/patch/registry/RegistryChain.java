package net.yangziwen.patchmaker.patch.registry;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.PrefixFileFilter;

public class RegistryChain {
	
	private File patchRootDir;
	
	private Map<String, Registry> chainMap = new LinkedHashMap<String, Registry>();
	
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
		// 这里本应该写成责任链的，现在有点不伦不类，但也没力气改了
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
	private void collectAndFillInnerClassFileMapping(Map<File, File> fileMapping) {
		Map<File, List<File>> classFileParentFolderMap = distributeClassFilesToParentFolderMap(fileMapping);
		for(Entry<File, List<File>> entry: classFileParentFolderMap.entrySet()) {
			File parentFolder = entry.getKey();
			List<File> classFileList = entry.getValue();
			if(classFileList == null || classFileList.size() == 0) {
				continue;
			}
			fillInnerClassFileMapping(fileMapping, collectInnerClassFilesInTheSameFolder(parentFolder, classFileList));
		}
	}
	
	/**
	 * 向补丁路径填充内部类
	 */
	private void fillInnerClassFileMapping(Map<File, File> fileMapping, List<File> innerClassFileList) {
		if(innerClassFileList == null || innerClassFileList.size() == 0) {
			return;
		}
		// 下面这段仅仅是为了找出补丁文件夹的路径
		File innerClassFile = innerClassFileList.get(0);
		String innerClassFilename = FilenameUtils.normalize(innerClassFile.getAbsolutePath());
		int $pos = innerClassFilename.indexOf("$");
		if($pos == -1) {
			return;
		}
		File patchFile = fileMapping.get(new File(innerClassFilename.substring(0, $pos) + ".class"));
		if(patchFile == null) {
			return;
		}
		String patchFileParentFolderPath = patchFile.getParent();
		for(File inner: innerClassFileList) {
			fileMapping.put(inner, new File(patchFileParentFolderPath + "/" + inner.getName()));
		}
	}
	
	/**
	 * 按父目录对class文件进行分组
	 */
	private Map<File, List<File>> distributeClassFilesToParentFolderMap(Map<File, File> fileMapping) {
		Map<File, List<File>> classFileParentFolderMap = new HashMap<>();
		for(File file: fileMapping.keySet()) {
			if(!Registry.CLASS_FILTER.accept(file)) {
				continue;
			}
			File classParentFolder = file.getParentFile();
			List<File> fileList = classFileParentFolderMap.get(classParentFolder);
			if(fileList == null) {
				fileList = new ArrayList<>();
				classFileParentFolderMap.put(classParentFolder, fileList);
			}
			fileList.add(file);
		}
		return classFileParentFolderMap;
	}
	
	private List<File> collectInnerClassFilesInTheSameFolder(File parentFolder, List<File> classFileList) {
		List<String> prefixList = new ArrayList<>(classFileList.size());
		for(File classFile: classFileList) {
			prefixList.add(classFile.getName().replace(".class", "$"));
		}
		FilenameFilter innerClassPrefixFilter = new PrefixFileFilter(prefixList);
		return Arrays.asList(parentFolder.listFiles(innerClassPrefixFilter));
	}
}
