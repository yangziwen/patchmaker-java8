package net.yangziwen.patchmaker.patch.registry.mvn;

import java.io.File;
import java.util.Map;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class MvnCssRegistry extends AbstractRegistry {
	
	private static final String  CSS_PREFIX = "/src/main/webapp/css/";
	
	private String cssDirName = "css";

	protected MvnCssRegistry(boolean useOptimized) {
		super(CSS_FILTER);
		if(useOptimized) {
			cssDirName = "stylecss_optimize";
		}
	}
	
	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(CSS_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + CSS_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String cssFileName = srcPath.substring(packageEndPos);
		
		String pkg = cssDirName + "." + packagePath.replace("/", ".");
		String patchFilePath = patchRootPath + pkg + "/" + cssFileName;
		
		if(!"css".equals(cssDirName)) {
			srcPath = srcPath.replace("/css/", "/" + cssDirName + "/");
		}
		
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
