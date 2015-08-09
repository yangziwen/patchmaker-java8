package net.yangziwen.patchmaker.model;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Blob extends Record implements PathRecord {
	
	public Blob(String hashCode, String relativeFilePath){
		this.hashCode = hashCode;
		this.type = Type.BLOB;
		this.relativeFilePath = relativeFilePath;
	}
	
	public Blob(String hashCode, String oper, String relativeFilePath, String commitHashCode){
		this.hashCode = hashCode;
		this.type = Type.BLOB;
		this.oper = oper;
		this.relativeFilePath = relativeFilePath;
		this.commitHashCode = commitHashCode;
	}
	
	private String commitHashCode;
	
	private String relativeFilePath;
	
	private String oper;

	public String getCommitHashCode() {
		return commitHashCode;
	}

	public void setCommitHashCode(String commitHashCode) {
		this.commitHashCode = commitHashCode;
	}

	public String getRelativeFilePath() {
		return relativeFilePath;
	}

	public void setRelativeFilePath(String relativeFilePath) {
		this.relativeFilePath = relativeFilePath;
	}
	
	public String getOper() {
		return oper;
	}

	public void setOper(String oper) {
		this.oper = oper;
	}
	
	public static Blob parseContentFromWhatChanged(String str) {
		return parseContentFromWhatChanged(str, null);
	}
	
	public static Blob parseContentFromWhatChanged(String str, String commitHashCode){
		if(StringUtils.isBlank(str) || !str.startsWith(":")) {
			return null;
		}
		String[] contentArr = str.split("\\s");
		if(contentArr == null || contentArr.length < 6) {
			return null;
		}
		String hashCode = contentArr[3].trim();
		int pos = hashCode.indexOf("...");
		if(pos != -1) {
			hashCode = hashCode.substring(0, pos);
		}
		String oper = contentArr[4].trim();
		int filePathPos = str.indexOf(contentArr[5]);
		String relativeFilePath = str.substring(filePathPos);
		return new Blob(hashCode, oper, relativeFilePath, commitHashCode);
	}
	
	@Override
	public String toString(){
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof Blob)) {
			return false;
		}
		Blob blob = (Blob) obj;
		if(blob == null 
				|| !this.hashCode.equals(blob.getHashCode()) 
				|| !this.relativeFilePath.equals(blob.getRelativeFilePath())) {
			return false;
		}
		return true;
 	}

}
