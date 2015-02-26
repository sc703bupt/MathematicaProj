package com.formulacalculate;

import java.math.BigInteger;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import com.wolfram.jlink.*;

public class FormulaCalculater {
	private KernelLink kernelLink;
	public FormulaCalculater() {
		String jLinkDir = "c:\\Program Files\\Wolfram Research\\Mathematica\\9.0\\SystemFiles\\Links\\JLink";
		System.setProperty("com.wolfram.jlink.libdir", jLinkDir);
		String argv = new String("-linkmode launch -linkname 'c:\\Program Files\\Wolfram Research\\Mathematica\\9.0\\mathkernel.exe'");
		try {
			kernelLink = MathLinkFactory.createKernelLink(argv);
			kernelLink.discardAnswer();// Get rid of the initial InputNamePacket
		} catch (MathLinkException e) {
			System.out.println("Fatal error opening link: " + e.getMessage());
			return;
		}
	}
	
	// complexExpr is the whole expression from webpage MATHEMATICA option
	// we should split it and compare each result from it with sample, 
	// leave sample null means don't compare
	public List<BigInteger> calculateToList(String complexExpr, String sample) throws Exception {
		if (kernelLink == null) {
			throw new Exception("KernelLink");
		}
		List<String> singleExprList = splitExpr(complexExpr);
		if (singleExprList == null || singleExprList.isEmpty()) {
			System.out.println("failed to split complex expr and get nothing.");
			return null;
		}
		List<List<BigInteger>> multiRet = new ArrayList<List<BigInteger>>();
		for (String singleExpr : singleExprList) {
			multiRet.add(getFromSingleExpr(singleExpr));
		}
		// sample == null means don't compare and return first elem not null and not emtpy
		if (sample == null || sample.isEmpty()) {
			for (List<BigInteger> singleRet : multiRet) {
				if (singleRet != null && !singleRet.isEmpty()) return singleRet;
			}
		} else {
			// return the first one elem that matches sample
			// convert sample to List<BigInteger>
			sample = sample.trim();
			String[] sampleNumbers = sample.split(",");
			List<BigInteger> sampleNumberList = new ArrayList<BigInteger>();
			for (String sampleNumber : sampleNumbers) {
				sampleNumberList.add(new BigInteger(sampleNumber.trim()));
			}
			for (List<BigInteger> singleRet : multiRet) {
				if (compareTwoBigIntegerList(sampleNumberList, singleRet)) {
					return singleRet;
				}
			}
		}
		return null;
	}
	
	// call calculateToList and get the String form
	public String calculateToString(String complexExpr, String sample) throws Exception{
		List<BigInteger> retList =  calculateToList(complexExpr, sample);
		if (retList == null) return null;
		return retList.toString();
	}
	
	// only care about single expression and return its result
	private List<BigInteger> getFromSingleExpr(String expr) {
		String[] numStrArray = null;
		List<BigInteger> numberList = null;
		try {
			kernelLink.evaluate("Remove[\"Global`*\"];");// this command cleans all env used before
			kernelLink.discardAnswer();
			kernelLink.evaluate(expr);
			kernelLink.waitForAnswer();
			numStrArray = kernelLink.getStringArray1();
			numberList = new ArrayList<BigInteger>();
		} catch (MathLinkException e) {
			System.out.println("Expcetion : " + e.toString() + ". when evaluating '" 
				+ expr + "'");
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
	
	// according to different rules, split expr and try
	// now only split by "*)"
	List<String> splitExpr(String complexExpr) {
		if (complexExpr == null) {
			return null;
		} 
		List<String> ret = new ArrayList<String>();
		if (complexExpr.indexOf("*)") == -1) {// finds no "*)"
			ret.add(complexExpr);
			return ret;
		}
		int startPos = 0;
		int symbolPos;
		do {
			symbolPos = complexExpr.indexOf("*)", startPos);
			if (symbolPos != -1) {
				ret.add(complexExpr.substring(startPos, symbolPos + 2));
			}
			startPos = symbolPos + 2;
		} while (symbolPos != -1);
		return ret;
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
				String A = fc.calculateToString(expr, sample);
				//List<BigInteger> B = fc.calculateToList(expr, null);
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
