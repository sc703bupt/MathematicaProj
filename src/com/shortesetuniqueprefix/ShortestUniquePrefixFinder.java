package com.shortesetuniqueprefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import com.util.*;

public class ShortestUniquePrefixFinder {
	public File divide() throws IOException {
		File fileRoot = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\");
		if (fileRoot.exists()) {
			Util.delFolder(fileRoot.getAbsolutePath());
		}
		fileRoot.mkdir();
		
		File sourceFile = new File(Constant.SOURCE_FOR_DIVIDE_PATH);
		BufferedReader sourceFileBufferedReader = new BufferedReader(new FileReader(sourceFile));
		
		String sourceItem = null;
		while((sourceItem = sourceFileBufferedReader.readLine()) != null) {
			
		}
		
		return fileRoot;
	}
	
	public int findSUP() throws IOException{
		File fileRoot = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\");
		
		if (!fileRoot.exists() || !fileRoot.isDirectory()) {
			throw new IOException("divided folder is not exist or not a folder.");
		}
		return 0;
	}
		
	public static void main(String[] args) throws IOException {
		ShortestUniquePrefixFinder supf = new ShortestUniquePrefixFinder();
		supf.divide();
		//System.out.println("The shortest unique prefix is " + supf.findSUP());
	}
}
