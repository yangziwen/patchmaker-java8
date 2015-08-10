package net.yangziwen.patchmaker.patch;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
		Optional<File> error = fileMapping.keySet().stream()
			.filter(src -> !src.exists() || !src.isFile())
			.findFirst();
		if(error.isPresent()) {
			String errorFilePath = FilenameUtils.normalize(error.get().getAbsolutePath(), true);
			throw new FileNotFoundException(String.format("以下源文件不存在! \n[%s]", errorFilePath));
		}
		// create patch folder and copy files
		fileMapping.forEach(PatchMaker::copyFile);
	}
	
	public static void copyFile(File srcFile, File destFile) {
		try {
			FileUtils.copyFile(srcFile, destFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
