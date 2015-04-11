package com.pipeline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import com.config.Config;
import com.formulacalculate.BatchFormulaCalculater;
import com.rawdataprocess.FileFetcher;
import com.rawdataprocess.FileParser;
import com.shortesetuniqueprefix.ShortestUniquePrefixFinder;
import com.util.Util;

public class Pipeline {
	public static Set<Integer> checkSet = Collections.synchronizedSet((new HashSet<Integer>())); 
	
	public static void main(String[] args) {
		Pipeline p = new Pipeline();
		//p.execute(1, Integer.valueOf(Config.getAttri("TARGET_END_ID")));
		int totalPageCount = Util.getTotalPageCountFromFile();
		int targetEndId = Integer.valueOf(Config.getAttri("TARGET_END_ID"));
		int i = 1;
		for (i = 1; totalPageCount + i * 1000 <= targetEndId; i++) {
			p.execute(1,  totalPageCount + i * 1000);
		}
		if (totalPageCount + (i-1) * 1000 < targetEndId) {
			p.execute(1,  targetEndId);
		}
	}
	
	public void execute(int startId, int endId) {
		initDataDir();
		int totalPageCount = Util.getTotalPageCountFromFile();
		startId = (startId <= totalPageCount) ? totalPageCount + 1 : startId;
		try {
			checkSet.clear();
			callFileFetcher(startId, endId);
			callFileParser(startId, endId);
			callBatchFormulaCalculater(startId, endId);
			//callShortestUniquePrefixFinder();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ensure clean and correct data space
	public void initDataDir() {
		String formulaCalculateDirPath = "data\\formulaCalculate";
		String rawDataProcessDirPath = "data\\rawDataProcess";
		String shortestUniquePrefixDirPath = "data\\shortestUniquePrefix";
		String webPageSavePathPrefix = Config.getAttri("WEB_PAGE_SAVE_PATH_PREFIX");
		String logSavePathPrefix = Config.getAttri("LOG_SAVE_PATH_PREFIX");
		
		File formulaCalculateDirFile = new File(formulaCalculateDirPath);
		File rawDataProcessDirFile = new File(rawDataProcessDirPath);
		File shortestUniquePrefixDirFile = new File(shortestUniquePrefixDirPath);
		File webPageSavePathPrefixDirFile = new File(webPageSavePathPrefix);
		File logSavePathPrefixDirFile = new File(logSavePathPrefix);
		
		if (!formulaCalculateDirFile.exists()) {
			formulaCalculateDirFile.mkdirs();
		} else {
			if (formulaCalculateDirFile.isFile()) {
				formulaCalculateDirFile.delete();
				formulaCalculateDirFile.mkdirs();
			}
		}
		
		if (!rawDataProcessDirFile.exists()) {
			rawDataProcessDirFile.mkdirs();
		} else {
			if (rawDataProcessDirFile.isFile()) {
				rawDataProcessDirFile.delete();
				rawDataProcessDirFile.mkdirs();
			}
		}
		
		if (!shortestUniquePrefixDirFile.exists()) {
			shortestUniquePrefixDirFile.mkdirs();
		} else {
			if (shortestUniquePrefixDirFile.isFile()) {
				shortestUniquePrefixDirFile.delete();
				shortestUniquePrefixDirFile.mkdirs();
			}
		}
		
		if (!webPageSavePathPrefixDirFile.exists()) {
			webPageSavePathPrefixDirFile.mkdirs();
		} else {
			if (webPageSavePathPrefixDirFile.isFile()) {
				webPageSavePathPrefixDirFile.delete();
				webPageSavePathPrefixDirFile.mkdirs();
			}
		}
		
		if (!logSavePathPrefixDirFile.exists()) {
			logSavePathPrefixDirFile.mkdirs();
		} else {
			if (logSavePathPrefixDirFile.isFile()) {
				logSavePathPrefixDirFile.delete();
				logSavePathPrefixDirFile.mkdirs();
			}
		}
		
		File file = new File(Config.getAttri("TOTAL_PAGES_COUNT_PATH"));
		if (!file.exists()) {
			Util.setTotalPageCount(0);
		}
	}
	
	public void callFileFetcher(int startId, int endId) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
        int totalItemCount = endId - startId + 1;
		int batchSingleThreadAbility = Integer.parseInt(Config.getAttri("SINGLE_THREAD_FETCH_PARSE_ABILITY"));
        int numberOfThread = totalItemCount / batchSingleThreadAbility;
        if (totalItemCount % batchSingleThreadAbility != 0) {
        	numberOfThread += 1;
        }
        final Semaphore semp = new Semaphore(numberOfThread);
		for (int i = 0; i <= numberOfThread - 1; i++) {
			FileFetcher ff = null;
			if (i != numberOfThread - 1) {
				ff = new FileFetcher(startId + batchSingleThreadAbility * i, startId + batchSingleThreadAbility * (i + 1) - 1, semp);
			} else {
				ff = new FileFetcher(startId + batchSingleThreadAbility * i, endId, semp);
			}
			pool.execute(ff);
		}
		
		// no continue until all batch tasks are done
		do {
			Thread.sleep(1000);
		} while (semp.availablePermits() != numberOfThread);
        pool.shutdown();
	}
	
	public void callFileParser(int startId, int endId) throws Exception {
		ExecutorService pool = Executors.newCachedThreadPool();
        int totalItemCount = endId - startId + 1;
        int batchSingleThreadAbility = Integer.parseInt(Config.getAttri("SINGLE_THREAD_FETCH_PARSE_ABILITY"));
        int numberOfThread = totalItemCount / batchSingleThreadAbility;
        if (totalItemCount % batchSingleThreadAbility != 0) {
        	numberOfThread += 1;
        }
        final Semaphore semp = new Semaphore(numberOfThread);
		for (int i = 0; i <= numberOfThread - 1; i++) {
			FileParser fp = null;
			if (i != numberOfThread - 1) {
				fp = new FileParser(startId + batchSingleThreadAbility * i, startId + batchSingleThreadAbility * (i + 1) - 1, semp);
			} else {
				fp = new FileParser(startId + batchSingleThreadAbility * i, endId, semp);
			}
			pool.execute(fp);
		}
		
		// no continue until all batch tasks are done
		do {
			Thread.sleep(1000);
		} while (semp.availablePermits() != numberOfThread);
        pool.shutdown();

        File sampleMergedFile = new File(Config.getAttri("SAMPLE_FILE_PATH"));
        FileWriter sampleMergedFileWriter = new FileWriter(sampleMergedFile, true);
        
        File exprMergedFile = new File(Config.getAttri("EXPRESSION_FILE_PATH"));
        FileWriter exprMergedFileWriter = new FileWriter(exprMergedFile, true);
        
        int totalPagesCount = Util.getTotalPageCountFromFile();
        for (int i = 0; i <= numberOfThread - 1; i++) {
        	File sampleFile = null;
        	File exprFile = null;
        	if (i != numberOfThread - 1) { 
        		sampleFile = new File(Config.getAttri("SAMPLE_FILE_PATH") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(startId + batchSingleThreadAbility * (i + 1) - 1));
        		exprFile = new File(Config.getAttri("EXPRESSION_FILE_PATH") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(startId + batchSingleThreadAbility * (i + 1) - 1));
        	} else {
        		sampleFile = new File(Config.getAttri("SAMPLE_FILE_PATH") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(endId));
        		exprFile = new File(Config.getAttri("EXPRESSION_FILE_PATH") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(endId));
        	}
        	
        	//handle sample
			BufferedReader sampleFileBufferedReader = new BufferedReader(new FileReader(sampleFile));
			String oneLine = null;
			while ((oneLine = sampleFileBufferedReader.readLine()) != null) {
				int index = Integer.parseInt(Util.getIndexFromItem(oneLine).substring(1));
				totalPagesCount = totalPagesCount > index ? totalPagesCount : index;
				sampleMergedFileWriter.write(oneLine + "\n");
			}
			sampleFileBufferedReader.close();
			sampleFile.delete();
			
			//handle expr
			BufferedReader exprFileBufferedReader = new BufferedReader(new FileReader(exprFile));
			while ((oneLine = exprFileBufferedReader.readLine()) != null) {
				exprMergedFileWriter.write(oneLine + "\n");
			}
			exprFileBufferedReader.close();
			exprFile.delete();
        }
        sampleMergedFileWriter.close();
        exprMergedFileWriter.close();
        
        // write totalPagesCount to "TOTAL_PAGES_COUNT_PATH"
        Util.setTotalPageCount(totalPagesCount);
	}
	
	public void callBatchFormulaCalculater(int startId, int endId) throws Exception {       
        int totalItemCount = endId - startId + 1;
        int batchSingleThreadAbility = Integer.parseInt(Config.getAttri("BATCH_SINGLE_THREAD_ABILITY"));
        int numberOfThread = totalItemCount / batchSingleThreadAbility;
        if (totalItemCount % batchSingleThreadAbility != 0) {
        	numberOfThread += 1;
        }
        
        ExecutorService fisrtRoundPool = Executors.newCachedThreadPool();
        final Semaphore firstRoundSemaphore = new Semaphore(numberOfThread);
		for (int i = 0; i <= numberOfThread - 1; i++) {
			BatchFormulaCalculater bfc = null;
			if (i != numberOfThread - 1) {
				bfc = new BatchFormulaCalculater(startId + batchSingleThreadAbility * i, startId + batchSingleThreadAbility * (i + 1) - 1, null, firstRoundSemaphore);
			} else {
				bfc = new BatchFormulaCalculater(startId + batchSingleThreadAbility * i, endId, null, firstRoundSemaphore);
			}
			fisrtRoundPool.execute(bfc);
		}
		
		// no continue until all batch tasks are done
		do {
			Thread.sleep(1000);
		} while (firstRoundSemaphore.availablePermits() != numberOfThread);
		fisrtRoundPool.shutdown();
        
        // check and redo
		// console log
		System.out.println("Pipeline: start check and redo.");
        ExecutorService checkRoundPool = Executors.newCachedThreadPool();
        final Semaphore checkRoundSemaphore = new Semaphore(numberOfThread);
        int round = 1;
        while (true) {
        	// console log
        	System.out.println("Pipeline: round " + round);
        	Map<String, String> fragmentToOriginalFileMap = new HashMap<String, String>();
        	boolean isTaskDone = true;
    		for (int i = 0; i <= numberOfThread - 1; i++) {
    			int lowerBound, upperBound;
    			if (i != numberOfThread - 1) {
    				lowerBound = startId + batchSingleThreadAbility * i;
    				upperBound = startId + batchSingleThreadAbility * (i + 1) - 1;
    			} else {
    				lowerBound = startId + batchSingleThreadAbility * i;
    				upperBound = endId;
    			}
    			for (int j = lowerBound; j <= upperBound; j++) {
    				if (!checkSet.contains(j)) {
    					isTaskDone = false;
    	    			// console log
    	    			System.out.println("Pipeline: start redo[" + j + ", " + upperBound +"]");
    					BatchFormulaCalculater bfc = new BatchFormulaCalculater(j, upperBound, null, checkRoundSemaphore);
    					checkRoundPool.execute(bfc);
    					String fragmentFileName = Config.getAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX") + 
    							"_" + new Integer(j) + "_" + new Integer(upperBound);
    					String originalFileName = Config.getAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX") + 
    							"_" + new Integer(lowerBound) + "_" + new Integer(upperBound);
    					fragmentToOriginalFileMap.put(fragmentFileName, originalFileName);
    					break;
    				}
    			}
    		}
    		
    		// no thread started for redo
    		if (isTaskDone) {
    			// console log
    			checkRoundPool.shutdown();
    			System.out.println("Pipeline: check pass.");
    			break;
    		}
    		
    		// no continue until all batch tasks are done
    		do {
    			Thread.sleep(1000);
    		} while (checkRoundSemaphore.availablePermits() != numberOfThread);
    		
    		// merge fragment generated by redo into original file
    		// console log
			System.out.println("Pipeline: start round " + round + " merging.");
    		for (Map.Entry<String, String> entry : fragmentToOriginalFileMap.entrySet()) {
    			File fragmentFile = new File(entry.getKey());
    			File originalFile = new File(entry.getValue());
    			Util.appendFile(originalFile, fragmentFile);
    		}
    		// console log
			System.out.println("Pipeline: round " + round + " merging done.");
    		round++;
        }
        
        // merge files into "TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH"
        File mergedFile = new File(Config.getAttri("TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH"));
        FileWriter mergedFileWriter = null;
        if (mergedFile.exists()) {
        	mergedFile.delete();
        	mergedFile.createNewFile();
        }
        mergedFileWriter = new FileWriter(mergedFile);
        
        for (int i = 0; i <= numberOfThread - 1; i++) {
        	File formulaCalculatedFile = null;
        	if (i != numberOfThread - 1) { 
        		formulaCalculatedFile = new File(Config.getAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(startId + batchSingleThreadAbility * (i + 1) - 1));
        	} else {
        		formulaCalculatedFile = new File(Config.getAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX") + 
        				"_" + new Integer(startId + batchSingleThreadAbility * i) + 
        				"_" + new Integer(endId));
        	}
			BufferedReader formulaCalculatedFileBufferedReader = new BufferedReader(new FileReader(formulaCalculatedFile));
			String oneLine = null;
			while ((oneLine = formulaCalculatedFileBufferedReader.readLine()) != null) {
				mergedFileWriter.write(oneLine + "\n");
			}
			formulaCalculatedFileBufferedReader.close();
			formulaCalculatedFile.delete();			
        }
		mergedFileWriter.close();
		
		// append "TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH" to "SOURCE_FOR_DIVIDE_PATH" if possible
		File sourceForDivideFile = new File(Config.getAttri("SOURCE_FOR_DIVIDE_PATH"));
        if (!sourceForDivideFile.exists()) {
        	sourceForDivideFile.createNewFile();
        }
        File toBeAppendedSourceForDivideFile = new File(Config.getAttri("TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH"));
		Util.appendFile(sourceForDivideFile, toBeAppendedSourceForDivideFile);
		
		// console log
		System.out.println("Pipeline: merge done.");
	}
	
	public void callShortestUniquePrefixFinder() throws Exception {
		ShortestUniquePrefixFinder supf = new ShortestUniquePrefixFinder();
		supf.getSUPAndOutput();
	}
}