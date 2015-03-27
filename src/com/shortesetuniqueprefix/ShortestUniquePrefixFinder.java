package com.shortesetuniqueprefix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.util.*;

public class ShortestUniquePrefixFinder {
	public Map<String, String> indexToSUPMap = new HashMap<String, String>();
	
	public final void getSUPAndOutput() throws Exception {
		Map<String, String> indexToSUPMap = divideAndFind();
		// TODO(shenchen): integrate and output
		for (String index : indexToSUPMap.keySet()) {
			System.out.println(index + "->" + indexToSUPMap.get(index));
		}
	}
	
	private Map<String, String> divideAndFind() throws Exception {
		// create workspace
		File fileRoot = new File(Constant.DIVIDE_SAVE_PATH_PREFIX + "FileRoot\\");
		if (fileRoot.exists()) {
			Util.delFolder(fileRoot.getAbsolutePath());
		}
		fileRoot.mkdir();
		
		// copy source file to workspace and rename
		String initFilePath = Constant.DIVIDE_SAVE_PATH_PREFIX + 
				"FileRoot\\c" + Integer.valueOf(Constant.SUP_INIT_FILE_NAME);
		Util.copyFile(Constant.SOURCE_FOR_DIVIDE_PATH, initFilePath);
		
		// clean result map
		indexToSUPMap.clear();
		
		// divide recursively
		doDivideAndFind(initFilePath, 0);
		
		return indexToSUPMap;
	}
	
	// divide and fulfill indexToSUPMap 
	// use "c + number" of items as the name of temperature file
	private boolean doDivideAndFind(String absSourceForDividePath, int level) throws Exception {
		File sourceFile = new File(absSourceForDividePath);
		String itemCountStr = sourceFile.getName().substring(1); // get rid of 'c'
		Integer itemCount = new Integer(itemCountStr);
		/* base case: 
		   if the path is too long or the number of items is less enough, 
		   stop dividing and calculate USP in memory
		*/
		if (sourceFile.getAbsolutePath().length() >= Constant.FILE_PATH_LIMIT ||
			itemCount <= Constant.SINGLE_FILE_ITEM_COUNT_VALVE) {
			return findInMemory(sourceFile, level);
		}
		
		/* recursion case:
		   step 1: find parent path
		   step 2: create sub folders(with single file) according number at level position 
		   step 3: rename all single files and call doDivide respectively
		*/
		// this map is used for storing FileWriter object
		Map<String, FileWriter> fileWriterObjMap = new HashMap<String, FileWriter>();
		
		// this map is used for storing prefix number
		Map<FileWriter, String>  prefixNumberMap = new HashMap<FileWriter, String>();
		
		// this map is used for storing File object
		Map<FileWriter, File> fileObjMap = new HashMap<FileWriter, File>();
		
		// this map is used for rename
		Map<FileWriter, Integer> itemCountMap = new HashMap<FileWriter, Integer>();
		
		// step 1
		String absParentPath = sourceFile.getParent() + "\\";
		// step 2
		BufferedReader sourceFileBufferedReader = new BufferedReader(new FileReader(sourceFile));
		String sourceItem = null;
		while((sourceItem = sourceFileBufferedReader.readLine()) != null) {
			String content = Util.getContentFromItem(sourceItem);
			String[] numberArray = content.split(", ");
			int i = 0;
			for (String number : numberArray) {
				number = number.trim();
				if (i >= level && Util.isNumber(number)) {
					File targetFolder = new File(absParentPath + number);
					if (!targetFolder.exists() || !targetFolder.isDirectory()) {
						targetFolder.mkdirs();
						File targetFile = new File(absParentPath + number + "\\temp");
						targetFile.createNewFile();
						FileWriter targetFileWriter = new FileWriter(targetFile);
						fileWriterObjMap.put(number, targetFileWriter);
						prefixNumberMap.put(targetFileWriter, number);
						fileObjMap.put(targetFileWriter, targetFile);
						itemCountMap.put(targetFileWriter, 0);
					}
					
					FileWriter targetFileWriter = fileWriterObjMap.get(number);	
					targetFileWriter.write(sourceItem + "\n");
					itemCountMap.put(targetFileWriter, itemCountMap.get(targetFileWriter) + 1);
					break;
				}
				i++;
			}
		}
		
		sourceFileBufferedReader.close();
		// delete the file 'cause it will never be used
		sourceFile.delete();
		
		// step 3
		for (FileWriter fw : fileWriterObjMap.values()) {
			fw.close();
			String renameFileName = fileObjMap.get(fw).getParent() + "\\c" + itemCountMap.get(fw);
			File renameFile = new File(renameFileName);
			fileObjMap.get(fw).renameTo(renameFile);
			boolean isSuccess = doDivideAndFind(renameFile.getAbsolutePath(), level + 1);
			if (!isSuccess) {
				return false;
			}
		}
		return true;
	}
	
	// level means the length of common prefix currently
	private boolean findInMemory(File sourceFile, int level) throws Exception {
		// prepare data
		Map<String, List<BigInteger>> progressionMap = new HashMap<String, List<BigInteger>>();
		BufferedReader sourceFileBufferedReader = new BufferedReader(new FileReader(sourceFile));
		String sourceItem = null;
		while((sourceItem = sourceFileBufferedReader.readLine()) != null) {
			String content = Util.getContentFromItem(sourceItem);
			String[] numberArray = content.split(", ");
			List<BigInteger> progressionList = new ArrayList<BigInteger>();
			for (String number : numberArray) {
				number = number.trim();
				if (!Util.isNumber(number)) {
					continue;
				}
				progressionList.add(new BigInteger(number));
			}
			String index = Util.getIndexFromItem(sourceItem);
			progressionMap.put(index, progressionList);
		}
		sourceFileBufferedReader.close();
		
		// generate initial node
		Set<String> indexList = progressionMap.keySet();
		Node initNode = new Node(indexList, level, null); 
		
		doFindInMemory(initNode, progressionMap);
		return true;
	}
	
	private boolean doFindInMemory(Node node, Map<String, List<BigInteger>> progressionMap) {
		// base case: only one index in the set, fulfill the indexToSUPMap
		if (node.indexSet.size() == 1) {
			for (String index : node.indexSet) {
				// find SUP string
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i <= node.level - 1; i++) {
					sb.append(progressionMap.get(index).get(i));
					if (i != node.level - 1) {
						sb.append(',');
					}
				}
				indexToSUPMap.put(index, sb.toString());
			}
			return true;
		}
		
		/* recursion case:
		   divide data into different node and call doFindInMemory respectively
		*/
		Map<BigInteger, Node> nodeMap = new HashMap<BigInteger, Node>();
		for (String index : node.indexSet) {
			List<BigInteger> progressionList = progressionMap.get(index);
			// if progression is not enough long, use special symbol for marking it and continue
			if (node.level > progressionList.size()-1) {
				indexToSUPMap.put(index, "length > " + Integer.valueOf(node.level));
				continue;
			}
			BigInteger divideNorm = progressionMap.get(index).get(node.level);
			Node targetNode = nodeMap.get(divideNorm);
			if (targetNode == null) {
				Set<String> indexSet = new HashSet<String>();
				indexSet.add(index);
				Node newNode = new Node(indexSet, node.level + 1, divideNorm);
				nodeMap.put(divideNorm, newNode);
			} else {
				targetNode.indexSet.add(index);
			}
		}
		
		for (Node n : nodeMap.values()) {
			boolean isSuccess = doFindInMemory(n, progressionMap);
			if (!isSuccess) {
				return false;
			}
		}
		return true;
	}
	
	public static void main(String[] args) throws Exception {
		ShortestUniquePrefixFinder supf = new ShortestUniquePrefixFinder();
		supf.getSUPAndOutput();
	}
	
	class Node {
		public Node(Set<String> indexSet, int level, BigInteger number) {
			this.indexSet = indexSet;
			this.level = level;
			this.number = number;
		}
		Set<String> indexSet;
		int level;
		BigInteger number; // norm we used to divide data
	}
}