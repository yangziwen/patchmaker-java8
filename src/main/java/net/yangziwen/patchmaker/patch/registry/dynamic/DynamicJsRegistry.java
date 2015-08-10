package net.yangziwen.patchmaker.patch.registry.dynamic;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.lang3.StringUtils;

import net.yangziwen.patchmaker.patch.registry.AbstractRegistry;


public class DynamicJsRegistry extends AbstractRegistry {
	
	private static final String  JS_PREFIX = "/WebRoot/js/";
	
	private String jsDirName = "js";

	protected DynamicJsRegistry(boolean useOptimized) {
		super(new OrFileFilter((IOFileFilter)JS_FILTER, (IOFileFilter)JSON_FILTER));
		if(useOptimized) {
			jsDirName = "js_optimize";
		}
	}

	@Override
	protected boolean fillFileMapping(String srcPath, String patchRootPath, Map<File, File> fileMapping) {
		int prefixPos = srcPath.indexOf(JS_PREFIX);
		if(prefixPos == -1) {
			return false;
		}
		int packageStartPos = prefixPos + JS_PREFIX.length();
		int packageEndPos = srcPath.lastIndexOf("/") + 1;
		
		String packagePath = srcPath.substring(packageStartPos, packageEndPos);
		String jsFileName = srcPath.substring(packageEndPos);
		
		String pkg = jsDirName;
		if(StringUtils.isNotBlank(packagePath)) {
			pkg += "." + packagePath.replace("/", ".");
		}
		String patchFilePath = patchRootPath + pkg + "/" + jsFileName;
		
		if(!"js".equals(jsDirName)) {
			srcPath = srcPath.replace("/js/", "/" + jsDirName + "/");
		}
		
		fileMapping.put(new File(srcPath), new File(patchFilePath));
		return true;
	}

}
