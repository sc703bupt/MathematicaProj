package com.formulacalculate;

import java.util.Scanner;

public class SpliterTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Please enter expr:");
		Scanner s = new Scanner(System.in);
		String expr = s.nextLine();
		int pos = 0;
		int x;
		do{
			x = expr.indexOf("*)",pos);
			if(x != -1)
			System.out.println(x + ":" + expr.substring(pos, x+2));	
			pos = x + 2;
		}while(x!=-1);
	}
}
