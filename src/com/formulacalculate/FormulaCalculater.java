package com.formulacalculate;

import java.math.BigInteger;
import java.util.Scanner;
import com.wolfram.jlink.*;
import java.util.List;
import java.util.ArrayList;

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
	
	List<BigInteger> calculateToList(String expr) {
		//TODO(shenchen):transfer string to list
		return new ArrayList<BigInteger>();
	}
	
	String calculateToString(String expr) throws Exception{
		if (kernelLink == null) {
			throw new Exception("KernelLink");
		}
		return kernelLink.evaluateToOutputForm(expr, 0);
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
				String A = fc.calculateToString(expr);
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
