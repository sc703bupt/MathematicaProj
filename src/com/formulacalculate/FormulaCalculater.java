package com.formulacalculate;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import com.util.Constant;
import com.util.Util;
import com.wolfram.jlink.*;

public class FormulaCalculater {
	private KernelLink kernelLink;
	
	public FormulaCalculater() {
		System.setProperty("com.wolfram.jlink.libdir", Constant.JLINK_DIR);
		try {
			kernelLink = MathLinkFactory.createKernelLink(Constant.KENERL_ARGV);
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
		if (Constant.IS_DETAIL_CALCULATION_LOG) {
			File currentIndexlogFile = new File(Constant.FORMULA_CALCULATE_LOG_PATH_PREFIX + index);
			if (currentIndexlogFile.exists()) {
				currentIndexlogFile.delete();
			}
			currentIndexlogFile.createNewFile();
			currentIndexLogFileWriter = new FileWriter(currentIndexlogFile);
		}
		
		if (kernelLink == null) {
			kernelLink = MathLinkFactory.createKernelLink(Constant.KENERL_ARGV);
			kernelLink.discardAnswer();// Get rid of the initial InputNamePacket
		}
		
		if (singleExprList == null || singleExprList.isEmpty()) {
			if (Constant.IS_DETAIL_CALCULATION_LOG) {
				currentIndexLogFileWriter.write("singelExprList is null or empty.\n");
				currentIndexLogFileWriter.close();	
			}
			return null;
		}
		
		if (Constant.IS_DETAIL_CALCULATION_LOG) {
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
		
		for (String singleExpr : singleExprList) {
			List<BigInteger> singleRet = getFromSingleExpr(singleExpr, index, statLogFileWriter);
			if (singleRet == null) {
				if (Constant.IS_DETAIL_CALCULATION_LOG) {
					currentIndexLogFileWriter.write("failed to calculate: " + singleExpr + "\n");	
				}
				continue;
			}
			if (singleRet.isEmpty()) {
				if (Constant.IS_DETAIL_CALCULATION_LOG) {
					currentIndexLogFileWriter.write("success to calculate but get empty result: " + 
							singleExpr + "\n");	
				}
				continue;
			}
			if (!compareTwoBigIntegerList(sampleNumberList, singleRet)) {
				if (Constant.IS_DETAIL_CALCULATION_LOG) {
					currentIndexLogFileWriter.write("success to calculate but inconsistent with sample: " + 
							singleExpr + "\n");	
				}
				continue;
			}
			if (Constant.IS_DETAIL_CALCULATION_LOG) {
				currentIndexLogFileWriter.write("success to calculate: " + singleExpr + "\n");
				currentIndexLogFileWriter.close();
			}
			return singleRet;
		}
		if (Constant.IS_DETAIL_CALCULATION_LOG) {
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
			InterruptTimer timer = new InterruptTimer(Constant.CALCULATE_TIME_OUT, kernelLink);
			timer.start();
			kernelLink.evaluate(expr);
			kernelLink.waitForAnswer();
			timer.interrupt();
			numStrArray = kernelLink.getStringArray1();
			numberList = new ArrayList<BigInteger>();
			for (String numStr : numStrArray) {
				BigInteger num = new BigInteger(numStr);
				if (num.compareTo(BigInteger.valueOf(Constant.PROGRESSION_MAX_VALUE)) == 1) {
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
	
	public static void main(String[] args) {
		FormulaCalculater fc = null;
		try {
			fc = new FormulaCalculater();
			while(true){
				System.out.println("Please enter expr:");
				Scanner s = new Scanner(System.in);
				String expr = s.nextLine();
				System.out.println("Please enter sample:");
				String sample = s.nextLine();
				List<String> exprList = new ArrayList<String>();
				exprList.add(expr);
				String A = fc.calculateToString(exprList, sample, "testIndex", null);
				System.out.println("Response: " + A);
				System.out.println("------------------------");
			}
		} catch (MathLinkException e) {
			System.out.println("MathLinkException occurred: " + e.getMessage());
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			fc.close();
		}
	}
}
