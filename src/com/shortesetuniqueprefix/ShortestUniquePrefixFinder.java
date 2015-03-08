package com.shortesetuniqueprefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import com.util.*;

public class ShortestUniquePrefixFinder {
	private String absOutputFolderName;
	
	public ShortestUniquePrefixFinder(String absOutputFolderName) {
		this.absOutputFolderName = absOutputFolderName;
	}
	
	/* Step 1: combine two source files and divide data into several folders/files
	   Step 2: calculate shortest unique prefix recursively
	*/
	public int find(String absSampleFileName, String absExprFileName, boolean isOutputOverride) throws IOException{
		if (absOutputFolderName == null || absOutputFolderName.isEmpty()) {
			throw new IOException("absOutputFolderName is null or empty.");
		}
		
		File absOutputFolder = new File(absOutputFolderName);
		
		if (absOutputFolder.exists()) {
			if (isOutputOverride) {
				//TODO
			}
		}
		if (!absOutputFolder.isDirectory()) {
			throw new IOException("absOutputFolderName is not folder.");
		}
		
		if (absSampleFileName == null || absExprFileName == null ||
			absSampleFileName.isEmpty() || absExprFileName.isEmpty()) {
			throw new IOException("sampleFileName or exprFileName is null or empty.");
		}
		
		File sampleFile = new File(absSampleFileName);
		File exprFile = new File(absExprFileName);
		
		BufferedReader sampleFileReader = new BufferedReader(new FileReader(sampleFile));
		BufferedReader exprFileReader = new BufferedReader(new FileReader(exprFile));
		// combine and divide
		Map.Entry<File, Integer> result = combineAndDivide(sampleFileReader, exprFileReader, 3);
		return result.getValue();
	}
	
	
	private Map.Entry<File, Integer> combineAndDivide(BufferedReader sampleFileReader, 
			BufferedReader exprFileReader, int levelForDivide) {
		for (int i = 1; i <= Constant.TOTAL_PAGES_COUNT; i++) {	
			//String
		}
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		ShortestUniquePrefixFinder supf = new ShortestUniquePrefixFinder("c:\\rootFolder");
		System.out.println(supf.find("", "", true));
	}
}
