package net.yangziwen.patchmaker.patch.registry;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

public abstract class AbstractRegistry implements Registry {

	protected final FileFilter fileFilter;
	
	protected AbstractRegistry(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}
	
	@Override
	public int registerDestinations(List<File> fileList, String patchRootPath, Map<File, File> fileMapping) {
		int cnt = 0;
		for(File src: fileList) {
			if(!fileFilter.accept(src)) {
				continue;
			}
			if(fillFileMapping(FilenameUtils.normalize(src.getAbsolutePath(), true), patchRootPath, fileMapping)){
				cnt ++;
			}
		}
		return cnt;
	}
	
	protected abstract boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping);
	
}
