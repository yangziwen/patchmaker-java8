package net.yangziwen.patchmaker.patch.registry.common;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;

public class JarRegistry extends AbstractRegistry {

	public JarRegistry() {
		super(JAR_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		String patchFilePath = patchRootPath + "/lib/" +  FilenameUtils.getName(srcPath);
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
