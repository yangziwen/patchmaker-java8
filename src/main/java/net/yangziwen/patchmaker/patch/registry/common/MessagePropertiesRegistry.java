package net.yangziwen.patchmaker.patch.registry.common;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class MessagePropertiesRegistry extends AbstractRegistry  {

	public MessagePropertiesRegistry() {
		super(MESSAGE_PROPERTIES_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		String fileName = FilenameUtils.getName(srcPath);
		String patchFilePath = patchRootPath + "/classPathConfig/" + fileName;
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
