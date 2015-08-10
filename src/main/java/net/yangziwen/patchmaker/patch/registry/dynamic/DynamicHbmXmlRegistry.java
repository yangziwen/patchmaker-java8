package net.yangziwen.patchmaker.patch.registry.dynamic;

import java.io.File;
import java.util.Map;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class DynamicHbmXmlRegistry extends AbstractRegistry {
	
	private static final String PACKAGE_PREFIX = "/src/";
	
	public DynamicHbmXmlRegistry(){
		super(HBM_XML_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(PACKAGE_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + PACKAGE_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String hbmXmlFileName = srcPath.substring(packageEndPos);
		
		String pkg = packagePath.replace("/", ".");
		String patchFilePath = patchRootPath + pkg + "/" + hbmXmlFileName;
		
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
