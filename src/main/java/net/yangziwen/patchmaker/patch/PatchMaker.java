package net.yangziwen.patchmaker.patch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import net.yangziwen.patchmaker.patch.registry.RegistryChain;


public class PatchMaker {
	
	private RegistryChain registryChain;
	
	public PatchMaker(RegistryChain registryChain) {
		this.registryChain = registryChain;
	}
	
	public void makePatch(List<File> fileList) throws FileNotFoundException {
		Map<File, File> fileMapping = registryChain.mappingFiles(fileList);
		// validate source files
		for(File srcFile: fileMapping.keySet()) {
			if(srcFile == null || !srcFile.exists() || !srcFile.isFile()) {
				throw new FileNotFoundException("以下源文件不存在! \n[ " + FilenameUtils.normalize(srcFile.getAbsolutePath(), true)+ " ]");
			}
		}
		// create patch folder and copy files
		fileMapping.forEach((src, dest) -> {
			 try {
				FileUtils.copyFile(src, dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

}
