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
	
	// expr is the whole expr from webpage MATHEMATICA option
	// we should split it and campare with sample, leave sample 
	// null means don't compare
	public String calculateToString(String expr, String sample) throws Exception{
		if (kernelLink == null) {
			throw new Exception("KernelLink");
		}
		// handle expr
		List<String> exprList = splitExpr(expr);
		List<String> retList = new ArrayList<String>();
		for (String e : exprList) {
			retList.add(kernelLink.evaluateToOutputForm(e, 0));
		}
		
		if (sample == null) {//don't compare and return first elem not null
			for (String ret : retList) {
				if (ret != null) return ret;
			}
		} else {
			//TODO(shenchen):impl compare logic
		}
		return null;
	}
	
	// according to different rules, split expr and try
	List<String> splitExpr(String expr) {
		//TODO(shenchen):impl different rules, now we just don't split
		List<String> ret = new ArrayList<String>();
		ret.add(expr);
		return ret;
	}
	
	public List<BigInteger> calculateToList(String expr, String sample) throws Exception {
		String retStr = calculateToString(expr, sample);
		// convert string to list
		if (retStr == null || retStr.isEmpty()) {
			return null;
		}
		String numberStr = retStr.substring(1, retStr.length()-1);
		System.out.println(numberStr);
		String[] numberStrList = numberStr.split(", ");
		List<BigInteger> numberList = new ArrayList<BigInteger>();
		for (String numStr : numberStrList) {
			System.out.println(numStr);
			BigInteger num = new BigInteger(numStr);
			numberList.add(num);
		}
		return numberList;
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
				String A = fc.calculateToString(expr, null);
				//List<BigInteger> B = fc.calculateToList(expr);
				System.out.println("Response: " + A);
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
