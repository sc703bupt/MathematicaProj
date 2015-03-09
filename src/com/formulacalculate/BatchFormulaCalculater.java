package com.formulacalculate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import com.util.Constant;
import com.util.Util;

public class BatchFormulaCalculater {
	private int startID;
	private int endID;
	
	public BatchFormulaCalculater(int startID, int endID) {
		this.startID = startID < 1 ? 1: startID;
		this.endID = endID > Constant.TOTAL_PAGES_COUNT ? Constant.TOTAL_PAGES_COUNT : endID;
	}
	
	public void batchCalculate() throws Exception {
		File sampleFile = new File(Constant.SAMPLE_FILE_PATH);
		File exprFile = new File(Constant.REPLACED_EXPRESSION_FILE_PATH);
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
		
		// find the first one exceed startID in sample file
		String sampleItem = null;
		while ((sampleItem = sampleFileBufferedReader.readLine()) != null) {
			if (Util.getIDFromIndex(sampleItem.substring(0, Constant.INDEX_WIDTH + 1)) >= startID) {
				break;
			}
		}

		// find the first one exceed startID in expression file
		String exprItem = null;
		while ((exprItem = exprFileBufferedReader.readLine()) != null) {
			if (Util.getIDFromIndex(exprItem.substring(0, Constant.INDEX_WIDTH + 1)) >= startID) {
				break;
			}
		}

		// 2-way merge
		int lastWrittenSampleID = -1;
		int sampleItemID = Util.getIDFromIndex(sampleItem.substring(0, Constant.INDEX_WIDTH + 1));
		int exprItemID = Util.getIDFromIndex(exprItem.substring(0, Constant.INDEX_WIDTH + 1));
		while(sampleItem != null && exprItem != null && sampleItemID <= endID && exprItemID <= endID) {
			if (sampleItemID == exprItemID) {
				List<String> exprList = new ArrayList<String>();
				exprList.add(exprItem.substring(Constant.INDEX_WIDTH + 2));
				while ((exprItem = exprFileBufferedReader.readLine()) != null) {
					int newExprItemID = Util.getIDFromIndex(exprItem.substring(0, Constant.INDEX_WIDTH + 1));
					if (newExprItemID != sampleItemID) {
						break;
					}
					exprList.add(exprItem.substring(Constant.INDEX_WIDTH + 2));
				}
				String calculatedResult = fc.calculateToString(exprList, sampleItem.substring(Constant.INDEX_WIDTH + 2)
						, sampleItem.substring(0 , Constant.INDEX_WIDTH + 1)); // get result calculated by formula
				formulaCalculatedFileWriter.write(sampleItem.substring(0, Constant.INDEX_WIDTH + 1) + ":" + calculatedResult + "\n");
				lastWrittenSampleID = sampleItemID;
			} else if (sampleItemID > exprItemID) {
				exprItem = exprFileBufferedReader.readLine();
			} else {
				if (lastWrittenSampleID != sampleItemID) {
					formulaCalculatedFileWriter.write(sampleItem + "\n");// find no expr, use sample itself	
					lastWrittenSampleID = sampleItemID;
				}	
				sampleItem = sampleFileBufferedReader.readLine();
			}
			sampleItemID = Util.getIDFromIndex(sampleItem.substring(0, Constant.INDEX_WIDTH + 1));
			exprItemID = Util.getIDFromIndex(exprItem.substring(0, Constant.INDEX_WIDTH + 1));
		}
		
		// close
		sampleFileBufferedReader.close();
		exprFileBufferedReader.close();
		formulaCalculatedFileWriter.close();
	}
	
	public static void main(String[] args) throws Exception {
		BatchFormulaCalculater bfc = new BatchFormulaCalculater(1, 20); 
		bfc.batchCalculate();
	}
}
