package net.yangziwen.patchmaker.model;

import net.yangziwen.patchmaker.model.Record.Type;

public interface PathRecord {
	
	String getHashCode();

	void setHashCode(String hashCode);

	Type getType();

	void setType(Type type);
	
	String getRelativeFilePath();
	
	void setRelativeFilePath(String relativeFilePath);
}
