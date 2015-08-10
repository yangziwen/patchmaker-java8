package net.yangziwen.patchmaker.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.revwalk.RevCommit;

public class Commit extends Record {
	
	private String author;
	
	private Date authorDate;
	
	private String commiter;
	
	private Date commitDate;

	private String comment = "";
	
	private List<String> parents = new ArrayList<String>(2);
	
	public Commit() {}
	
	public Commit(RevCommit rc) {
		this.author = rc.getAuthorIdent().getName();
		this.authorDate = rc.getAuthorIdent().getWhen();
		this.commiter = rc.getCommitterIdent().getName();
		this.commitDate = new Date(rc.getCommitTime() * 1000L);
		this.hashCode = rc.getId().getName();
		this.comment = rc.getShortMessage();
		Arrays.asList(rc.getParents()).stream().forEach(p -> addParent(p.getId().name()));
	}

	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Date getAuthorDate() {
		return authorDate;
	}
	public void setAuthorDate(Date authorDate) {
		this.authorDate = authorDate;
	}
	public String getCommiter() {
		return commiter;
	}
	public void setCommiter(String commiter) {
		this.commiter = commiter;
	}
	public Date getCommitDate() {
		return commitDate;
	}
	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public Timestamp getCommitTimestamp() {
		if(commitDate == null) {
			return null;
		}
		return new Timestamp(commitDate.getTime());
	}
	public List<String> getParents() {
		return parents;
	}
	public void setParents(List<String> parents) {
		this.parents = parents;
	}
	public void setParentsStr(String parentsStr) {
		if(StringUtils.isBlank(parentsStr)) return;
		this.parents.clear();
		for(String parent: parentsStr.split("\\s")) {
			addParent(parent);
		}
	}
	public void addParent(String parentHashCode) {
		if(StringUtils.isEmpty(parentHashCode)) {
			return;
		}
		this.parents.add(parentHashCode);
	}
	
	public static String getPreviousCommitPointer(String hashCode, int interval, int parentIdx) {
		if(interval < 0 || parentIdx < 0) {
			throw new IllegalArgumentException("Both interval and parentIdx should not be less than 0!");
		}
		String pointer = hashCode;
		if(interval > 0) {
			pointer += "~" + interval;
		}
		if(parentIdx > 0) {
			pointer += "^" + parentIdx;
		}
		return pointer;
	}
	
	public static String getPreviousCommitPointer(String hashCode, int interval){
		return getPreviousCommitPointer(hashCode, interval, 0);
	}
	
	public static String getPreviousCommitPointer(String hashCode) {
		return getPreviousCommitPointer(hashCode, 1);
	}
	
}
