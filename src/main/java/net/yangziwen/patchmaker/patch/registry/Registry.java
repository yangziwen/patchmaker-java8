package net.yangziwen.patchmaker.patch.registry;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;

public interface Registry {
	
	public static final FileFilter JAVA_FILTER = new SuffixFileFilter(".java");
	public static final FileFilter GROOVY_FILTER = new SuffixFileFilter(".groovy");
	public static final FileFilter CLASS_FILTER = new SuffixFileFilter(".class");
	public static final FileFilter JSP_FILTER = new SuffixFileFilter(".jsp");
	public static final FileFilter JS_FILTER = new SuffixFileFilter(".js");
	public static final FileFilter JSON_FILTER = new SuffixFileFilter(".json");
	public static final FileFilter CSS_FILTER = new SuffixFileFilter(".css");
	public static final FileFilter XML_FILTER = new SuffixFileFilter(".xml");
	public static final FileFilter JAR_FILTER = new SuffixFileFilter(".jar");
	public static final FileFilter CONF_FILTER = new SuffixFileFilter(".conf");
	public static final FileFilter PROPERTIES_FILTER = new SuffixFileFilter(".properties");
	public static final FileFilter MESSAGE_PROPERTIES_FILTER = new AndFileFilter(new PrefixFileFilter("messages"), (IOFileFilter)PROPERTIES_FILTER);
	public static final FileFilter IMAGE_FILTER = new SuffixFileFilter(new String[]{".png", ".gif", ".ico", ".jpg", ".jpeg", ".psd"});
	public static final FileFilter HBM_XML_FILTER = new SuffixFileFilter(".hbm.xml");
	public static final FileFilter WEB_INF_XML_FILTER = file -> file != null && Registry.XML_FILTER.accept(file) && file.getAbsolutePath().contains("WEB-INF");

	public int registerDestinations(List<File> fileList, String patchRootPath, Map<File, File> fileMapping);
	
}
