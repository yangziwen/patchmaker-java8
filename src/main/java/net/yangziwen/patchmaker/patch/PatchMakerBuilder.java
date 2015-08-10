package net.yangziwen.patchmaker.patch;

import java.io.File;

public interface PatchMakerBuilder {

	public PatchMaker buildPatchMaker(File patchRootDir, boolean userOptimized);
	
}
