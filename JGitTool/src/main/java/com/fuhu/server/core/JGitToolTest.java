package com.fuhu.server.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.json.simple.JSONObject;

public class JGitToolTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JGitTool jGit = new JGitTool();
		try {
//			jGit.createGit("C:\\Users\\william.lan\\Git\\Test");
//
//			jGit.cloneGit("C:\\Users\\william.lan\\Git\\Test", "C:\\Users\\william.lan\\Git\\Test2");
//			File file = new File("C:\\Users\\william.lan\\Git\\Test2\\test.txt");
//			file.createNewFile();
//			jGit.addToGit("C:\\Users\\william.lan\\Git\\Test2", "test.txt");
//			jGit.commitToGit("C:\\Users\\william.lan\\Git\\Test2", "master", "add test.txt");
			//jGit.push("C:\\Users\\william.lan\\Git\\Test2","master", "S0355125");
		    JSONObject obj = jGit.getWorkingTree("C:\\Users\\william.lan\\Git\\william", "master");
		    System.out.println(obj.toJSONString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (GitAPIException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
 catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
