package com.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Config {
	private static Config config = new Config();
	private static String SAMPLE_FILE_SAVE_PATH_PREFIX = "D:\\sample\\data";
	private static String EXPRESSION_FILE_SAVE_PATH_PREFIX = "D:\\expr\\replaceExpr";
	private static String FILE_PARSER_LOG_SAVE_PATH_PREFIX = "D:\\parseLog\\";
	private static String WEB_PAGE_SAVE_PATH_PREFIX = "D:\\download\\";
	
	public static Config getInstance()
	{
		return config;
	}

	private Config()
	{
		Properties p = new Properties();
		try
		{
			InputStream in = this.getClass().getClassLoader().getResourceAsStream("config/config.properties");
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
		
		setSAMPLE_FILE_SAVE_PATH_PREFIX(p.getProperty("SAMPLE_FILE_SAVE_PATH_PREFIX"));
		setEXPRESSION_FILE_SAVE_PATH_PREFIX(p.getProperty("EXPRESSION_FILE_SAVE_PATH_PREFIX"));
		setFILE_PARSER_LOG_SAVE_PATH_PREFIX(p.getProperty("FILE_PARSER_LOG_SAVE_PATH_PREFIX"));
	}

	public static String getSAMPLE_FILE_SAVE_PATH_PREFIX() {
		return SAMPLE_FILE_SAVE_PATH_PREFIX;
	}

	public static void setSAMPLE_FILE_SAVE_PATH_PREFIX(
			String sAMPLE_FILE_SAVE_PATH_PREFIX) {
		SAMPLE_FILE_SAVE_PATH_PREFIX = sAMPLE_FILE_SAVE_PATH_PREFIX;
	}

	public static String getEXPRESSION_FILE_SAVE_PATH_PREFIX() {
		return EXPRESSION_FILE_SAVE_PATH_PREFIX;
	}

	public static void setEXPRESSION_FILE_SAVE_PATH_PREFIX(
			String eXPRESSION_FILE_SAVE_PATH_PREFIX) {
		EXPRESSION_FILE_SAVE_PATH_PREFIX = eXPRESSION_FILE_SAVE_PATH_PREFIX;
	}

	public static String getFILE_PARSER_LOG_SAVE_PATH_PREFIX() {
		return FILE_PARSER_LOG_SAVE_PATH_PREFIX;
	}

	public static void setFILE_PARSER_LOG_SAVE_PATH_PREFIX(
			String fILE_PARSER_LOG_SAVE_PATH_PREFIX) {
		FILE_PARSER_LOG_SAVE_PATH_PREFIX = fILE_PARSER_LOG_SAVE_PATH_PREFIX;
	}

	public static String getWEB_PAGE_SAVE_PATH_PREFIX() {
		return WEB_PAGE_SAVE_PATH_PREFIX;
	}

	public static void setWEB_PAGE_SAVE_PATH_PREFIX(
			String wEB_PAGE_SAVE_PATH_PREFIX) {
		WEB_PAGE_SAVE_PATH_PREFIX = wEB_PAGE_SAVE_PATH_PREFIX;
	}
}
