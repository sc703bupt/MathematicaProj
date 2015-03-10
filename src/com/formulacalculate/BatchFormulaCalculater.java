package com.formulacalculate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.util.Constant;
import com.util.Util;

public class BatchFormulaCalculater extends Thread{
	private int startID;
	private int endID;
	List<Integer> skipIDList;
	
	public BatchFormulaCalculater(int startID, int endID, List<Integer> skipIDList) {
		this.startID = startID < 1 ? 1: startID;
		this.endID = endID > Constant.TOTAL_PAGES_COUNT ? Constant.TOTAL_PAGES_COUNT : endID;
		this.skipIDList = skipIDList;
	}
	
	public void run() {
		try {
			batchCalculate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void batchCalculate() throws Exception {
		File sampleFile = new File(Constant.SAMPLE_FILE_PATH);
		File exprFile = new File(Constant.REPLACED_EXPRESSION_FILE_PATH);
		BufferedReader sampleFileBufferedReader = new BufferedReader(new FileReader(sampleFile));
		BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
		
		// override by default
		File formulaCalculatedFile = new File(Constant.FORMULA_CALCULATED_SAVE_PATH_PREFIX + 
				"_" + new Integer(startID) + "_" + new Integer(endID));
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
		int failedCalculateItemCount = 0;
		int lackOfFormulaCount = 0;
		int lastWrittenSampleID = -1;
		int sampleItemID = Util.getIDFromIndex(sampleItem.substring(0, Constant.INDEX_WIDTH + 1));
		int exprItemID = Util.getIDFromIndex(exprItem.substring(0, Constant.INDEX_WIDTH + 1));
		while(sampleItem != null && exprItem != null && sampleItemID <= endID && exprItemID <= endID) {
			// skip if the id in skipIDList
			if (skipIDList.contains(new Integer(sampleItemID))) {
				if (lastWrittenSampleID != sampleItemID) {
					formulaCalculatedFileWriter.write(sampleItem + "\n");// find no expr, use sample itself	
					lastWrittenSampleID = sampleItemID;
				}	
				sampleItem = sampleFileBufferedReader.readLine();
				sampleItemID = Util.getIDFromIndex(sampleItem.substring(0, Constant.INDEX_WIDTH + 1));
				continue;
			}
			
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
				if (calculatedResult == null) {
					failedCalculateItemCount++;
				}
				formulaCalculatedFileWriter.write(sampleItem.substring(0, Constant.INDEX_WIDTH + 1) + ":" + calculatedResult + "\n");
				lastWrittenSampleID = sampleItemID;
			} else if (sampleItemID > exprItemID) {
				exprItem = exprFileBufferedReader.readLine();
			} else {
				if (lastWrittenSampleID != sampleItemID) {
					lackOfFormulaCount++;
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
		
		System.out.println("Total failed item count:" + failedCalculateItemCount + 
				" ratio:" + (1.0 * failedCalculateItemCount) / (endID - startID + 1));
		System.out.println("Total lack of formula count:" + lackOfFormulaCount + 
				" ratio:" + (1.0 * lackOfFormulaCount) / (endID - startID + 1));
	}
	
	public static void main(String[] args) throws Exception {
		List<Integer> skipIDList = 
			Arrays.asList(new Integer[]{94, 238, 341, 534});
		// 50 : running costs too much time
		// 94 : web page parse error
		// 238 : running costs too much time
		// 341 : running costs too much time
		// 534 : running costs too much time
		BatchFormulaCalculater bfc1 = new BatchFormulaCalculater(49, 51, skipIDList); 
		//BatchFormulaCalculater bfc2 = new BatchFormulaCalculater(1001, 2000, skipIDList);
		//BatchFormulaCalculater bfc3 = new BatchFormulaCalculater(2001, 3000, skipIDList);
		//BatchFormulaCalculater bfc4 = new BatchFormulaCalculater(3001, 4000, skipIDList);
		//BatchFormulaCalculater bfc5 = new BatchFormulaCalculater(4001, 5000, skipIDList);
		bfc1.start();
		//bfc2.start();
		//bfc3.start();
		//bfc4.start();
		//bfc5.start();
	}
}
