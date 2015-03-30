package com.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.naming.Context;


public class Config {
	private static Config config = new Config();
	private static String SAMPLE_FILE_SAVE_PATH_PREFIX = "D:\\sample\\data";
	private static String EXPRESSION_FILE_SAVE_PATH_PREFIX = "D:\\expr\\replaceExpr";
	private static String FILE_PARSER_LOG_SAVE_PATH_PREFIX = "D:\\parseLog\\";
	private static String WEB_PAGE_SAVE_PATH_PREFIX = "D:\\download\\";
	private static String SERIESE_FILE_PATH = "C:\\Users\\Lee\\Desktop\\demo_result_A000000_A030000.txt"; 
	
	public static Config getInstance()
	{
		return config;
	}

	public Config()
	{
		Properties p = new Properties();
		try
		{
			//System.out.println(this.);
			//FileInputStream in = new FileInputStream("/com/config/config.properties");
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("com/config/config.properties");
			p.load(in);
			in.close();
		}
		catch (FileNotFoundException ex)
		{
			System.out.print("Configure file not found \n");
			ex.printStackTrace();
		}
		catch (IOException e)
		{
			System.out.print("loading failed\n");
			e.printStackTrace();
		}
		
//		setSAMPLE_FILE_SAVE_PATH_PREFIX(p.getProperty("SAMPLE_FILE_SAVE_PATH_PREFIX"));
//		setEXPRESSION_FILE_SAVE_PATH_PREFIX(p.getProperty("EXPRESSION_FILE_SAVE_PATH_PREFIX"));
//		setFILE_PARSER_LOG_SAVE_PATH_PREFIX(p.getProperty("FILE_PARSER_LOG_SAVE_PATH_PREFIX"));
		setSERIESE_FILE_PATH(p.getProperty("SERIESE_FILE_PATH"));
	}

	public static String getSAMPLE_FILE_SAVE_PATH_PREFIX() {
		return SAMPLE_FILE_SAVE_PATH_PREFIX;
	}

	public static void setSAMPLE_FILE_SAVE_PATH_PREFIX(
			String str) {
		SAMPLE_FILE_SAVE_PATH_PREFIX = str;
	}

	public static String getEXPRESSION_FILE_SAVE_PATH_PREFIX() {
		return EXPRESSION_FILE_SAVE_PATH_PREFIX;
	}

	public static void setEXPRESSION_FILE_SAVE_PATH_PREFIX(
			String str) {
		EXPRESSION_FILE_SAVE_PATH_PREFIX = str;
	}

	public static String getFILE_PARSER_LOG_SAVE_PATH_PREFIX() {
		return FILE_PARSER_LOG_SAVE_PATH_PREFIX;
	}

	public static void setFILE_PARSER_LOG_SAVE_PATH_PREFIX(
			String str) {
		FILE_PARSER_LOG_SAVE_PATH_PREFIX = str;
	}

	public static String getWEB_PAGE_SAVE_PATH_PREFIX() {
		return WEB_PAGE_SAVE_PATH_PREFIX;
	}

	public static void setWEB_PAGE_SAVE_PATH_PREFIX(
			String str) {
		WEB_PAGE_SAVE_PATH_PREFIX = str;
	}

	public static String getSERIESE_FILE_PATH() {
		return SERIESE_FILE_PATH;
	}

	public static void setSERIESE_FILE_PATH(String str) {
		SERIESE_FILE_PATH = str;
	}


}
