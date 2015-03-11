package com.shortesetuniqueprefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.util.*;

public class ShortestUniquePrefixFinder {
	public File divide() throws IOException {
		if (Constant.FOLDER_LEVEL_FOR_DIVIDE <= 0) {
			System.out.println("Folder level must equal or larger than 1.");
			return null;
		}
		
		File fileRoot = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\");
		if (fileRoot.exists()) {
			Util.delFolder(fileRoot.getAbsolutePath());
		}
		fileRoot.mkdir();
		
		File sourceFile = new File(Constant.SOURCE_FOR_DIVIDE_PATH);
		BufferedReader sourceFileBufferedReader = new BufferedReader(new FileReader(sourceFile));
		
		// e.g. <"1//2//3",fw1>, <"2//3//4, fw2">
		Map<String, FileWriter> writerMap = new HashMap<String, FileWriter>();
		
		Map<String, Integer> itemCountMap = new HashMap<String, Integer>();
		
		String sourceItem = null;
		while((sourceItem = sourceFileBufferedReader.readLine()) != null) {
			String content = Util.getContentFromItem(sourceItem).trim();
			String[] numbers = content.split(", ");
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i <= Constant.FOLDER_LEVEL_FOR_DIVIDE - 1; i++) {
				if (i > numbers.length - 1) continue; // test
				sb.append(numbers[i]);
				sb.append("\\");
			}
			String filePath = sb.toString();
			FileWriter targetFileWriter = writerMap.get(filePath);
			if (targetFileWriter == null) {
				File targetFolder = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\" 
						+ filePath);
				targetFolder.mkdirs();
				File targetFile = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\" 
						+ filePath + Constant.DEFAULT_LEAF_FILE);
				targetFileWriter = new FileWriter(targetFile);
				writerMap.put(filePath, targetFileWriter);
				itemCountMap.put(filePath, 0);
			} 
			targetFileWriter.write(sourceItem + "\n");
			itemCountMap.put(filePath, itemCountMap.get(filePath) + 1);
		}
		
		// close all file writers
		Collection<FileWriter> fileWriterList = writerMap.values();
		for (FileWriter w : fileWriterList) {
			w.close();
		}
		
		Collection<Integer> itemCountList = itemCountMap.values();
		int minCount = Integer.MAX_VALUE, maxCount = 0, totalCount = 0, fileCount = 0;
		for(Integer i : itemCountList) {
			fileCount++;
			minCount = minCount < i.intValue() ? minCount : i.intValue();
			maxCount = maxCount > i.intValue() ? maxCount : i.intValue();
			totalCount += i.intValue();
		}
		
		System.out.println("For folder level " + Constant.FOLDER_LEVEL_FOR_DIVIDE);
		System.out.println("min:" + minCount);
		System.out.println("max:" + maxCount);
		System.out.println("avg:" + totalCount/fileCount);
		
		sourceFileBufferedReader.close();
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
