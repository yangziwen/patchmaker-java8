package net.yangziwen.patchmaker.patch.registry.common;

import java.io.File;
import java.util.Map;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;

public class JspRegistry extends AbstractRegistry {
	
	private static final String JSP_PREFIX = "/WEB-INF/jsp/";
	
	public JspRegistry() {
		super(JSP_FILTER);
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(JSP_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + JSP_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String jspFileName = srcPath.substring(packageEndPos);
		
		String pkg = "jsp." + packagePath.replace("/", ".");
		String patchFilePath = patchRootPath + pkg + "/" + jspFileName;
		
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}
	

}
