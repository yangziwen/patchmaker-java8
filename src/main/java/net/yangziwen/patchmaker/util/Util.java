package net.yangziwen.patchmaker.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

public class Util {

	private Util() {}
	
	public static <T> List<T> toList(Iterator<T> iterator) {
		return toList(iterator, 10);
	}
	
	public static <T> List<T> toList(Iterator<T> iterator, int estimatedSize) {
        if (iterator == null) {
            throw new NullPointerException("Iterator must not be null");
        }
        if (estimatedSize < 1) {
            throw new IllegalArgumentException("Estimated size must be greater than 0");
        }
        List<T> list = new ArrayList<>(estimatedSize);
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
	
	public static File findGitRootDir(File dir) {
		if(dir == null || !dir.exists()) {
			return null;
		}
		for(File parent = dir; parent != null; parent = parent.getParentFile()) {
			File[] files = parent.listFiles((d, name) -> ".git".equals(name));
			if(ArrayUtils.isNotEmpty(files)) {
				return parent;
			}
		}
		return null;
	}
	
	public static File checkWorkspaceDir(String workspaceDir, Map<String, Object> resultMap) throws IOException {
		File dir = null;
		if(StringUtils.isBlank(workspaceDir) || !(dir = new File(workspaceDir)).exists()) {
			resultMap.put("success", false);
			resultMap.put("message", "工作目录为空或不存在!");
			return null;
		}
		File gitRootDir = Util.findGitRootDir(dir);
		if(gitRootDir == null) {
			resultMap.put("success", false);
			resultMap.put("message", "工作目录不是有效的git目录!");
			return null;
		}
		return gitRootDir;
	}
	
	public static boolean isMavenProject(File rootDir){
		return getPomFromProject(rootDir) != null;
	}
	
	public static File getPomFromProject(File rootDir) {
		FileFilter filter = new NameFileFilter("pom.xml", IOCase.INSENSITIVE);
		List<File> list = findFile(rootDir, filter, 2);
		return list.size() > 0? list.get(0): null;
	}
	
	public static List<File> findFile(File dir, FileFilter fileFilter, int depth) {
		if(depth <= 0 || dir == null || !dir.isDirectory() || fileFilter == null) {
			return Collections.emptyList();
		}
		List<File> fileList = new LinkedList<>();
		for(File file: dir.listFiles()) {
			if(fileFilter.accept(file)) {
				fileList.add(file);
			}
			if(file.isDirectory()) {
				fileList.addAll(findFile(file, fileFilter, depth - 1));
			}
		}
		return fileList;
	}
	
	public static Counter counter() {
		return counter(0);
	}
	
	public static Counter counter(int n) {
		return new Counter(n);
	}
	
	public static class Counter {
		int cnt;
		Counter(int n) {
			this.cnt = n;
		}
		
		public int get() {
			return cnt;
		}
		
		public int getAndIncr() {
			return getAndIncr(1);
		}
		
		public int getAndIncr(int n) {
			int old = cnt;
			cnt += n;
			return old;
		}
		
		public int incrAndGet() {
			return incrAndGet(1);
		}
		
		public int incrAndGet(int n) {
			return cnt += n;
		}
	}
	
}
