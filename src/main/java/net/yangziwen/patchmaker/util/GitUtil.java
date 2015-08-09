package net.yangziwen.patchmaker.util;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import net.yangziwen.patchmaker.model.Branch;
import net.yangziwen.patchmaker.model.Commit;

public class GitUtil {

	private GitUtil() {}
	
	public static String getVersion() {
		return "git version 3.7.1.201504261725-r.jgit";
	}
	
	public static List<Branch> getBranchList(File dir) {
		try(Git git = new Git(dir)) {
			ObjectId headId = git.getRepository().resolve(Constants.HEAD).toObjectId();
			return git.branchList().call().stream()
					.map(ref -> new Branch(ref.getName(), ref.getObjectId().equals(headId)))
					.collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	public static List<Commit> getCommitList(int offset, int limit, File dir) {
		try(Git git = new Git(dir)) {
			return Util.toList(git.log().setSkip(offset).setMaxCount(limit).call().iterator())
					.stream()
					.map(rc -> {
						Commit commit = new Commit();
						commit.setAuthor(rc.getAuthorIdent().getName());
						commit.setAuthorDate(rc.getAuthorIdent().getWhen());
						commit.setCommiter(rc.getCommitterIdent().getName());
						commit.setCommitDate(new Date(rc.getCommitTime() * 1000L ));
						commit.setHashCode(rc.getId().getName());
						commit.setComment(rc.getShortMessage());
						Arrays.asList(rc.getParents()).stream().forEach(p -> commit.addParent(p.getId().name()));
						return commit;
					}).collect(Collectors.toList());
		} catch (Exception e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	public static int getCommitTotalCount(File dir) {
		try(Git git = new Git(dir)) {
			Iterator<?> iter = git.log().call().iterator();
			int cnt = 0;
			while(iter.hasNext()) {
				iter.next();
				cnt ++;
			}
			return cnt;
		} catch(Exception e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	private static Repository buildRepository(File dir) {
		try {
			return new FileRepositoryBuilder()
				.setWorkTree(dir)
				.readEnvironment()
				.setMustExist(true)
				.build();
		} catch (IOException e) {
			throw new RuntimeException(String.format("Repository[%s] does not exist!", dir));
		}
	}

	public static String getUsername(File dir) {
		try(Git git = new Git(dir)) {
			return git.getRepository().getConfig().getString("user", null, "name");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static AbstractTreeIterator prepareTreeParser(Repository repo, String hashCode) {
		try {
			RevWalk walk = new RevWalk(repo);
			RevCommit commit = walk.parseCommit(repo.resolve(hashCode));
			RevTree tree = walk.parseTree(commit.getTree().getId());
	
			ObjectReader oldReader = repo.newObjectReader();
			CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
			oldTreeParser.reset(oldReader, tree.getId());
			return oldTreeParser;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
	
	public static class Git extends org.eclipse.jgit.api.Git implements Closeable {
		
		public Git(Repository repo) {
			super(repo);
		}
		
		public Git(File dir) {
			super(buildRepository(dir));
		}
	}
}