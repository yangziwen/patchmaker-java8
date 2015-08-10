package net.yangziwen.patchmaker.patch.registry.dynamic;

import java.io.File;

import net.yangziwen.patchmaker.patch.PatchMaker;
import net.yangziwen.patchmaker.patch.PatchMakerBuilder;
import net.yangziwen.patchmaker.patch.registry.RegistryChain;
import net.yangziwen.patchmaker.patch.registry.common.JarRegistry;
import net.yangziwen.patchmaker.patch.registry.common.JspRegistry;
import net.yangziwen.patchmaker.patch.registry.common.MessagePropertiesRegistry;
import net.yangziwen.patchmaker.patch.registry.common.WebInfXmlRegistry;


public class DynamicPatchMakerBuilder implements PatchMakerBuilder {

	@Override
	public PatchMaker buildPatchMaker(File patchRootDir, boolean useOptimized) {
		RegistryChain registryChain = new RegistryChain(patchRootDir)
			.addRegistry(new DynamicJavaRegistry())
			.addRegistry(new JspRegistry())
			.addRegistry(new DynamicJsRegistry(useOptimized))
			.addRegistry(new DynamicCssRegistry(useOptimized))
			.addRegistry(new DynamicImageRegistry())
			.addRegistry(new WebInfXmlRegistry())
			.addRegistry(new DynamicHbmXmlRegistry())
			.addRegistry(new JarRegistry())
			.addRegistry(new MessagePropertiesRegistry());
		return new PatchMaker(registryChain);
	}

}
