package net.yangziwen.patchmaker.patch.registry.mvn;

import java.io.File;

import net.yangziwen.patchmaker.patch.PatchMaker;
import net.yangziwen.patchmaker.patch.PatchMakerBuilder;
import net.yangziwen.patchmaker.patch.registry.RegistryChain;
import net.yangziwen.patchmaker.patch.registry.common.JarRegistry;
import net.yangziwen.patchmaker.patch.registry.common.JspRegistry;
import net.yangziwen.patchmaker.patch.registry.common.MessagePropertiesRegistry;
import net.yangziwen.patchmaker.patch.registry.common.WebInfXmlRegistry;


public class MvnPatchMakerBuilder implements PatchMakerBuilder {
	
	public PatchMaker buildPatchMaker(File patchRootDir, boolean useOptimized){
		RegistryChain registryChain = new RegistryChain(patchRootDir)
			.addRegistry(new MvnJavaRegistry())
			.addRegistry(new JspRegistry())
			.addRegistry(new MvnJsRegistry(useOptimized))
			.addRegistry(new MvnCssRegistry(useOptimized))
			.addRegistry(new MvnImageRegistry())
			.addRegistry(new WebInfXmlRegistry())
			.addRegistry(new MvnHbmXmlRegistry())
			.addRegistry(new JarRegistry())
			.addRegistry(new MessagePropertiesRegistry())
			.addRegistry(new MvnGroovyRegistry());
		return new PatchMaker(registryChain);
	}

}
