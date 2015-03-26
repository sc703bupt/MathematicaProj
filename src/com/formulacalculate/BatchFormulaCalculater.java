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
		System.out.println("Task for " + startID + " to " + endID +" is done.");
	}
	
	public void batchCalculate() throws Exception {
		long startTime = System.currentTimeMillis();

		File sampleFile = new File(Constant.SAMPLE_FILE_PATH);
		File exprFile = new File(Constant.REPLACED_EXPRESSION_FILE_PATH);
		BufferedReader sampleFileBufferedReader = new BufferedReader(new FileReader(sampleFile));
		BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
		
		// override by default
		File statLogFile = new File(Constant.FORMULA_STATICATICS_LOG_PREFIX + 
				"_" + new Integer(startID) + "_" + new Integer(endID));
		if (statLogFile.exists()) {
			statLogFile.delete();
		}
		statLogFile.createNewFile();
		FileWriter statLogFileWriter = new FileWriter(statLogFile);
		
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
			if (Util.getIDFromIndex(Util.getIndexFromItem(sampleItem)) >= startID) {
				break;
			}
		}

		// find the first one exceed startID in expression file
		String exprItem = null;
		while ((exprItem = exprFileBufferedReader.readLine()) != null) {
			if (Util.getIDFromIndex(Util.getIndexFromItem(exprItem)) >= startID) {
				break;
			}
		}

		// 2-way merge
		int failedCalculateItemCount = 0;
		int lackOfFormulaCount = 0;
		int skipCount = 0;
		int lastWrittenSampleID = -1;
		int sampleItemID = Util.getIDFromIndex(Util.getIndexFromItem(sampleItem));
		int exprItemID = Util.getIDFromIndex(Util.getIndexFromItem(exprItem));
		while(sampleItem != null && exprItem != null && sampleItemID <= endID && exprItemID <= endID) {
			// skip if the id in skipIDList
			if (skipIDList != null && skipIDList.contains(new Integer(sampleItemID))) {
				if (lastWrittenSampleID != sampleItemID) {
					skipCount++;
					statLogFileWriter.write("[SKIP]:" + Util.getIndexFromItem(sampleItem) + "\n");
					formulaCalculatedFileWriter.write(sampleItem + "\n");// skip calculation, use sample itself	
					lastWrittenSampleID = sampleItemID;
				}	
				sampleItem = sampleFileBufferedReader.readLine();
				sampleItemID = Util.getIDFromIndex(Util.getIndexFromItem(sampleItem));
				continue;
			}
			
			if (sampleItemID == exprItemID) {
				List<String> exprList = new ArrayList<String>();
				exprList.add(Util.getContentFromItem(exprItem));
				while ((exprItem = exprFileBufferedReader.readLine()) != null) {
					int newExprItemID = Util.getIDFromIndex(Util.getIndexFromItem(exprItem));
					if (newExprItemID != sampleItemID) {
						break;
					}
					exprList.add(Util.getContentFromItem(exprItem));
				}
				String calculatedResult = fc.calculateToString(exprList, Util.getContentFromItem(sampleItem)
						, Util.getIndexFromItem(sampleItem), statLogFileWriter); // get result calculated by formula
				if (calculatedResult == null) {
					calculatedResult = Util.getContentFromItem(sampleItem);
					failedCalculateItemCount++;
				}
				formulaCalculatedFileWriter.write(Util.getIndexFromItem(sampleItem) + ":" + calculatedResult + "\n");
				lastWrittenSampleID = sampleItemID;
			} else if (sampleItemID > exprItemID) {
				exprItem = exprFileBufferedReader.readLine();
			} else {
				if (lastWrittenSampleID != sampleItemID) {
					lackOfFormulaCount++;
					statLogFileWriter.write("[LACK]:" + Util.getIndexFromItem(sampleItem) + "\n");
					formulaCalculatedFileWriter.write(sampleItem + "\n");// find no expr, use sample itself	
					lastWrittenSampleID = sampleItemID;
				}	
				sampleItem = sampleFileBufferedReader.readLine();
			}
			sampleItemID = Util.getIDFromIndex(Util.getIndexFromItem(sampleItem));
			exprItemID = Util.getIDFromIndex(Util.getIndexFromItem(exprItem));
		}
		
		// TODO(shenchen): impl: handle superfluous sample item
		
		long endTime = System.currentTimeMillis(); //获取结束时间
		
		statLogFileWriter.write("Total all formula failed item count:" + failedCalculateItemCount + "\n");
		statLogFileWriter.write("Total lack of formula item count:" + lackOfFormulaCount + "\n");
		statLogFileWriter.write("Total skip item count:" + skipCount + "\n");
		statLogFileWriter.write("Total time used:" + (endTime-startTime)/1000 + "s\n");
		
		// close
		sampleFileBufferedReader.close();
		exprFileBufferedReader.close();
		formulaCalculatedFileWriter.close();
		statLogFileWriter.close();
	}
	
	public static void main(String[] args) throws Exception {
		List<Integer> skipIDList = 
			Arrays.asList(new Integer[]{94});
		// 50 : running costs too much time
		// 94 : web page parse error
		// 238 : running costs too much time
		// 341 : running costs too much time
		// 534 : running costs too much time
		//BatchFormulaCalculater bfc1 = new BatchFormulaCalculater(200, 300, null); 
		//BatchFormulaCalculater bfc2 = new BatchFormulaCalculater(1001, 2000, skipIDList);
		//bfc1.start();
		//bfc2.start();
		
		for (int i = 3; i <= 3; i++) {
			BatchFormulaCalculater bfc = new BatchFormulaCalculater(10000 * i + 1, 10000 * (i + 1), null);
			bfc.start();
		}
	}
}
