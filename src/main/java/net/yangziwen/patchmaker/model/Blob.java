package net.yangziwen.patchmaker.model;

import org.eclipse.jgit.diff.DiffEntry;

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
	
	public Blob(DiffEntry diffEntry) {
		this(
			diffEntry.getNewId().name(), 
			diffEntry.getChangeType().name().substring(0, 1), 
			generateRelativeFilePath(diffEntry), 
			null
		);
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
	
	private static String generateRelativeFilePath(DiffEntry diffEntry) {
		switch(diffEntry.getChangeType()) {
			case ADD: 
			case MODIFY:
			case COPY:
				return diffEntry.getNewPath();
			case DELETE:
				return diffEntry.getOldPath();
			case RENAME:
				return diffEntry.getOldPath() + " > " + diffEntry.getNewPath();
			default:
				return "";
		}
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
