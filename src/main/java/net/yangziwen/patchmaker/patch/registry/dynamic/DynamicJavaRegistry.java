package net.yangziwen.patchmaker.patch.registry.dynamic;

import java.io.File;
import java.util.Map;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class DynamicJavaRegistry extends AbstractRegistry {
	
	private static final String PACKAGE_PREFIX = "/src/";

	protected DynamicJavaRegistry() {
		super(JAVA_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(PACKAGE_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + PACKAGE_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		
		String projectRootPath = srcPath.substring(0, prefixPos) + "/";
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String classFileName = srcPath.substring(packageEndPos).replace(".java", ".class");
		
		String classRootPath = projectRootPath + "WebRoot/WEB-INF/classes/";
		String classFilePath = classRootPath + packagePath + "/" + classFileName;
		String pkg = packagePath.replace("/", ".");
		String patchFilePath = patchRootPath + pkg + "/" + classFileName;
		
		fileMapping.put(new File(classFilePath), new File(patchFilePath));
		return true;
	}

}
