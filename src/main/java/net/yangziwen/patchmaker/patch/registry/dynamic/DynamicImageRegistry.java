package net.yangziwen.patchmaker.patch.registry.dynamic;

import java.io.File;
import java.util.Map;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class DynamicImageRegistry extends AbstractRegistry {
	
	private static final String  IMG_PREFIX = "/WebRoot/images/";

	protected DynamicImageRegistry() {
		super(IMAGE_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(IMG_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + IMG_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String imgFileName = srcPath.substring(packageEndPos);
		
		String pkg = "images." + packagePath.replace("/", ".");
		String patchFilePath = patchRootPath + pkg + "/" + imgFileName;
		
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
