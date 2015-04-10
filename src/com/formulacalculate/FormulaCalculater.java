package com.formulacalculate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;

import com.config.Config;
import com.util.Util;
import com.wolfram.jlink.*;

public class FormulaCalculater {
	private KernelLink kernelLink;
	private int exprCount = 0;
	
	public FormulaCalculater() {
		System.setProperty("com.wolfram.jlink.libdir", Config.getAttri("JLINK_DIR"));
		try {
			kernelLink = MathLinkFactory.createKernelLink(Config.getAttri("KENERL_ARGV"));
			kernelLink.discardAnswer();// Get rid of the initial InputNamePacket
		} catch (MathLinkException e) {
			System.out.println("Fatal error opening link: " + e.getMessage());
			return;
		}
	}
	
	// complexExpr is the whole expression from webpage MATHEMATICA option
	// we should split it and compare each result from it with sample, 
	// leave sample null means don't compare
	public List<BigInteger> calculateToList(List<String> singleExprList, String sample, String index, FileWriter statLogFileWriter) throws Exception {
		FileWriter currentIndexLogFileWriter = null;
		Boolean isDetailCalculationLog = Boolean.parseBoolean(Config.getAttri("IS_DETAIL_CALCULATION_LOG"));
		if (isDetailCalculationLog) {
			File currentIndexlogFile = new File(Config.getAttri("FORMULA_CALCULATE_LOG_PATH_PREFIX") + index);
			if (currentIndexlogFile.exists()) {
				currentIndexlogFile.delete();
			}
			currentIndexlogFile.createNewFile();
			currentIndexLogFileWriter = new FileWriter(currentIndexlogFile);
		}
		
		if (singleExprList == null || singleExprList.isEmpty()) {
			if (isDetailCalculationLog) {
				currentIndexLogFileWriter.write("singelExprList is null or empty.\n");
				currentIndexLogFileWriter.close();	
			}
			return null;
		}
		
		if (isDetailCalculationLog) {
			currentIndexLogFileWriter.write("For index " + index + "\n");
			currentIndexLogFileWriter.write("Number of expressions: " + new Integer(singleExprList.size()) + "\n");	
		}
		
		// covert sample to List<BigInteger>
		sample = sample.trim();
		String[] sampleNumbers = sample.split(", ");
		List<BigInteger> sampleNumberList = new ArrayList<BigInteger>();
		for (String sampleNumber : sampleNumbers) {
			if (!Util.isNumber(sampleNumber.trim())) {
				continue;
			}
			sampleNumberList.add(new BigInteger(sampleNumber.trim()));
		}
		
		int interval = Integer.valueOf(Config.getAttri("INTERVAL_FOR_RESTART_MATHKERNEL"));
		for (String singleExpr : singleExprList) {
			if (exprCount == interval) {// force to kill when calculate some expressions
				kernelLink.terminateKernel();
				kernelLink.close();
				kernelLink = null;
			}
			if (kernelLink == null) {
				kernelLink = MathLinkFactory.createKernelLink(Config.getAttri("KENERL_ARGV"));
				kernelLink.discardAnswer();// Get rid of the initial InputNamePacket
				exprCount = 0;
			}
			List<BigInteger> singleRet = getFromSingleExpr(singleExpr, index, statLogFileWriter);
			exprCount++;
			if (singleRet == null) {
				if (isDetailCalculationLog) {
					currentIndexLogFileWriter.write("failed to calculate: " + singleExpr + "\n");	
				}
				continue;
			}
			if (singleRet.isEmpty()) {
				if (isDetailCalculationLog) {
					currentIndexLogFileWriter.write("success to calculate but get empty result: " + 
							singleExpr + "\n");	
				}
				continue;
			}
			if (!compareTwoBigIntegerList(sampleNumberList, singleRet)) {
				if (isDetailCalculationLog) {
					currentIndexLogFileWriter.write("success to calculate but inconsistent with sample: " + 
							singleExpr + "\n");	
				}
				continue;
			}
			if (isDetailCalculationLog) {
				currentIndexLogFileWriter.write("success to calculate: " + singleExpr + "\n");
				currentIndexLogFileWriter.close();
			}

			return singleRet;
		}
		if (isDetailCalculationLog) {
			currentIndexLogFileWriter.close();
		}
		return null;
	}
	
	// call calculateToList and get the String form
	public String calculateToString(List<String> singleExprList, String sample, String index, FileWriter statLogFile) throws Exception{
		List<BigInteger> retList =  calculateToList(singleExprList, sample, index, statLogFile);
		if (retList == null) return null;
		String retListStr = retList.toString();
		return retListStr.substring(1, retListStr.length()-1);
	}
	
	// only care about single expression and return its result
	private List<BigInteger> getFromSingleExpr(String expr, String index, FileWriter statLogFileWriter) throws IOException {
		String[] numStrArray = null;
		List<BigInteger> numberList = null;
		try {
			kernelLink.evaluate("Remove[\"Global`*\"];");// this command cleans all env used before
			kernelLink.discardAnswer();
			InterruptTimer timer = new InterruptTimer(Integer.parseInt(Config.getAttri("CALCULATE_TIME_OUT")), kernelLink);
			timer.start();
			kernelLink.evaluate(expr);
			kernelLink.waitForAnswer();
			timer.interrupt();
			numStrArray = kernelLink.getStringArray1();
			numberList = new ArrayList<BigInteger>();
			int progressionMaxValue = Integer.parseInt(Config.getAttri("PROGRESSION_MAX_VALUE"));
			for (String numStr : numStrArray) {
				BigInteger num = new BigInteger(numStr);
				if (num.compareTo(BigInteger.valueOf(progressionMaxValue)) == 1) {
					break;
				}
				numberList.add(num);
			}
			statLogFileWriter.write("[Finish]:" + index + "\n");	
		} catch (MathLinkException e) {
			if (e.getErrCode() == 25) {
				kernelLink.terminateKernel();
				kernelLink.close();
				kernelLink = null;
				statLogFileWriter.write("[TIMEOUT]:" + index + ", error code is " + e.getErrCode() + "\n");
			} else if(e.getErrCode() == 1) {
				kernelLink.terminateKernel();
				kernelLink.close();
				kernelLink = null;
				statLogFileWriter.write("[FATAL]:" + index + ", error code is " + e.getErrCode() + "\n");
			} else {
				kernelLink.clearError();
				kernelLink.newPacket();
				statLogFileWriter.write("[MathLinkException]: " + index + ", error code is " + e.getErrCode() + "\n");
			}
			return null;
		} catch (Exception e) {
			statLogFileWriter.write("[OtherException]:"+ index + ", reason is " + e.toString() + "\n");
			return null;
		}
		return numberList;
	}

	// compare prefix of two list
	// TODO(shenchen): add one para to enable fuzzy matching
	private boolean compareTwoBigIntegerList(List<BigInteger> sampleNumberList, List<BigInteger> singleRet) {
		int sampleNumberListLength = sampleNumberList.size();
		int singleRetLength = singleRet.size();
		int minLength = Math.min(sampleNumberListLength, singleRetLength);
		for (int i = 0; i <= minLength - 1; i++) {
			if (!sampleNumberList.get(i).equals(singleRet.get(i))) return false;
		}
		return true;
	}
	
	// remember call this method when never use KernelLink
	void close() {
		kernelLink.close();
	}
}
