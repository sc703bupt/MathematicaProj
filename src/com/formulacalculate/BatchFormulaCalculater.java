package com.formulacalculate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.config.Config;
import com.pipeline.Pipeline;
import com.util.Util;

public class BatchFormulaCalculater extends Thread{
	private int startID;
	private int endID;
	private List<Integer> skipIDList;
	private Semaphore semp;
	
	public BatchFormulaCalculater(int startID, int endID, List<Integer> skipIDList, Semaphore semp) {
		this.startID = startID;
		this.endID = endID;
		this.skipIDList = skipIDList;
		this.semp = semp;
	}
	
	public void run() {
		try {
			semp.acquire();
			batchCalculate();
			// console log
			System.out.println("BatchFormulaCalculator[" + startID + ", " + endID +"]: task done.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			semp.release();
		}
	}
	
	public void batchCalculate() throws Exception {
		long startTime = System.currentTimeMillis(); // get start time

		File sampleFile = new File(Config.getAttri("SAMPLE_FILE_PATH"));
		File exprFile = new File(Config.getAttri("EXPRESSION_FILE_PATH"));
		BufferedReader sampleFileBufferedReader = new BufferedReader(new FileReader(sampleFile));
		BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
		
		// override by default
		File statLogFile = new File(Config.getAttri("FORMULA_STATICATICS_LOG_PREFIX") + 
				"_" + new Integer(startID) + "_" + new Integer(endID));
		if (statLogFile.exists()) {
			statLogFile.delete();
		}
		statLogFile.createNewFile();
		FileWriter statLogFileWriter = new FileWriter(statLogFile);
		
		// override by default
		File formulaCalculatedFile = new File(Config.getAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX") + 
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
			// console log
			if ((sampleItemID - startID) % ((endID - startID + 1)/10) == 0) {
				String percetage = (sampleItemID - startID) / ((endID - startID + 1)/10) + "0%";
				System.out.println("BatchFormulaCalculator[" + startID + ", " + endID +"]: " + percetage + " done.");
			}
			
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
				// collecting expressions
				List<String> exprList = new ArrayList<String>();
				exprList.add(Util.getContentFromItem(exprItem));
				while ((exprItem = exprFileBufferedReader.readLine()) != null) {
					int newExprItemID = Util.getIDFromIndex(Util.getIndexFromItem(exprItem));
					if (newExprItemID != sampleItemID) {
						break;
					}
					exprList.add(Util.getContentFromItem(exprItem));
				}
				
				// if the last number in sample is bigger than PROGRESSION_MAX_VALUE, use sample itself
				String cuttedSample = Util.progressionCutter(sampleItem);
				String calculatedResult = null;
				if (cuttedSample.equals(sampleItem)) {
					 calculatedResult = fc.calculateToString(exprList, Util.getContentFromItem(sampleItem)
							, Util.getIndexFromItem(sampleItem), statLogFileWriter); // get result calculated by formula
				} else {
					 calculatedResult = Util.getContentFromItem(sampleItem);
				}
				
				// if calculate fail, use sample instead
				if (calculatedResult == null) {
					calculatedResult = Util.getContentFromItem(sampleItem);
					failedCalculateItemCount++;
				}
				
				// cut and write
				String cuttedResult = Util.progressionCutter(Util.getIndexFromItem(sampleItem) + ":" + calculatedResult);
				formulaCalculatedFileWriter.write(cuttedResult + "\n");
				formulaCalculatedFileWriter.flush();
				Pipeline.checkSet.add(sampleItemID);
				lastWrittenSampleID = sampleItemID;
			} else if (sampleItemID > exprItemID) {
				exprItem = exprFileBufferedReader.readLine();
			} else {
				if (lastWrittenSampleID != sampleItemID) {
					lackOfFormulaCount++;
					statLogFileWriter.write("[LACK]:" + Util.getIndexFromItem(sampleItem) + "\n");
					String cuttedSample = Util.progressionCutter(sampleItem);
					formulaCalculatedFileWriter.write(cuttedSample + "\n");// find no expr, use sample itself	
					formulaCalculatedFileWriter.flush();
					Pipeline.checkSet.add(sampleItemID);
					lastWrittenSampleID = sampleItemID;
				}	
				sampleItem = sampleFileBufferedReader.readLine();
			}
			
			if (sampleItem != null) {
				sampleItemID = Util.getIDFromIndex(Util.getIndexFromItem(sampleItem));
			}
			
			if(exprItem != null) {
				exprItemID = Util.getIDFromIndex(Util.getIndexFromItem(exprItem));
			}
		}
		
		// handle superfluous sample item, write them directly to file
		while(sampleItem != null && sampleItemID <= endID) {
			if (lastWrittenSampleID != sampleItemID) {
				String cuttedSample = Util.progressionCutter(sampleItem);
				formulaCalculatedFileWriter.write(cuttedSample + "\n");
				formulaCalculatedFileWriter.flush();
				Pipeline.checkSet.add(sampleItemID);
				lastWrittenSampleID = sampleItemID;
			}
			sampleItem = sampleFileBufferedReader.readLine();
			if (sampleItem != null) {
				sampleItemID = Util.getIDFromIndex(Util.getIndexFromItem(sampleItem));
			}
		}
		
		long endTime = System.currentTimeMillis(); // get end time
		
		statLogFileWriter.write("Total all formula failed item count:" + failedCalculateItemCount + "\n");
		statLogFileWriter.write("Total lack of formula item count:" + lackOfFormulaCount + "\n");
		statLogFileWriter.write("Total skip item count:" + skipCount + "\n");
		statLogFileWriter.write("Total time used:" + (endTime-startTime)/1000 + "s\n");
		
		// close
		fc.close();
		sampleFileBufferedReader.close();
		exprFileBufferedReader.close();
		formulaCalculatedFileWriter.close();
		statLogFileWriter.close();
	}
}
