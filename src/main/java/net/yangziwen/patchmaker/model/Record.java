package net.yangziwen.patchmaker.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Record {

	protected String hashCode;
	protected Type type;
	
	public String getHashCode() {
		return hashCode;
	}

	public void setHashCode(String hashCode) {
		this.hashCode = hashCode;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public enum Type {
		
		COMMIT, TREE, BLOB;
		
		public static Type parseType(String type){
			if(type == null  || (type = type.trim().toUpperCase()).length() == 0) {
				return null;
			}
			if("PARENT".equals(type)) {
				return COMMIT;
			}
			return Type.valueOf(type);
		}
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
}
