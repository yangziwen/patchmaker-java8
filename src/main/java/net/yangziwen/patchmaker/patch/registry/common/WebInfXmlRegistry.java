package net.yangziwen.patchmaker.patch.registry.common;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class WebInfXmlRegistry extends AbstractRegistry {
	
	public WebInfXmlRegistry() {
		super(WEB_INF_XML_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		String xmlFileName = FilenameUtils.getName(srcPath);
		String patchFilePath = patchRootPath + "/xml/" + xmlFileName;
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
