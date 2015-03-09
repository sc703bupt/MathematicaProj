package com.rawdataprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.util.Constant;

public class SpecialSymbolReplacer {
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
			exprItem = replaceSpecialSymbol(exprItem);
			replacedExprFileWriter.write(exprItem + "\n");
		}
		exprFileBufferedReader.close();
		replacedExprFileWriter.close();
		return replaceCountMap;
	}
	
	public static String replaceSpecialSymbol(String str) {
		str = str.replaceAll("&lt;","<");
		str = str.replaceAll("&gt;",">");
		str = str.replaceAll("&amp;","&");
		str = str.replaceAll("&nbsp;"," ");
		str = str.replaceAll("&quot;","\"");
		return str;
	}
	
	public static void main(String[] args) throws IOException {
		SpecialSymbolReplacer ssp = new SpecialSymbolReplacer();
		ssp.replace();
	}
}
