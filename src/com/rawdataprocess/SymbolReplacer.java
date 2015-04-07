package com.rawdataprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import com.config.Config;
import com.util.Util;

public class SymbolReplacer {
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
		if (replacedExprItem.equals(exprItem)) {
			replacedExprItem = ruleThree(exprItem);
		}
		return replacedExprItem;
	}
	
	// Table[...{x, 0, 100}] -> Table[...{x, 0, Constant.PROGRESSION_LENGTH - 1}]
	// Table[...{x, 100}] - > Table[...{x, 0, Constant.PROGRESSION_LENGTH - 1}]
	private static String ruleOne(String exprItem) {
		int startPos = 0;
		int symbolPos;
		do {
			symbolPos = exprItem.indexOf("Table", startPos);
			if(symbolPos == -1) {
				break;
			}
			int leftSquareBracketsPos = 
					symbolPos + 5 <= exprItem.length() - 1 && exprItem.charAt(symbolPos + 5) == '['? symbolPos + 5 : -1;
			if (leftSquareBracketsPos != -1) {
				Stack<Integer> posStk = new Stack<Integer>();
				int pos = leftSquareBracketsPos + 1;
				posStk.push(leftSquareBracketsPos);
				while (pos <= exprItem.length() - 1) {
					char c = exprItem.charAt(pos);
					if (c == '[') {
						posStk.push(pos);
					} else if (c == ']') {
						posStk.pop();
					}
					if (posStk.empty()) {
						break;
					}
					pos++;
				}
				
				// valid && found, now pos is the ']' position counterpart of the '[' 
				// right behind the "Table"
				if (posStk.empty() && pos <= exprItem.length() - 1) {
					int rightBracePos = -1, leftBracePos = -1;
					while (pos >= 0) {
						char c = exprItem.charAt(pos);
						if (c == '}') {
							rightBracePos = pos;
						} else if (c == '{') {
							leftBracePos = pos;
							break;
						}
						pos--;
					}
					
					// the positions of '{' and '}' are found
					if (rightBracePos != -1 && leftBracePos != -1) {
						String paraString = exprItem.substring(leftBracePos + 1, rightBracePos);
						String frontPart = exprItem.substring(0, leftBracePos + 1);
						String backPart = exprItem.substring(rightBracePos);
						String[] paraArray = paraString.split(",");
						if (paraArray.length == 2 && Util.isNumber(paraArray[1].trim())) {// {x, 100}
							return frontPart + paraArray[0].trim() + ", " + Config.getAttri("PROGRESSION_LENGTH") + backPart;
						} else if (paraArray.length == 3 && Util.isNumber(paraArray[1].trim())) {// {x,0,99}
							BigInteger startNumber = new BigInteger(paraArray[1].trim());
							BigInteger expectedEndNumber = startNumber.add(new BigInteger(String.valueOf(Integer.parseInt(Config.getAttri("PROGRESSION_LENGTH")) - 1)));
							return frontPart + paraArray[0].trim() + ", " + paraArray[1].trim() + ", " + expectedEndNumber + backPart;
						}
					}
				}
			}
			startPos = symbolPos + 5;
		} while(symbolPos != -1);
		
		// no replacement, return exprItem itself
		return exprItem;
	}
	
	// Series[...{x, 0, 100}] -> Table[...{x, 0, Constant.PROGRESSION_LENGTH - 1}]
	// Series[...{x, 100}] - > Table[...{x, 0, Constant.PROGRESSION_LENGTH - 1}]
	private static String ruleTwo(String exprItem) {
		int startPos = 0;
		int symbolPos;
		do {
			symbolPos = exprItem.indexOf("Series", startPos);
			if(symbolPos == -1) {
				break;
			}
			int leftSquareBracketsPos = 
					symbolPos + 5 <= exprItem.length() - 1 && exprItem.charAt(symbolPos + 5) == '['? symbolPos + 5 : -1;
			if (leftSquareBracketsPos != -1) {
				Stack<Integer> posStk = new Stack<Integer>();
				int pos = leftSquareBracketsPos + 1;
				posStk.push(leftSquareBracketsPos);
				while (pos <= exprItem.length() - 1) {
					char c = exprItem.charAt(pos);
					if (c == '[') {
						posStk.push(pos);
					} else if (c == ']') {
						posStk.pop();
					}
					if (posStk.empty()) {
						break;
					}
					pos++;
				}
				
				// valid && found, now pos is the ']' position counterpart of the '[' 
				// right behind the "Table"
				if (posStk.empty() && pos <= exprItem.length() - 1) {
					int rightBracePos = -1, leftBracePos = -1;
					while (pos >= 0) {
						char c = exprItem.charAt(pos);
						if (c == '}') {
							rightBracePos = pos;
						} else if (c == '{') {
							leftBracePos = pos;
							break;
						}
						pos--;
					}
					
					// the positions of '{' and '}' are found
					if (rightBracePos != -1 && leftBracePos != -1) {
						String paraString = exprItem.substring(leftBracePos + 1, rightBracePos);
						String frontPart = exprItem.substring(0, leftBracePos + 1);
						String backPart = exprItem.substring(rightBracePos);
						String[] paraArray = paraString.split(",");
						if (paraArray.length == 2 && Util.isNumber(paraArray[1].trim())) {// {x, 100}
							return frontPart + paraArray[0].trim() + ", " + Config.getAttri("PROGRESSION_LENGTH") + backPart;
						} else if (paraArray.length == 3 && Util.isNumber(paraArray[1].trim())) {// {x,0,99}
							BigInteger startNumber = new BigInteger(paraArray[1].trim());
							BigInteger expectedEndNumber = startNumber.add(new BigInteger(String.valueOf(Integer.parseInt(Config.getAttri("PROGRESSION_LENGTH")) - 1)));
							return frontPart + paraArray[0].trim() + ", " + paraArray[1].trim() + ", " + expectedEndNumber + backPart;
						}
					}
				}
			}
			startPos = symbolPos + 5;
		} while(symbolPos != -1);
		
		// no replacement, return exprItem itself
		return exprItem;
	}
	
	// Range[100] -> Range[Constant.PROGRESSION_LENGTH]
	// Range[0,99] or Range[0,nn] -> Range[0,Constant.PROGRESSION_LENGTH]
	private static String ruleThree(String exprItem) {
		int startPos = 0;
		int symbolPos;
		do {
			symbolPos = exprItem.indexOf("Range", startPos);
			if(symbolPos == -1) {
				break;
			}
			int leftSquareBracketsPos = 
				symbolPos + 5 <= exprItem.length() - 1 && exprItem.charAt(symbolPos + 5) == '['? symbolPos + 5 : -1;
			int rightSquareBracketsPos = exprItem.indexOf("]", symbolPos + 5);
			if (leftSquareBracketsPos != -1 && rightSquareBracketsPos != -1) {
				String paraString = exprItem.substring(leftSquareBracketsPos + 1, rightSquareBracketsPos);
				String frontPart = exprItem.substring(0, leftSquareBracketsPos + 1);
				String backPart = exprItem.substring(rightSquareBracketsPos);
				if (Util.isNumber(paraString)) { // [100]
					return frontPart + Config.getAttri("PROGRESSION_LENGTH") + backPart;
				} else { // [0,99] or [0,nn]
					String[] paraArray = paraString.split(",");
					if (paraArray.length == 2 && Util.isNumber(paraArray[0].trim())) {
						BigInteger startNumber = new BigInteger(paraArray[0].trim());
						BigInteger expectedEndNumber = startNumber.add(new BigInteger(String.valueOf(Integer.parseInt(Config.getAttri("PROGRESSION_LENGTH")) - 1)));
						return frontPart + paraArray[0].trim() + ", " + expectedEndNumber + backPart;
					}
				}
			}
			startPos = symbolPos + 5;
		}while(symbolPos != -1);
 		
		// no replacement, return exprItem itself
		return exprItem;
	}
}
