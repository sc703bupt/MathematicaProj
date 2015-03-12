package com.rawdataprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.util.Constant;
import com.util.Util;

public class SymbolReplacer {
	public Map<String, Integer> replace() throws IOException {
		Map<String, Integer> replaceCountMap = new HashMap<String, Integer>();
		File exprFile = new File(Constant.EXPRESSION_FILE_PATH);
		File replacedExprFile = new File(Constant.REPLACED_EXPRESSION_FILE_PATH);
		BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
		if (replacedExprFile.exists()) {
			replacedExprFile.delete();
		}
		replacedExprFile.createNewFile();
		FileWriter replacedExprFileWriter = new FileWriter(replacedExprFile);
		String exprItem = null;
		while ((exprItem = exprFileBufferedReader.readLine()) != null) {
			exprItem = replaceSymbolForOneItem(exprItem);
			exprItem = replaceDataRangeForOneItem(exprItem);
			replacedExprFileWriter.write(exprItem + "\n");
		}
		exprFileBufferedReader.close();
		replacedExprFileWriter.close();
		return replaceCountMap;
	}
	
	public static String replaceSymbolForOneItem(String exprItem) {
		exprItem = exprItem.replaceAll("&lt;","<");
		exprItem = exprItem.replaceAll("&gt;",">");
		exprItem = exprItem.replaceAll("&amp;","&");
		exprItem = exprItem.replaceAll("&nbsp;"," ");
		exprItem = exprItem.replaceAll("&quot;","\"");
		return exprItem;
	}
	
	public static String replaceDataRangeForOneItem(String exprItem) {
		String replacedExprItem = ruleOne(exprItem);
		// if rule one not work, try rule two
		if (replacedExprItem.equals(exprItem)) {
			replacedExprItem = ruleTwo(exprItem);
		}
		return replacedExprItem;
	} 
	
	// Range[100] -> Range[Constant.PROGRESSION_LENGTH]
	private static String ruleOne(String exprItem) {
		int startPos = 0;
		int symbolPos;
		do{
			symbolPos = exprItem.indexOf("Range", startPos);
			if(symbolPos == -1) {
				break;
			}
			int leftSquareBracketsPos = 
				symbolPos + 5 <= exprItem.length() - 1 && exprItem.charAt(symbolPos + 5) == '['? symbolPos + 5 : -1;
			int rightSquareBracketsPos = exprItem.indexOf("]", symbolPos + 5);
			if (leftSquareBracketsPos != -1 && rightSquareBracketsPos != -1) {
				String paraString = exprItem.substring(leftSquareBracketsPos + 1, rightSquareBracketsPos);
				if (Util.isNumber(paraString)) {
					String frontPart = exprItem.substring(0, leftSquareBracketsPos + 1);
					String backPart = exprItem.substring(rightSquareBracketsPos);
					return frontPart + Constant.PROGRESSION_LENGTH + backPart;
				}
			}
			startPos = symbolPos + 5;
		}while(symbolPos != -1);
 		
		// no replacement, return exprItem itself
		return exprItem;
	}
	
	// {x, 0, 100} -> {x, 0, Constant.PROGRESSION_LENGTH - 1} 
	// {x, 100} -> {x, PROGRESSION_LENGTH}
	private static String ruleTwo(String exprItem) {
		return null;
	}
	
	public static void main(String[] args) throws IOException {
		//SymbolReplacer ssp = new SymbolReplacer();
		//ssp.replace();
		while(true) {
			System.out.println("Please enter expr:");
			Scanner s = new Scanner(System.in);
			String expr = s.nextLine();
			System.out.println(ruleOne(expr));
			System.out.println("--");
		}
	}
}
