package com.fuhu.server.core;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.RemoteSession;
import org.eclipse.jgit.transport.SshSessionFactory;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.FS;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

public class JGitTool {

	private static final Logger log = LoggerFactory.getLogger(JGitTool.class);

	private JSONObject treeObj;
	private int treeIndex;
	private JSONArray temp;
	private JSONObject folder;

	public JGitTool() {
		treeObj = new JSONObject();
		treeIndex = 0;

	}

	/**
	 * Create git repository
	 * 
	 * @param path
	 *            - Create repository with path
	 * @throws GitAPIException
	 * 
	 * @throws IOException
	 */
	public void createGit(String path) throws IOException {
		log.info("Create git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check dirPath
		validateString(path);
		File folder = new File(path);
		try {
			// check folder is existed or not
			if (!folder.exists()) {
				folder.mkdir();
			}
			if (builder.findGitDir(folder).getGitDir() == null) {
				Repository repository = builder.setWorkTree(folder).build();
				repository.create();
				// Git git = new Git(repository);
				// git.init().call();
				log.info("Repository is created, the path is \""
						+ repository.getDirectory() + "\"");
				repository.close();
				log.info("Create git complete!!!~~");
			} else {
				log.info("Repository is existed!!!!!");
				throw new IllegalArgumentException();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Clone git repository for HTTP
	 * 
	 * @param dirPath
	 *            - local git path
	 * @param gitPath
	 *            - remote git path
	 * @param username
	 *            - username
	 * @param password
	 *            - password
	 * @throws Exception
	 * 
	 */
	public void cloneGit(String dirPath, String gitPath, String username,
			String password) throws Exception {
		log.info("Clone start!!!~~");
		// check gitPath
		validateString(gitPath);
		// check dirPath
		validateString(dirPath);
		// check username
		validateString(username);
		// check password
		validateString(password);
		File folder = new File(dirPath);
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			// check local folder is exist or not
			if (!folder.exists()) {
				folder.mkdir();
			}
			FileRepositoryBuilder existGit = builder.findGitDir(folder);
			// check local git is not existed
			if (existGit.getGitDir() == null) {
				// check .git is exist or not
				Git.cloneRepository()
						.setURI(gitPath)
						.setDirectory(folder)
						.setCredentialsProvider(
								new UsernamePasswordCredentialsProvider(
										username, password)).call();
			} else {
				log.info("Repository is existed!!!!!");
				throw new IllegalArgumentException();
			}
			log.info("Clone complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Clone git repository for SSH
	 * 
	 * @param dirPath
	 *            - local git path
	 * @param gitPath
	 *            - remote git path
	 * @param password
	 *            - password
	 * @throws Exception
	 * 
	 */
	public void cloneGit(String dirPath, String gitPath, String password)
			throws Exception {
		log.info("Clone start!!!~~");
		// check gitPath
		validateString(gitPath);
		// check dirPath
		validateString(dirPath);
		// check password
		validateString(password);
		File folder = new File(dirPath);
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			// check local folder is exist or not
			if (!folder.exists()) {
				folder.mkdir();
			}
			// FileRepositoryBuilder existGit = builder.findGitDir(folder);
			// check local git is not existed
			// if (existGit.getGitDir() == null) {
			CredentialsProvider provider = getCredentialProvider(password);
			// check .git is exist or not
			Git.cloneRepository().setURI(gitPath).setDirectory(folder)
					.setCredentialsProvider(provider).setTimeout(100).call();
			// } else {
			// log.info("Repository is existed!!!!!");
			// throw new IllegalArgumentException();
			// }
			log.info("Clone complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Clone git repository for SSH
	 * 
	 * @param dirPath
	 *            - local git path
	 * @param gitPath
	 *            - remote git path
	 * @param sshPassword
	 *            - ssh password
	 * @param password
	 *            - remote server password
	 * @throws Exception
	 * 
	 */
	// @Deprecated
	// public void cloneGitForTemp(String dirPath, String gitPath,
	// String sshPassword, String password) throws Exception {
	// log.info("Clone start!!!~~");
	//
	// // check gitPath
	// validateString(gitPath);
	// // check dirPath
	// validateString(dirPath);
	// // check password
	// validateString(password);
	// File folder = new File(dirPath);
	// // new FileRepositoryBuilder
	// FileRepositoryBuilder builder = new FileRepositoryBuilder();
	// try {
	// // check local folder is exist or not
	// if (!folder.exists()) {
	// folder.mkdir();
	// }
	// FileRepositoryBuilder existGit = builder.findGitDir(folder);
	// // check local git is not existed
	// if (existGit.getGitDir() == null) {
	//
	// CredentialsProvider provider = getCredentialProvider(sshPassword);
	//
	// // check .git is exist or not
	// Git.cloneRepository()
	// .setURI(gitPath)
	// .setDirectory(folder)
	// .setTimeout(100)
	// .setCredentialsProvider(provider)
	// //.setTransportConfigCallback(getTransportConfig(password))
	// .call();
	// } else {
	// log.info("Repository is existed!!!!!");
	// throw new IllegalArgumentException();
	// }
	// log.info("Clone complete!!!~~");
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// }
	// }

	/**
	 * Get TransportConfig
	 * 
	 * @param password
	 *            - remote server password
	 * @return TransportConfigCallback
	 */
	private TransportConfigCallback getTransportConfig(String password) {
		final String pwd = password;
		TransportConfigCallback config = new TransportConfigCallback() {
			@Override
			public void configure(Transport transport) {
				if (transport instanceof SshTransport) {
					((SshTransport) transport)
							.setSshSessionFactory(new JschConfigSessionFactory() {
								@Override
								protected void configure(Host host,
										Session session) {
									session.setPassword(pwd);
								}
							});
				}
			}
		};
		return config;
	}

	/**
	 * Get credential provider
	 * 
	 * @param password
	 *            - password
	 * @return CredentialsProvider
	 */
	private CredentialsProvider getCredentialProvider(String password) {
		final String pws = password;

		return new CredentialsProvider() {

			@Override
			public boolean supports(CredentialItem... items) {
				return true;
			}

			@Override
			public boolean isInteractive() {
				return true;
			}

			@Override
			public boolean get(URIish uri, CredentialItem... items)
					throws UnsupportedCredentialItem {

				for (CredentialItem item : items) {
					if (item instanceof CredentialItem.StringType) {
						((CredentialItem.StringType) item).setValue(new String(
								pws));
						continue;
					}
				}
				return true;
			}
		};
	}

	/**
	 * Open existing local git repository
	 * 
	 * @param gitPath
	 *            - remote git path
	 * @param dirPath
	 *            - local git path
	 * @param username
	 *            - username
	 * @param password
	 *            - password
	 * @throws FileNotFoundException
	 * 
	 */
	@SuppressWarnings("finally")
	public Repository openExistingGit(String existedGitPath)
			throws FileNotFoundException {
		log.info("Open git start!!!~~");
		Repository repository = null;
		// check dirPath
		validateString(existedGitPath);
		File folder = new File(existedGitPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			repository = builder.setGitDir(folder).readEnvironment().build();
			log.info("Open git complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return repository;
		}
	}

	/**
	 * Clone git repository
	 * 
	 * @param gitPath
	 *            - remote git path
	 * @param dirPath
	 *            - local git path
	 * @throws Exception
	 * 
	 */
	public void cloneGit(String gitPath, String dirPath) throws Exception {
		log.info("Clone git start!!!~~");
		// check gitPath
		validateString(gitPath);
		// check dirPath
		validateString(dirPath);
		// local folder
		File folder = new File(dirPath);
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		try {
			// check local folder is existed or not
			if (!folder.exists()) {
				folder.mkdir();
			}
			FileRepositoryBuilder existGit = builder.findGitDir(folder);
			// check local git is not existed
			if (existGit.getGitDir() == null) {
				Git.cloneRepository().setURI(gitPath).setDirectory(folder)
						.call();
			} else {
				log.info("Repository is existed!!!!!");
				throw new IllegalArgumentException();
			}
			log.info("Clone Complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Add index to git
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch Name
	 * @param filename
	 *            - file name with extension(ex: aaa.txt)
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void addToGit(String repositoryPath, String filename)
			throws IOException, GitAPIException {
		log.info("Add git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// if filefullname is ".",add all files in repository
			if (filename.startsWith(".")) {
				for (File file : folder.listFiles()) {
					if (!file.getName().equals(".git")) {
						AddCommand add = git.add();
						add.addFilepattern(filename).call();
						git.close();
					}
				}
			} else {
				AddCommand add = git.add();
				add.addFilepattern(filename).call();
				git.close();
			}
			log.info("Add git complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Add tag to current Head
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch name
	 * @param tagName
	 *            - Tag name
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void addTag(String repositoryPath, String branchName, String tagName)
			throws IOException, GitAPIException {
		log.info("Add tag start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check commitId
		validateString(branchName);
		// check tagName
		validateString(tagName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Get object id from given branch
			ObjectId objId = git.getRepository().resolve(
					"refs/heads/" + branchName);
			RevWalk walk = new RevWalk(repository);
			RevCommit commit = walk.parseCommit(objId);
			git.tag().setName(tagName).setObjectId(commit).call();
			git.close();
			log.info("Add tag complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Delete tag with tag name
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param tagName
	 *            - Tag name
	 * 
	 * @throws FileNotFoundException
	 * 
	 */
	public void deleteTag(String repositoryPath, String tagName)
			throws FileNotFoundException {
		log.info("Delte tag start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check tagName
		validateString(tagName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Delete tag
			git.tagDelete().setTags(tagName).call();
			git.close();
			log.info("Delte tag complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Commit to git
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch name
	 * @param message
	 *            - Commit comment
	 * @param author
	 *            - Author
	 * @param email
	 *            - Author's email
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void commitToGit(String repositoryPath, String branchName,
			String message, String author, String email) throws IOException,
			GitAPIException {
		log.info("Commit git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check branchName
		validateString(branchName);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// check any branch is existing or not
			List<Ref> refList = git.branchList().call();
			if (refList.size() != 0) {
				git.checkout().setName(branchName).call();
			}
			CommitCommand commit = git.commit();
			commit.setMessage(message).setAuthor(author, email).call();
			git.close();
			log.info("Commit git complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Commit to git
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch name
	 * @param message
	 *            - Commit comment
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void commitToGit(String repositoryPath, String branchName,
			String message) throws IOException, GitAPIException {
		log.info("Commit git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// check any branch
			if (git.branchList().call().size() != 0) {
				git.checkout().setName(branchName).call();
			}
			CommitCommand commit = git.commit().setAll(true);
			commit.setMessage(message).call();
			git.close();
			log.info("Commit git complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Create new branch
	 * 
	 * @param repositoryPath
	 *            - Repository path
	 * @param branchName
	 *            - Branch Name
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void createNewBranch(String repositoryPath, String branchName)
			throws IOException, GitAPIException {
		log.info("Create new branch start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check branch name
		validateString(branchName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Status status = git.status().call();
			git.branchCreate().setName(branchName).call();
			git.close();
			log.info("Create new branch complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Change branch
	 * 
	 * @param repositoryPath
	 *            - Repository path
	 * @param branchName
	 *            - Branch Name
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void changeBranch(String repositoryPath, String branchName)
			throws IOException, GitAPIException {
		log.info("Change branch start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check branch name
		validateString(branchName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Status status = git.status().call();
			git.checkout().setName(branchName).call();
			git.close();
			log.info("Create new branch complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Delete branch
	 * 
	 * @param repositoryPath
	 *            - Repository path
	 * @param branchName
	 *            - Branch Name
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void deleteBranch(String repositoryPath, String branchName)
			throws IOException, GitAPIException {
		log.info("Delete branch start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check branch name
		validateString(branchName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Status status = git.status().call();
			git.branchDelete().setBranchNames(branchName).call();
			git.close();
			log.info("Delete branch complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Merge branch
	 * 
	 * @param repositoryPath
	 *            - Repository path
	 * @param targetName
	 *            - Target branch name
	 * @param message
	 *            - Comment
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public void mergeBranch(String repositoryPath, String originName,
			String targetName, String message) throws IOException,
			GitAPIException {
		log.info("Merge branch start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check source branch name
		validateString(originName);
		// check target branch name
		validateString(targetName);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Get origin branch
			// ObjectId originId = git.getRepository().resolve(
			// "refs/heads/" + originName);
			// Get target branch
			ObjectId targetId = git.getRepository().resolve(
					"refs/heads/" + targetName);
			// Checkout origin branch
			git.checkout().setName(originName).call();
			// merge target branch with origin branch
			MergeResult result = git.merge().include(targetId)
					.setMessage(message).call();
			log.info("Result of merging~~~~~>> " + result.getMergeStatus());
			git.close();
			log.info("Merge branch complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;

		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Get git status
	 * 
	 * @param repositoryPath
	 *            - Repository path
	 * 
	 * 
	 * @throws FileNotFoundException
	 * 
	 */
	public List<String> getStatus(String repositoryPath, boolean isFile)
			throws FileNotFoundException {
		log.info("Get status start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		// Status list
		List<String> statusList = new ArrayList<String>();
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			//
			Status status = git.status().call();

			String untrack = "Untracked files: "
					+ status.getUntracked().toString();
			String added = "Added files: " + status.getAdded().toString();
			String modified = "Modified files: "
					+ status.getModified().toString();
			String changed = "Changed files: " + status.getChanged().toString();
			String conflicted = "Conflicted files: "
					+ status.getConflicting().toString();
			String missing = "Missing files: " + status.getMissing().toString();
			String removed = "Removed files: " + status.getRemoved().toString();
			String uncommited = "Uncommited files: "
					+ status.getUncommittedChanges().toString();
			statusList.add(untrack);
			statusList.add(added);
			statusList.add(modified);
			statusList.add(changed);
			statusList.add(conflicted);
			statusList.add(missing);
			statusList.add(removed);
			statusList.add(uncommited);
			for (String str : statusList) {
				log.info("~~~~>>" + str);
			}
			// Check write into file
			if (isFile) {
				writeStatusToFile(statusList, folder.getName());
			}
			git.close();
			log.info("Get status complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return statusList;
		}
	}

	/**
	 * Get git log from repository
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch Name
	 * @param count
	 *            - Show the ammount of logs - If you want list all log, filling
	 *            count with 0
	 * @return List<String> - commitnumber--messages--committime
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public List<String> getLogFromGit(String repositoryPath, String branchName,
			int count, boolean isFile) throws IOException, GitAPIException {
		log.info("Get log start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		Iterable<RevCommit> logg = null;
		List<String> logList = new ArrayList<String>();

		// check repositoryPath
		validateString(repositoryPath);
		// check branchName
		validateString(branchName);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Get log from given branch
			LogCommand logCommand = git.log().add(
					git.getRepository().resolve("refs/heads/" + branchName));
			// Check log count
			if (count == 0) {
				logg = logCommand.call();
			} else {
				logg = logCommand.setMaxCount(count).call();
			}
			Iterator<RevCommit> logs = logg.iterator();
			while (logs.hasNext()) {
				RevCommit rev = logs.next();
				String str = rev.getName() + "--" + rev.getShortMessage()
						+ "--" + rev.getAuthorIdent().getWhen();
				logList.add(str);
			}
			// Write commit log to file
			if (isFile) {
				writeCommitLogToFile(logList, branchName);
			}
			git.close();
			log.info("Get log complete!!!~~");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (NoFilepatternException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

		return logList;
	}

	/**
	 * Push to repository for HTTP
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param branchName
	 *            - Branch name
	 * @param userName
	 *            - The username of remote repository
	 * @param password
	 *            - The password of remote repository
	 * 
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void push(String repositoryPath, String branchName, String userName,
			String password) throws IOException, GitAPIException {
		log.info("Push start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check source branch name
		validateString(branchName);
		// check username
		validateString(userName);
		// check password
		validateString(password);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);

			// push branch to local/remote repository
			git.push()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(userName,
									password)).setTimeout(100).add(branchName)
					.call();
			git.close();
			log.info("Push complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Push to repository for SSH
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param branchName
	 *            - Branch name
	 * @param password
	 *            - The password of remote repository
	 * 
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void push(String repositoryPath, String branchName, String password)
			throws IOException, GitAPIException {
		log.info("Push start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check source branch name
		validateString(branchName);
		// check password
		validateString(password);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			CredentialsProvider provider = getCredentialProvider(password);
			// push branch to local/remote repository
			git.push().setCredentialsProvider(provider).setTimeout(100)
					.add(branchName).call();
			git.close();
			log.info("Push complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Push to repository for SSH
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param branchName
	 *            - Branch name
	 * @param sshpassword
	 *            - ssh password
	 * 
	 * @param password
	 *            - remote server password
	 * 
	 * @throws IOException
	 * @throws GitAPIException
	 */
	// @Deprecated
	// public void pushForTemp(String repositoryPath, String branchName,
	// String sshpassword, String password) throws IOException,
	// GitAPIException {
	// log.info("Push start!!!~~");
	// // new FileRepositoryBuilder
	// FileRepositoryBuilder builder = new FileRepositoryBuilder();
	// // check repositoryPath
	// validateString(repositoryPath);
	// // check source branch name
	// validateString(branchName);
	// // check password
	// validateString(password);
	// //
	// File folder = new File(repositoryPath);
	// // check folder is existed or not
	// validatePathExisted(folder);
	// // check git is existed or not
	// validateGitExisted(folder);
	// try {
	// Repository repository = builder.setWorkTree(folder).build();
	// Git git = new Git(repository);
	// CredentialsProvider provider = getCredentialProvider(sshpassword);
	// // push branch to local/remote repository
	// git.push().setCredentialsProvider(provider)
	// .setTransportConfigCallback(getTransportConfig(password))
	// .setTimeout(100).add(branchName).call();
	// git.close();
	// log.info("Push complete!!!~~");
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (RefAlreadyExistsException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (RefNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (InvalidRefNameException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (GitAPIException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// }
	//
	// }

	/**
	 * Pull from repository for HTTP
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param userName
	 *            - The username of remote repository
	 * @param password
	 *            - The password of remote repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void pull(String repositoryPath, String userName, String password)
			throws IOException, GitAPIException {
		log.info("Pull start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check username
		validateString(userName);
		// check password
		validateString(password);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// pull from local/remote repository
			git.pull()
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(userName,
									password)).setTimeout(100).call();
			git.close();
			log.info("Pull complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Pull from repository for SSH
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param sshPassword
	 *            - SSH password
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void pull(String repositoryPath, String sshPassword)
			throws IOException, GitAPIException {
		log.info("Pull start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check password
		validateString(sshPassword);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// pull from local/remote repository
			CredentialsProvider provider = getCredentialProvider(sshPassword);
			git.pull().setCredentialsProvider(provider).setTimeout(100).call();
			git.close();
			log.info("Pull complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Pull from repository
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param sshPassword
	 *            - ssh password
	 * @param password
	 *            - remote server password
	 * @throws IOException
	 * @throws GitAPIException
	 */
	// @Deprecated
	// public void pullForTemp(String repositoryPath, String sshPassword,
	// String password) throws IOException, GitAPIException {
	// log.info("Pull start!!!~~");
	// // new FileRepositoryBuilder
	// FileRepositoryBuilder builder = new FileRepositoryBuilder();
	// // check repositoryPath
	// validateString(repositoryPath);
	// // check sshPassword
	// validateString(sshPassword);
	// // check password
	// validateString(password);
	// //
	// File folder = new File(repositoryPath);
	// // check folder is existed or not
	// validatePathExisted(folder);
	// // check git is existed or not
	// validateGitExisted(folder);
	// try {
	// Repository repository = builder.setWorkTree(folder).build();
	// Git git = new Git(repository);
	// // pull from local/remote repository
	// CredentialsProvider provider = getCredentialProvider(sshPassword);
	// git.pull()
	// .setCredentialsProvider(provider)
	// .setTransportConfigCallback(getTransportConfig(password))
	// .setTimeout(100).call();
	// git.close();
	// log.info("Pull complete!!!~~");
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (RefAlreadyExistsException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (RefNotFoundException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (InvalidRefNameException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// } catch (GitAPIException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// }
	// }

	/**
	 * Fetch from repository
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param remotePath
	 *            - Remote repository path
	 * @param userName
	 *            - The username of remote repository
	 * @param password
	 *            - The password of remote repository
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void fetch(String repositoryPath, String remotePath,
			String userName, String password) throws IOException,
			GitAPIException {
		log.info("Fetch start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check remote name
		validateString(remotePath);
		// check username
		validateString(userName);
		// check password
		validateString(password);
		//
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {

			Repository repository = builder.setGitDir(folder).build();
			Git git = new Git(repository);

			// fetch from local/remote repository
			FetchResult result = git
					.fetch()
					.setRemote(remotePath)
					.setCredentialsProvider(
							new UsernamePasswordCredentialsProvider(userName,
									password)).setTimeout(100)
					.setCheckFetchedObjects(true).call();
			log.info("~~~~>>" + result.getMessages());
			git.close();
			log.info("Fetch complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (RefNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (InvalidRefNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (GitAPIException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}

	}

	/**
	 * Diff from git repository with now and previous
	 * 
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * 
	 * @throws FileNotFoundException
	 * 
	 * 
	 * 
	 */
	public List<String> diffGitBetweenNowAndPrevious(String repositoryPath)
			throws FileNotFoundException {
		log.info("Diff git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// Diff log list
		List<String> diffLogList = new ArrayList<String>();
		List<String> diffReturnList = new ArrayList<String>();
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		//
		String originCommit = null;
		String targetCommit = null;
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			Iterator<RevCommit> iter = git.log().call().iterator();
			int i = 0;
			// get first two log from parent git
			while (i < 2) {
				if (i == 0) {
					targetCommit = iter.next().getName();
					System.out.println("first commit--->>" + targetCommit);
				}
				if (i == 1) {
					originCommit = iter.next().getName();
					System.out.println("second commit--->>" + originCommit);
				}
				i++;
				if (i == 2) {
					break;
				}
			}

			// Get source commit tree
			CanonicalTreeParser oldTreeIter = getGitTree(repository,
					originCommit);
			// Get target commit tree
			CanonicalTreeParser newTreeIter = getGitTree(repository,
					targetCommit);
			// compare out diff between old and new
			List<DiffEntry> diffList = git.diff().setNewTree(newTreeIter)
					.setOldTree(oldTreeIter).call();

			for (DiffEntry entry : diffList) {
				// new JSON object
				JSONObject json = new JSONObject();
				if (!entry.getNewPath().contains("null")) {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					json.put(entry.getChangeType(), entry.getNewPath());
				} else {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					json.put(entry.getChangeType(), entry.getOldPath());
				}

				diffReturnList.add(json.toJSONString());
			}
			// Write log into file
			writeToFile(diffLogList, originCommit, targetCommit);
			log.info("Diff git complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return diffReturnList;
		}
	}

	/**
	 * Diff from git repository with commit id
	 * 
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param originCommit
	 *            - Origin commit id
	 * @param targetCommit
	 *            - Compared commit id
	 * @throws FileNotFoundException
	 * 
	 * 
	 * 
	 */
	public List<String> diffGit(String repositoryPath, String originCommit,
			String targetCommit) throws FileNotFoundException {
		log.info("Diff git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// Diff log list
		List<String> diffLogList = new ArrayList<String>();
		List<String> diffReturnList = new ArrayList<String>();
		// check source commit
		validateString(originCommit);
		// check target commit
		validateString(targetCommit);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Get source commit tree
			CanonicalTreeParser oldTreeIter = getGitTree(repository,
					originCommit);
			// Get target commit tree
			CanonicalTreeParser newTreeIter = getGitTree(repository,
					targetCommit);
			// compare out diff between old and new
			List<DiffEntry> diffList = git.diff().setNewTree(newTreeIter)
					.setOldTree(oldTreeIter).call();

			for (DiffEntry entry : diffList) {
				// new JSON object
				JSONObject json = new JSONObject();
				if (!entry.getNewPath().contains("null")) {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					json.put(entry.getChangeType(), entry.getNewPath());
				} else {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					json.put(entry.getChangeType(), entry.getOldPath());
				}

				diffReturnList.add(json.toJSONString());
			}
			// Write log into file
			writeToFile(diffLogList, originCommit, targetCommit);
			log.info("Diff git complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return diffReturnList;
		}
	}

	/**
	 * Diff from git repository with tag name
	 * 
	 * 
	 * @param repositoryPath
	 *            - Local repository path
	 * @param originTag
	 *            - Origin tag
	 * @param targetTag
	 *            - Compared tag
	 * @throws FileNotFoundException
	 * 
	 * 
	 * 
	 */
	public List<String> diffGitWithTag(String repositoryPath, String originTag,
			String targetTag) throws FileNotFoundException {
		log.info("Diff git start!!!~~");
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// Diff log list
		List<String> diffLogList = new ArrayList<String>();
		List<String> diffReturnList = new ArrayList<String>();
		// Origin commit id
		String sourceCommit = null;
		// Target commit id
		String targetCommit = null;
		// check origin tag
		validateString(originTag);
		// check target tag
		validateString(targetTag);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);
		try {
			// Get git repository
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// Get tag from git
			List<Ref> tagList = git.tagList().call();
			// Find commit id with tag name
			for (Ref ref : tagList) {
				// Get tag name from tag list
				String tagName = ref.getName().substring(
						ref.getName().lastIndexOf("/") + 1,
						ref.getName().length());

				// if found origin commit id
				if (tagName.equals(originTag)) {
					sourceCommit = ref.getObjectId().getName();
					continue;
				}

				// if found target commit id
				if (tagName.equals(targetTag)) {
					targetCommit = ref.getObjectId().getName();
					continue;
				}

			}
			// check origin commit id
			validateCommitId(sourceCommit);
			// check target commit id
			validateCommitId(targetCommit);
			// Get source commit tree
			CanonicalTreeParser oldTreeIter = getGitTree(repository,
					sourceCommit);
			// Get target commit tree
			CanonicalTreeParser newTreeIter = getGitTree(repository,
					targetCommit);
			// compare out diff between old and new
			List<DiffEntry> diffNewList = git.diff().setNewTree(newTreeIter)
					.setOldTree(oldTreeIter).call();

			for (DiffEntry entry : diffNewList) {
				// new JSON object
				JSONObject json = new JSONObject();
				if (!entry.getNewPath().contains("null")) {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getNewPath());
					json.put(entry.getChangeType(), entry.getNewPath());
				} else {
					diffLogList.add("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					log.info("CommitId: " + targetCommit + " -->> "
							+ entry.getChangeType() + " " + entry.getOldPath());
					json.put(entry.getChangeType(), entry.getOldPath());
				}

				diffReturnList.add(json.toJSONString());
			}
			// Write log into file
			writeToFile(diffLogList, originTag, targetTag);
			log.info("Diff git complete!!!~~");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return diffReturnList;
		}
	}

	/**
	 * Get working directory tree
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @param branchName
	 *            - Branch Name
	 * @return JSONObject
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	public JSONObject getWorkingTree(String repositoryPath, String branchName)
			throws IOException, GitAPIException {
		log.info("Get working tree start!!!~~");
		JSONObject returnObj = new JSONObject();
		// new FileRepositoryBuilder
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		// check repositoryPath
		validateString(repositoryPath);
		// check branchName
		validateString(branchName);
		File folder = new File(repositoryPath);
		// check folder is existed or not
		validatePathExisted(folder);
		// check git is existed or not
		validateGitExisted(folder);

		try {
			Repository repository = builder.setWorkTree(folder).build();
			Git git = new Git(repository);
			// check branch is existed or not
			int count = git.branchList().call().size();
			if (count != 0) {
				git.checkout().setName(branchName).call();
				File repoFiles = new File(repositoryPath);
				JSONObject obj = resolveTree(repoFiles);
				 JSONArray j = (JSONArray) obj.get(repoFiles.getName());
				//JSONArray j = (JSONArray) obj.get("children");
				returnObj.put(repoFiles.getName(), j);
				// treeObj.put(repoFiles.getName(), treeArray);
				// FileWriter fw = new FileWriter(
				// "C:\\Users\\william.lan\\Desktop\\test.json");
				// BufferedWriter bw = new BufferedWriter(fw);
				// bw.write(returnObj.toJSONString());
				// bw.close();
			}
			log.info("Get working tree complete!!!~~");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return returnObj;
		}
	}

	/**
	 * Disconnect git
	 * 
	 * @param repositoryPath
	 *            - Repository Path
	 * @throws IOException
	 * @throws GitAPIException
	 * 
	 */
	// public void disconnectGit(String repositoryPath,String dirPath) throws
	// IOException,
	// GitAPIException {
	// log.info("Disconnect git start!!!~~");
	// // new FileRepositoryBuilder
	// FileRepositoryBuilder builder = new FileRepositoryBuilder();
	// // check repositoryPath
	// validateString(repositoryPath);
	// File folder = new File(repositoryPath);
	// // check folder is existed or not
	// validatePathExisted(folder);
	// // check git is existed or not
	// validateGitExisted(folder);
	//
	// try {
	// Repository repository = builder.setWorkTree(folder).build();
	// Git git = new Git(repository);
	// TransportConfigCallback config = new TransportConfigCallback() {
	// @Override
	// public void configure(Transport transport) {
	// if (transport instanceof SshTransport) {
	// ((SshTransport) transport)
	// .setSshSessionFactory(new JschConfigSessionFactory() {
	// @Override
	// protected void configure(Host host,
	// Session session) {
	// session.disconnect();
	// }
	// });
	// }
	// }
	// };
	//
	// git.lsRemote().setTransportConfigCallback(config);
	// log.info("Disconnect git complete!!!~~");
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw e;
	// }
	// }

	/**
	 * 
	 */
	private JSONObject resolveTree(File file) {

		if (file.isDirectory()) {
			if (!file.getName().equals(".git")) {
				// first time
				if (treeIndex == 0) {
					temp = new JSONArray();
					treeObj.put(file.getName(), temp);
					// treeObj.put("name", file.getName());
					// treeObj.put("children", temp);
					treeIndex++;
				} else {
					JSONArray inner = (JSONArray) treeObj.get(file
							.getParentFile().getName());
					// JSONArray inner = (JSONArray) treeObj.get("children");
					folder = new JSONObject();
					JSONArray test = new JSONArray();
					folder.put(file.getName(), test);
//					folder.put("name", file.getName());
//					folder.put("children", test);
					inner.add(folder);
					treeObj.put(file.getName(), test);
					//treeObj.put("children", inner);
					//System.out.println("---->>" + treeObj.toJSONString());

				}
				for (File f : file.listFiles()) {
					resolveTree(f);
				}
			}

		} else {
			JSONArray inner = (JSONArray) treeObj.get(file.getParentFile()
					.getName());

			inner.add(file.getName());
			// folder.put(file.getParentFile().getName(), inner);
			treeObj.put(file.getParentFile().getName(), inner);

			// System.out.println("contain~~~>>" + file.getName());
		}

		return treeObj;
	}

	/**
	 * Get git tree
	 * 
	 * @param repository
	 *            - Git repository
	 * @param commitId
	 *            - commit id
	 * @return CanonicalTreeParser
	 */
	private CanonicalTreeParser getGitTree(Repository repository,
			String commitId) {
		CanonicalTreeParser oldTreeIter = null;
		try {
			RevWalk walk = new RevWalk(repository);

			// Setting old commit id
			RevCommit commit = walk.parseCommit(ObjectId.fromString(commitId));
			RevTree tree = walk.parseTree(commit.getTree().getId());
			ObjectReader reader = repository.newObjectReader();
			oldTreeIter = new CanonicalTreeParser();
			oldTreeIter.reset(reader, tree);
		} catch (MissingObjectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IncorrectObjectTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} finally {
			return oldTreeIter;
		}

	}

	/**
	 * Validate String
	 */
	private void validateString(String str) {
		if (StringUtils.isBlank(str)) {
			throw new NullPointerException();
		}
	}

	/**
	 * Write diff log into file By Diff
	 */
	private void writeToFile(List<String> logList, String old, String nw) {
		try {
			String dir = System.getProperty("user.home");
			File logFile = new File(dir + "\\" + nw + "_diff_log.txt");
			log.info("The file path~~~>>" + logFile.getCanonicalPath());
			FileWriter fw = new FileWriter(logFile);
			BufferedWriter bw = new BufferedWriter(fw);
			// Write title
			bw.write("---------------------------------------------------------------------------------\n");
			bw.write(nw + " is different from " + old);
			bw.write("\n---------------------------------------------------------------------------------\n");
			for (String str : logList) {
				bw.write(str);
				bw.write("\n");
			}
			bw.write("---------------------------------------------------------------------------------\n");
			bw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write commit log into file
	 */
	private void writeCommitLogToFile(List<String> logList, String branchName) {
		String dir = System.getProperty("user.home");
		File logFile = new File(dir + "\\" + branchName + "_commit_log.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(logFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(branchName + " commit log");
			// Write title
			bw.write("\n---------------------------------------------------------------------------------\n");
			for (String str : logList) {
				bw.write(str);
				bw.write("\n");
			}
			bw.write("---------------------------------------------------------------------------------\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Write status into file
	 */
	private void writeStatusToFile(List<String> logList, String folderName) {
		String dir = System.getProperty("user.home");
		File logFile = new File(dir + "\\" + folderName + "_status.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(logFile);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(folderName + " status log");
			// Write title
			bw.write("\n---------------------------------------------------------------------------------\n");
			for (String str : logList) {
				bw.write(str);
				bw.write("\n");
			}
			bw.write("---------------------------------------------------------------------------------\n");
			bw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Validate Commit id is existed or not
	 */
	private void validateCommitId(String str) {
		if (StringUtils.isBlank(str)) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Validate Git is existed or not
	 * 
	 * @throws FileNotFoundException
	 */
	private void validateGitExisted(File folder) throws FileNotFoundException {
		FileRepositoryBuilder builder = new FileRepositoryBuilder();
		FileRepositoryBuilder existGit = builder.findGitDir(folder);
		if (existGit.getGitDir() == null) {
			throw new FileNotFoundException();
		}

	}

	/**
	 * Validate path is existed or not
	 * 
	 * @throws FileNotFoundException
	 */
	private void validatePathExisted(File folder) throws FileNotFoundException {
		if (!folder.exists()) {
			throw new FileNotFoundException();
		}
	}

}
