package com.formulacalculate;

import java.io.File;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

import com.util.Constant;
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
	public List<BigInteger> calculateToList(List<String> singleExprList, String sample, String index) throws Exception {
		File logFile = new File(Constant.FORMULA_CALCULATE_LOG_PATH_PREFIX + index);
		if (logFile.exists()) {
			logFile.delete();
		}
		logFile.createNewFile();
		FileWriter logFileWriter = new FileWriter(logFile);
		
		if (kernelLink == null) {
			logFileWriter.write("KenerLink is null.\n");
			logFileWriter.close();
			throw new Exception("KernelLink");
		}
		if (singleExprList == null || singleExprList.isEmpty()) {
			logFileWriter.write("singelExprList is null or empty.\n");
			logFileWriter.close();
			return null;
		}
		
		logFileWriter.write("For index " + index + "\n");
		logFileWriter.write("Number of expressions: " + new Integer(singleExprList.size()) + "\n");
		
		// covert sample to List<BigInteger>
		sample = sample.trim();
		String[] sampleNumbers = sample.split(", ");
		List<BigInteger> sampleNumberList = new ArrayList<BigInteger>();
		for (String sampleNumber : sampleNumbers) {
			sampleNumberList.add(new BigInteger(sampleNumber.trim()));
		}
		
		for (String singleExpr : singleExprList) {
			List<BigInteger> singleRet = getFromSingleExpr(singleExpr, index);
			if (singleRet == null) {
				logFileWriter.write("failed to calculate: " + singleExpr + "\n");
				continue;
			}
			if (singleRet.isEmpty()) {
				logFileWriter.write("success to calculate but get empty result: " + 
						singleExpr + "\n");
				continue;
			}
			if (!compareTwoBigIntegerList(sampleNumberList, singleRet)) {
				logFileWriter.write("success to calculate but inconsistent with sample: " + 
						singleExpr + "\n");
				continue;
			}
			logFileWriter.write("success to calculate: " + singleExpr + "\n");
			logFileWriter.close();
			return singleRet;
		}
		logFileWriter.close();
		return null;
	}
	
	// call calculateToList and get the String form
	public String calculateToString(List<String> singleExprList, String sample, String index) throws Exception{
		List<BigInteger> retList =  calculateToList(singleExprList, sample, index);
		if (retList == null) return null;
		String retListStr = retList.toString();
		return retListStr.substring(1, retListStr.length()-1);
	}
	
	// only care about single expression and return its result
	private List<BigInteger> getFromSingleExpr(String expr, String index) {
		String[] numStrArray = null;
		List<BigInteger> numberList = null;
		try {
			if (kernelLink == null) {
				kernelLink = MathLinkFactory.createKernelLink(Constant.KENERL_ARGV);
				kernelLink.discardAnswer();// Get rid of the initial InputNamePacket
			}
			kernelLink.evaluate("Remove[\"Global`*\"];");// this command cleans all env used before
			kernelLink.discardAnswer();
			InterruptTimer timer = new InterruptTimer(Constant.CALCULATE_TIME_OUT, kernelLink, index);
			timer.start();
			kernelLink.evaluate(expr);
			kernelLink.waitForAnswer();
			timer.interrupt();
			numStrArray = kernelLink.getStringArray1();
			numberList = new ArrayList<BigInteger>();
		} catch (MathLinkException e) {
			if (kernelLink.clearError()) {
				kernelLink.newPacket();
			} else {
				kernelLink.terminateKernel();
				kernelLink.close();
				kernelLink = null;
			}
			return null;
		}
		for (String numStr : numStrArray) {
			numberList.add(new BigInteger(numStr));
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
				String A = fc.calculateToString(exprList, sample, "testIndex");
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
