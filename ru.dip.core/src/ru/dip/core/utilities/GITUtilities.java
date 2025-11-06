/******************************************************************************* * 
 * Copyright (c) 2025 Denis Melnik.
 * Copyright (c) 2025 Ruslan Sabirov.
 * Copyright (c) 2025 Andrei Motorin.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package ru.dip.core.utilities;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.egit.core.GitCorePreferences;
import org.eclipse.egit.core.GitProvider;
import org.eclipse.egit.core.project.GitProjectData;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.SubmoduleUpdateCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.eclipse.team.core.RepositoryProvider;

import ru.dip.core.DipCorePlugin;
import ru.dip.core.model.DipProject;
import ru.dip.core.model.interfaces.IDipDocumentElement;
import ru.dip.core.utilities.git.CommitCache;
import ru.dip.core.utilities.git.CommitInfo;
import ru.dip.core.utilities.git.GitRevisionModel;
import ru.dip.core.utilities.git.GitTagModel;

public class GITUtilities {

	/**
	 * Статус: - текущий коммит - был ли изменен
	 */
	public static class GitFileStatus {

		private boolean fChanged;
		private String fHash;

		public GitFileStatus(String hash, boolean changed) {
			fHash = hash;
			fChanged = changed;
		}

		public String getHash() {
			return fHash;
		}

		public boolean isChanged() {
			return fChanged;
		}

		@Override
		public String toString() {
			return "GIT STATUS: " + fHash + "  " + fChanged;
		}
	}
	
	
	/**
	 * Возвращает все ветви
	 * 
	 * ListMode.ALL - все ветви
	 * ListMode.REMOTE - удаленные
	 * без ListMode - локальные 	 
	 */
	public static String[] getAllBranches(String repoPath) throws IOException, NoHeadException, GitAPIException {
		return getAllBranches(repoPath, ListMode.ALL);
	}
	
	public static String[] getAllBranches(String repoPath, ListMode mode) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(new File(repoPath));
		List<Ref> refs = mode != null? git.branchList().setListMode(mode).call() 
				: git.branchList().call();
		return refs.stream().map(ref -> {
			String result = ref.getName();
			if (result.startsWith("refs/heads/")) {
				result = result.substring(11);
			} else if (result.startsWith("refs/remotes/")) {
				result = result.substring(13);				
			}
			return result;
		}).toArray(String[]::new);
	}
	
	
	public static Map<Ref, List<RevCommit>> getCommitsByBranches(String repoPath) throws IOException, GitAPIException {
		Map<Ref, List<RevCommit>> result = new HashMap<Ref, List<RevCommit>>();
		
		Git git = Git.open(new File(repoPath));
		Repository repo = git.getRepository();
		
		List<Ref>  refs = git.branchList().call();
		for (Ref ref: refs) {
			Iterable<RevCommit> commits = git.log().add(repo.resolve(ref.getName())).call();
			
			List<RevCommit> branchCommits = new ArrayList<>();						
			for (RevCommit commit : commits) {
				branchCommits.add(commit);							
			}	
			result.put(ref, branchCommits);			
		}						
		return result;
	}
	

	/**
	 * Возвращает все теги (метки) для указанного репозитория
	 */
	public static HashMap<String, String> getTags(String repoPath) {
		HashMap<String, String> result = new HashMap<>();
		try {
			Git git = Git.open(new File(repoPath));
			RevWalk walk = new RevWalk(git.getRepository());
			Map<String, Ref> tags = git.getRepository().getTags();
			for (Entry<String, Ref> entry : tags.entrySet()) {
				Ref ref = entry.getValue();
				RevObject revObj = walk.parseAny(ref.getObjectId());
				int type = revObj.getType();
				if (type == 4) {
					RevTag revTag = walk.parseTag(ref.getObjectId());
					String commitName = revTag.getObject().getName();
					result.put(commitName, entry.getKey());
				} else {
					result.put(revObj.getName(), entry.getKey());
				}
			}
			walk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
		
	
	public static List<GitTagModel> getTagModelss(String repoPath, String from) throws NoHeadException, GitAPIException {
		List<GitTagModel> result = new ArrayList<>();
		try {
			Git git = Git.open(new File(repoPath));
			RevWalk walk = new RevWalk(git.getRepository());
			Map<String, Ref> tags = git.getRepository().getTags();
			
			List<GitRevisionModel> revisionModels = null;
			if (from != null) {
				revisionModels = getAllRevisions(repoPath, from);
			}
			
			for (Entry<String, Ref> entry : tags.entrySet()) {
				Ref ref = entry.getValue();
				RevObject revObj = walk.parseAny(ref.getObjectId());
				int type = revObj.getType();
				if (type == 4) {
					RevTag revTag = walk.parseTag(ref.getObjectId());															
					String commitName = revTag.getObject().getName();
					
					// если нужны теги с определенного коммита
					if (from != null && !contains(revisionModels, revTag.getObject())) {
						continue;
					}
					
					GitTagModel tagModel = new GitTagModel(commitName, entry.getKey(), revTag);
					result.add(tagModel);				
				} 
			}
			walk.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private static boolean contains(List<GitRevisionModel> revisions, RevObject revObject) {
		for (GitRevisionModel revision: revisions) {
			if (revision.getRevCommit().getId().equals(revObject.getId())){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Возвращает хеш, для указанного ресурса
	 */
	public static String getFullId(IResource resource) {
		IProject project = resource.getProject();
		RepositoryProvider provider = RepositoryProvider.getProvider(project, GitProvider.ID);
		if (provider != null && provider instanceof GitProvider) {
			GitProvider gitProvider = (GitProvider) provider;
			GitProjectData data = gitProvider.getData();
			RepositoryMapping mapping = data.getRepositoryMapping(resource);
			Repository repo = mapping.getRepository();
			Path repoPath = Paths.get(repo.getDirectory().getAbsolutePath()).getParent();
			Path resPath = Paths.get(resource.getLocationURI());
			Path path = repoPath.relativize(resPath);
			try {
				Git git = new Git(repo);
				return getCurrentHash(git, path.toString());
			} catch (RevisionSyntaxException | IOException | GitAPIException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	// =======================================
	// repo
		
	/**
	 * Дефолтная папка с репозиториями
	 */
	@SuppressWarnings("restriction")
	public static String defaultRepositoryDir() {
		return org.eclipse.egit.core.Activator.getDefault().getPluginPreferences().getString(GitCorePreferences.core_defaultRepositoryDir);		
	}
	
	/**
	 * Поиск репозитория
	 */
	public static Repository findRepo(IResource resource) {
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		//repositoryBuilder.addCeilingDirectory(new File("/home"));  могут возникнуть проблемы на винде
		repositoryBuilder.findGitDir(resource.getLocation().toFile());
		try {
			return repositoryBuilder.build();
		} catch (Exception e) {
			//e.printStackTrace();
			DipCorePlugin.logError("Git Repository not found " + resource);
			return null;
		}
	}
	
	/**
	 * Текущий хэш репозитория
	 */
	public static String getCurrentHash(String repoPath)
			throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(new File(repoPath));
		Repository repository = git.getRepository();
		ObjectId head = repository.resolve(Constants.HEAD);
		Iterable<RevCommit> commits = git.log().add(head).setMaxCount(1).call();
		for (RevCommit commit : commits) {
			return commit.getId().getName();
		}
		return null;
	}
	
	/**
	 * Возвращает все коммиты репозитория
	 * @throws IOException 
	 * @throws GitAPIException 
	 * @throws NoHeadException 
	 */
	public static List<RevCommit> getAllRevCommits(String repoPath) throws IOException, NoHeadException, GitAPIException{
		ArrayList<RevCommit> result = new ArrayList<>();
		Git git = Git.open(new File(repoPath));
		Iterable<RevCommit> commits = git.log().all().call();
		for (RevCommit commit : commits) {
			result.add(commit);
		}
		git.close();
		return result;
	}
	
	public static List<GitRevisionModel> getAllRevisions(String repoPath, String from) throws IOException, NoHeadException, GitAPIException{		
		List<GitRevisionModel> result = new ArrayList<>();
		Git git = Git.open(new File(repoPath));
		LogCommand logCommand = git.log(); 
		//LogCommand logCommand = git.log().all();  // все коммиты для всех веток (в экспорте не требуется) 
		Iterable<RevCommit> commits = logCommand.call();
		
		
		GitRevisionModel start = null;
		for (RevCommit commit : commits) {
			GitRevisionModel revisionModel = new GitRevisionModel(commit);
			result.add(0, revisionModel);
			if (from != null && commit.getId().startsWith(AbbreviatedObjectId.fromString(from)) || commit.getId().getName().equals(from)) {
				start = revisionModel;
			}
		}
		
		if (start != null) {
			int index = result.indexOf(start);
			if (index >= 0) {
				result = result.subList(index+1, result.size());
			}
		}				
		git.close();
		return result;
	}
		
	/**
	 * Возвращает тег по коммиту
	 */
	public static String getRepoTag(String repoPath, String hash) {
		int tagTime = 0;
		String tag = null;
		try {
			Git git = Git.open(new File(repoPath));
			RevWalk walk = new RevWalk(git.getRepository());
			Map<String, Ref> tags = git.getRepository().getTags();
			for (Entry<String, Ref> entry : tags.entrySet()) {
				Ref ref = entry.getValue();				
				RevObject revObj = walk.parseAny(ref.getObjectId());
				int type = revObj.getType();
				if (type == 4) {
					RevTag revTag = walk.parseTag(ref.getObjectId());
					int time = walk.parseCommit(revObj.getId()).getCommitTime();
					if (time > tagTime) {

						for (RevCommit commit : git.log().add(revTag.getObject()).setMaxCount(1)
								.call()) {
							if (commit.getName().equals(hash)) {
								tagTime = time;
								tag = entry.getKey();
							}
						}
					}
				} else {
					int time = walk.parseCommit(revObj.getId()).getCommitTime();
					if (time > tagTime) {
						for (RevCommit commit : git.log().add(revObj).setMaxCount(1).call()) {
							if (commit.getName().equals(hash)) {
								tagTime = time;
								tag = entry.getKey();
							}
						}
					}
				}
			}
			walk.close();
			git.close();
		} catch (IOException | RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}		
		return tag;
	}
	
	//======================================
	// is repo
	
	public static boolean isGitRepo(String repoPath) {
		Path gitPath = Paths.get(repoPath, ".git");				
		return Files.exists(gitPath);
	}
	
	// ===============================
	// file in repo / commits

	/**
	 * Возвращает информацию о коммите или null если не найден
	 * Указывать можно hash, tag, HEAD, HEAD~2 и т.п. 
	 */
	public static CommitInfo getCommitInfoByAll(DipProject dipProject, String hashOrTag) {
		Repository repo = dipProject.getGitRepo();
		if (repo == null) {
			return null;
		}						
		return getCommitInfoByAll(repo, hashOrTag);
	}
	
	/**
	 * Возвращает информацию о коммите или null если не найден
	 * Указывать можно hash, tag, HEAD, HEAD~2 и т.п. 
	 */
	public static CommitInfo getCommitInfoByAll(Repository repo, String hashOrTag) {
		RevCommit commit = getCommitByAll(repo, hashOrTag);
		if (commit != null) {
			return new CommitInfo(commit, repo.getDirectory().getParentFile().getAbsolutePath());
		}
		return null;
	}
	
	/**
	 * Возвращает RevCommit или null если не найден
	 * Указывать можно hash, tag, HEAD, HEAD~2 и т.п. 
	 */
	private static RevCommit getCommitByAll(Repository repo, String hashOrTag) {		
		if (hashOrTag.equals("HEAD")) {
			try {
				Git git = Git.open(repo.getDirectory().getParentFile());
				return getHead(git);
			} catch (RevisionSyntaxException | IOException | GitAPIException e) {
				e.printStackTrace();
			}

			return null;
		}
		if (hashOrTag.startsWith("HEAD~")) {
			try {
				int commitNumber = Integer.parseInt(hashOrTag.substring(5));
				Git git = Git.open(repo.getDirectory().getParentFile());
				return getCommitByNumberFromHead(git, commitNumber);
			} catch (NumberFormatException | RevisionSyntaxException | IOException | GitAPIException e) {
				e.printStackTrace();
			}
			return null;			
		}

		// get by tag
		try {
			Ref ref = repo.findRef(hashOrTag);
			if (ref != null) {			
				RevWalk walk = new RevWalk(repo);
				RevTag tag = walk.parseTag(ref.getObjectId());
				walk.close();
				if (tag != null) {
					RevObject object = tag.getObject();
					if (object instanceof RevCommit) {
						hashOrTag = object.getName();
					}			
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		// getByCommit
		try {
			RevCommit commit = getRevCommitByHash(repo, hashOrTag);
			if (commit != null) {
				return commit;
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static RevCommit getHead(Git git) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException, NoHeadException, GitAPIException {
		Repository repository = git.getRepository();
		ObjectId head = repository.resolve(Constants.HEAD);
		Iterable<RevCommit> commits = git.log().add(head).setMaxCount(1).call();
		Iterator<RevCommit> iterator = commits.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
		
	private static RevCommit getCommitByNumberFromHead(Git git, int number) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException, NoHeadException, GitAPIException {
		ObjectId head = git.getRepository().resolve( "HEAD~" + number );
		Iterable<RevCommit> commits = git.log().add(head).setMaxCount(1).call();
		Iterator<RevCommit> iterator = commits.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}
	
	/**
	 * Возвращает RevCommit по Hash
	 */
	public static RevCommit getRevCommitByHash(Repository repo, String commitHash) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		ObjectId commitId = ObjectId.fromString(commitHash);
		try (RevWalk revWalk = new RevWalk(repo)) {
		  return revWalk.parseCommit(commitId);
		}
	}
	
	/**
	 * Возвращает предыдущий коммит
	 */
	public static RevCommit getPrevCommit(RevCommit commit, Repository repo) throws IOException {
		try (RevWalk walk = new RevWalk(repo)) {
			// Starting point
			walk.markStart(commit);
			int count = 0;
			for (RevCommit rev : walk) {
				// got the previous commit.
				if (count == 1) {
					return rev;
				}
				count++;
			}
			walk.dispose();
		}
		return null;
	}
	
	/**
	 * Возвращает первый коммит
	 */
	public static RevCommit getStartCommit(Repository repo) throws RevisionSyntaxException, AmbiguousObjectException, IncorrectObjectTypeException, IOException {
		try(RevWalk revWalk = new RevWalk(repo)) {
			ObjectId commitId = repo.resolve(Constants.HEAD);
			RevCommit commit = revWalk.parseCommit(commitId);	
			revWalk.markStart(commit);
			RevCommit first = null;
			for (RevCommit rev : revWalk) {
				first = rev;
			}
			return first;
		}
	}
	
	
	/**
	 * Возвращает все комитты для файла
	 */
	public static List<String> getCommits(String repoPath, String filePath)
			throws IOException, NoHeadException, GitAPIException {
		ArrayList<String> result = new ArrayList<>();
		Git git = Git.open(new File(repoPath));
		Iterable<RevCommit> commits = git.log().all().addPath(filePath).call();
		for (RevCommit commit : commits) {
			result.add(commit.getId().getName());
		}
		git.close();
		return result;
	}
	
	public static List<RevCommit> getAllRevCommits(String repoPath, String filePath)
			throws IOException, NoHeadException, GitAPIException {
		ArrayList<RevCommit> result = new ArrayList<>();
		Git git = Git.open(new File(repoPath));
		Iterable<RevCommit> commits = git.log().all().addPath(filePath).call();
		for (RevCommit commit : commits) {
			result.add(commit);
		}
		git.close();
		return result;
	}
	
	public static List<RevCommit> getRevCommits(String repoPath, String filePath)
			throws IOException, NoHeadException, GitAPIException {
		List<RevCommit> result = new ArrayList<>();
		Git git = Git.open(new File(repoPath));
		Iterable<RevCommit> commits = git.log().addPath(filePath).call();
		for (RevCommit commit : commits) {
			result.add(commit);
		}
		git.close();
		return result;
	}
	
	/**
	 * Возвращает тег по коммиту
	 */
	public static String getTag(String repoPath, String filePath, String hash) {
		int tagTime = 0;
		String tag = null;
		try {
			Git git = Git.open(new File(repoPath));
			RevWalk walk = new RevWalk(git.getRepository());
			Map<String, Ref> tags = git.getRepository().getTags();
			for (Entry<String, Ref> entry : tags.entrySet()) {
				Ref ref = entry.getValue();
				RevObject revObj = walk.parseAny(ref.getObjectId());
				int type = revObj.getType();
				if (type == 4) {
					RevTag revTag = walk.parseTag(ref.getObjectId());
					int time = walk.parseCommit(revObj.getId()).getCommitTime();
					if (time > tagTime) {

						for (RevCommit commit : git.log().addPath(filePath).add(revTag.getObject()).setMaxCount(1)
								.call()) {
							if (commit.getName().equals(hash)) {
								tagTime = time;
								tag = entry.getKey();
							}
						}
					}
				} else {
					int time = walk.parseCommit(revObj.getId()).getCommitTime();
					if (time > tagTime) {
						for (RevCommit commit : git.log().addPath(filePath).add(revObj).setMaxCount(1).call()) {
							if (commit.getName().equals(hash)) {
								tagTime = time;
								tag = entry.getKey();
							}
						}
					}
				}
			}
			walk.close();
			git.close();
		} catch (IOException | RevisionSyntaxException e) {
			e.printStackTrace();
		} catch (NoHeadException e) {
			e.printStackTrace();
		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		
		return tag;
	}

	/**
	 * Все файлы которые есть в этом коммите (не измененные, а все дерево)
	 */
	public static List<Path> getFilesForCommit(RevCommit commit, Repository repo) throws MissingObjectException, IncorrectObjectTypeException, CorruptObjectException, IOException{
		List<Path> result = new ArrayList<>();
		ObjectId treeId = commit.getTree().getId();
		try (TreeWalk treeWalk = new TreeWalk(repo)) {
			  treeWalk.reset(treeId);	
			  treeWalk.setRecursive(true);
			  while (treeWalk.next()) {
			    String path = treeWalk.getPathString();
			    Path fullPath = repo.getDirectory().getParentFile().toPath().resolve(path);
			    result.add(fullPath);
			  }
		}
		return result;
	}
	
	/**
	 * Возвращает статус (текущий хеш и был ли изменен)
	 */
	public static GitFileStatus checkStatus(String repoPath, String filePath) {
		try {
			Git git = Git.open(new File(repoPath));
			String hash = getCurrentHash(git, filePath);
			boolean changed = isChanged(git, filePath);
			if (hash == null) {
				return null;
			}
			return new GitFileStatus(hash, changed);
		} catch (IOException | RevisionSyntaxException | GitAPIException e) {
			return null;
		}
	}
	
	

	
	/**
	 * Возвращает текущий хэш
	 */
	private static String getCurrentHash(Git git, String filePath) throws RevisionSyntaxException,
			AmbiguousObjectException, IncorrectObjectTypeException, IOException, NoHeadException, GitAPIException {
		Repository repository = git.getRepository();
		ObjectId head = repository.resolve(Constants.HEAD);
		Iterable<RevCommit> commits = git.log().add(head).addPath(filePath).setMaxCount(1).call();
		for (RevCommit commit : commits) {
			return commit.getId().getName();
		}
		return null;
	}

	/**
	 * Статус. Изменён ли файл
	 */
	public static boolean isChanged(String repoPath, String filePath)
			throws IOException, NoWorkTreeException, GitAPIException {
		Git git = Git.open(new File(repoPath));
		return isChanged(git, filePath);
	}

	/**
	 * Статус. Изменеён ли файл
	 */
	private static boolean isChanged(Git git, String filePath) throws NoWorkTreeException, GitAPIException {
		Status status = git.status().addPath(filePath).call();
		return !status.getModified().isEmpty();
	}
	
	/**
	 * Возвращает Describe (для коммита в котором файл был изменен) либо null если нет репозитория
	 */
	public static String getDescribe(IDipDocumentElement dde) {
		Repository repository = dde.dipProject().getGitRepo();		
		try {
			if (repository != null) {
				// возможно имеет смысл устанавливать хэш в поле проекта перед обновлением, чтобы не высчитывать для каждого элемента				
				String currentHash = getCurrentHash(repository.getDirectory().getAbsolutePath());
				try {
					return getDescribe(repository, currentHash, dde);
				} catch (IOException | GitAPIException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException | GitAPIException e1) {
			e1.printStackTrace();
		}	
		return null;
	}
	
	
	private static String getDescribe(Repository repo, String currentHash, IDipDocumentElement dde)
			throws IOException, NoHeadException, GitAPIException {
		String commitName = CommitCache.instance.getCommitChanged(dde.resource(), currentHash);
		if (commitName == null) {
			RevCommit commit = getChangedCommit(repo, dde);
			if (commit != null) {
				commitName = commit.getName();				
				CommitCache.instance.putChangedCommit(dde.resource(), currentHash, commitName);
			}
		}
		if (commitName != null) {
			String describe = CommitCache.instance.getDescribe(dde.dipProject().getProject(), commitName);
			if (describe != null) {
				return describe;
			}
			
			Git git = Git.open(repo.getDirectory().getParentFile());
			describe = git.describe().setTarget(commitName).call();
			CommitCache.instance.putDescribe(dde.dipProject().getProject(), commitName, describe);
			return describe;
		}
		return null;
	}

	/**
	 * Возвращает коммит в котором файл был изменен
	 */
	public static RevCommit getChangedCommit(Repository repo, IDipDocumentElement dde) throws IOException, NoHeadException, GitAPIException {
		Git git = Git.open(repo.getDirectory().getParentFile());
		String path = getRepoPath(repo, dde.resource());
		path = path.replace('\\', '/');
		Iterable<RevCommit> commits = git.log().addPath(path).setMaxCount(1).call();
		Iterator<RevCommit> iterator = commits.iterator();
		if (iterator.hasNext()) {
			RevCommit commit = iterator.next();
			if (commit != null) {
				return commit;
			}
		}
		return null;
	}
	
	
	/**
	 * Возвращает путь относительно репозитория
	 */
	public static String getRepoPath(Repository repo, IResource resoure) {
		Path repoPath = repo.getDirectory().getParentFile().toPath();
		Path resourcePath = Path.of(resoure.getLocationURI());
		return repoPath.relativize(resourcePath).toString();
	}
	
	//=============================
	// Submodules
	
	/**
	 * Обновить сабмодули 
	 */
	public static void updateSubmodules(Repository repo){
		SubmoduleUpdateCommand command = new SubmoduleUpdateCommand(repo);
		try {
			command.setFetch(true).call();
		} catch (Exception e1) {
			e1.printStackTrace();
			WorkbenchUtitlities.openError("Submodule Update Error", e1.getMessage());
		}
	}
	
	//================================
	// diff
	
	/**
	 * Возвращает Diff для Staging
	 * @param paths - фильтр для путей внутри репозитория
	 * @throws GitAPIException 
	 */
	public static List<DiffEntry> stagingChanged(Git git, Repository repo, Set<Path> paths) throws GitAPIException {
		TreeFilter filter = getTreeFilter(repo, paths);
		List<DiffEntry> diffs = git.diff().setPathFilter(filter).call();
		diffs.addAll(git.diff().setCached(true).setPathFilter(filter).call());
		return diffs;
	}
	
	/**
	 * Возвращает Diff между двумя коммитами
	 * @param paths - фильтр для путей внутри репозитория (должны быть полные пути, потом сам определит относительные)
	 */
	public static List<DiffEntry> computeChanged(Repository repo, Git git, RevCommit revCommit1, RevCommit revCommit2,
			Collection<Path> paths) throws IOException, GitAPIException {
		ObjectId oldObject = repo.resolve(revCommit1.getTree().getName());
		ObjectId newObject = repo.resolve(revCommit2.getTree().getName());
		return computeChanged(repo, git, oldObject, newObject, paths);
	}

	/**
	 * Возвращает Diff между двумя коммитами
	 * @param paths - фильтр для путей внутри репозитория (должны быть полные пути, потом сам определит относительные)
	 */
	public static List<DiffEntry> computeChanged(Repository repo, Git git, ObjectId objId1, ObjectId objId2,
			Collection<Path> paths) throws IncorrectObjectTypeException, IOException, GitAPIException {
		ObjectReader reader = repo.newObjectReader();
		CanonicalTreeParser oldIter = new CanonicalTreeParser();
		oldIter.reset(reader, objId1);
		CanonicalTreeParser newIter = new CanonicalTreeParser();
		newIter.reset(reader, objId2);
		DiffCommand diffCommand = git.diff().setNewTree(newIter).setOldTree(oldIter);
		if (paths != null) {
			TreeFilter filter = getTreeFilter(repo, paths);
			diffCommand = diffCommand.setPathFilter(filter);
		}
		return diffCommand.call();
	}
	
	private static TreeFilter getTreeFilter(Repository repo, Collection<Path> paths) {	
		Path fRepoPath = Paths.get(repo.getDirectory().getAbsolutePath()).getParent();
		Set<String> strPaths = paths.stream().map(path -> fRepoPath.relativize(path).toString()).collect(Collectors.toSet());		
		return new TreeFilter() {
			
			@Override
			public boolean shouldBeRecursive() {
				return false;
			}
			
			@Override
			public boolean include(TreeWalk tree) throws MissingObjectException, IncorrectObjectTypeException, IOException {
				for (String parentPath: strPaths) {
					if (parentPath.startsWith(tree.getPathString()) 
							|| tree.getPathString().startsWith(parentPath)) {
						return true;
					} 					
				}			
				return false;
			}
			
			@Override
			public TreeFilter clone() {
				return null;
			}
		};			
	}
	
	//=========================================
	// get content from commit
	
	public static String getContent(Repository repo, RevCommit revCommit, String path) throws IOException {
		byte[] bytes = getDataFromCommit(repo, revCommit, path);
		return new String(bytes, StandardCharsets.UTF_8);
	}
	
	public static InputStream getInputStream(Repository repo, RevCommit revCommit, String path) throws IOException {
		byte[] bytes = getDataFromCommit(repo, revCommit, path);
		return new ByteArrayInputStream(bytes);
	}
	
	private static byte[] getDataFromCommit(Repository repo, RevCommit revCommit, String path) throws IOException {
		try (TreeWalk treeWalk = TreeWalk.forPath(repo, path, revCommit.getTree())) {
			ObjectId blobId = treeWalk.getObjectId(0);
			try (ObjectReader objectReader = repo.newObjectReader()) {
				ObjectLoader objectLoader = objectReader.open(blobId);
				return objectLoader.getBytes();
			}
		}
	}
	
}
