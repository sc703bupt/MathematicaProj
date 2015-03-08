package com.formulacalculate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.util.Constant;
import com.util.Util;

public class BatchFormulaCalculater {
	private int startID;
	private int endID;
	
	public BatchFormulaCalculater(int startID, int endID) {
		this.startID = startID < 1 ? 1: startID;
		this.endID = endID > Constant.TOTAL_PAGES_COUNT ? Constant.TOTAL_PAGES_COUNT : endID;
	}
	
	// TODO(shenchen):expr not found case impl
	public void batchCalculate() throws Exception {
		File sampleFile = new File(Constant.SAMPLE_FILE_PATH);
		File exprFile = new File(Constant.EXPRESSION_FILE_PATH);
		BufferedReader sampleFileBufferedReader = new BufferedReader(new FileReader(sampleFile));
		BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
		
		// override by default
		File formulaCalculatedFile = new File(Constant.FORMULA_CALCULATED_SAVE_PATH);
		if (formulaCalculatedFile.exists()) {
			formulaCalculatedFile.delete();
		}
		formulaCalculatedFile.createNewFile();
		FileWriter formulaCalculatedFileWriter = new FileWriter(formulaCalculatedFile);
		
		FormulaCalculater fc = new FormulaCalculater();
		
		String previousExpr = null;
		
		for (int i = startID; i <= endID; i++) {	
			String index = Util.getIndexFromID(i);
			String sample = getSampleByIndex(sampleFileBufferedReader, index);
			Map.Entry<List<String>, String> ret = getExprListByIndex(exprFileBufferedReader, index, previousExpr);
			List<String> exprList = ret.getKey();
			previousExpr = ret.getValue(); // for next iteration
			String calculatedResult = fc.calculateToString(exprList, sample); // get result calculated by formula
			formulaCalculatedFileWriter.write(index + ":" + calculatedResult);
		}
		sampleFileBufferedReader.close();
		exprFileBufferedReader.close();
		formulaCalculatedFileWriter.close();
	}
	
	// return null means EOF, while empty list means NOT FOUND
	// NOTE: we suppose the sample file contains all index we want, otherwise this method will throw exception
	private String getSampleByIndex(BufferedReader br, String index) throws Exception {
		String item = br.readLine();
		if (item == null) {
			return null;
		}
		if (!item.startsWith(index)) {
			throw new Exception(index + " is not found, please check the sample file.");
		}
		String sample = item.substring(Constant.INDEX_WIDTH + 2);
		return sample;
	}
	
	// the value in the entry is the item for next index, because we will always read next line to see
	// whether there are more expressions or not
	private Map.Entry<List<String>, String> getExprListByIndex(BufferedReader br, String index, String previousExpr) throws IOException {
		List<String> exprList = new ArrayList<String>();
		if (previousExpr != null) {
			exprList.add(previousExpr);
		}
		Map.Entry<List<String>, String> entry = null;
		String expr = null;
        while ((expr = br.readLine()) != null) {
        	if (expr.startsWith(index)) {
        		exprList.add(expr.substring(Constant.INDEX_WIDTH + 2));
        	} else {
        		entry = new AbstractMap.SimpleEntry(exprList, expr.substring(Constant.INDEX_WIDTH + 2));
        		break;
        	}
        }  
        return entry;
	}
	
	public static void main(String[] args) throws Exception {
		BatchFormulaCalculater bfc = new BatchFormulaCalculater(1, 1); 
		bfc.batchCalculate();
	}

}
