package com.pipeline;

import java.io.File;

public class Pipeline {
	public static void main(String[] args) {
		Pipeline p = new Pipeline();
		p.createDataDir();
		p.callFileFetcher();
		p.callFileParser();
		p.callBatchFormulaCalculater();
		p.callShortestUniquePrefixFinder();
	}
	
	// ensure clean and correct data space
	public void createDataDir() {
		String formulaCalculateDirPath = "data\\formulaCalculate";
		String rawDataProcessDirPath = "data\\rawDataProcess";
		String shortestUniquePrefixDirPath = "data\\shortestUniquePrefix";
		File formulaCalculateDirFile = new File(formulaCalculateDirPath);
		File rawDataProcessDirFile = new File(rawDataProcessDirPath);
		File shortestUniquePrefixDirFile = new File(shortestUniquePrefixDirPath);
		
		if (!formulaCalculateDirFile.exists()) {
			formulaCalculateDirFile.mkdirs();
		} else {
			if (formulaCalculateDirFile.isFile()) {
				formulaCalculateDirFile.delete();
				formulaCalculateDirFile.mkdirs();
			}
		}
		
		if (!rawDataProcessDirFile.exists()) {
			rawDataProcessDirFile.mkdirs();
		} else {
			if (rawDataProcessDirFile.isFile()) {
				rawDataProcessDirFile.delete();
				rawDataProcessDirFile.mkdirs();
			}
		}
		
		if (!shortestUniquePrefixDirFile.exists()) {
			shortestUniquePrefixDirFile.mkdirs();
		} else {
			if (shortestUniquePrefixDirFile.isFile()) {
				shortestUniquePrefixDirFile.delete();
				shortestUniquePrefixDirFile.mkdirs();
			}
		}
	}
	
	// TODO(shenchen):impl
	public void callFileFetcher() {
		
	}
	
	// TODO(shenchen):impl
	public void callFileParser() {
		
	}
	
	// TODO(shenchen):impl
	public void callBatchFormulaCalculater() {
		
	}
	
	// TODO(shenchen):impl
	public void callShortestUniquePrefixFinder() {
		
	}
}
