package com.shortesetuniqueprefix;

import java.io.File;
import java.io.IOException;

public class ShortestUniquePrefixFinder {
	/* Step 1: combine two source files and divide data into several folders/files
	   Step 2: calculate shortest unique prefix recursively
	*/
	public int find(String sampleFileName, String exprFileName) throws IOException{
		if (sampleFileName == null || exprFileName == null) {
			throw new IOException("sampleFileName or exprFileName is null.");
		}
		File sampleFile = new File(sampleFileName);
		File exprFile = new File(exprFileName);
		
		// combine and divide
		
		return 0;
	}
	
	private File combineAndDivde() {
		return null;
	}
	
	public static void main(String[] args) {
		
	}

}
