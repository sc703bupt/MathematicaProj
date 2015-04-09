package com.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Config {
	private static Config config = new Config();
	private static Map<String, String> attriMap;
    private static Properties p;

	private Config()
	{
		p = new Properties();
		attriMap = new HashMap<String, String>();
		try
		{
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
		
		// only for package:rawdataprocess
		setAttri("OEIS_URL_PREFIX", p.getProperty("OEIS_URL_PREFIX"));
		setAttri("WEB_PAGE_SAVE_PATH_PREFIX", p.getProperty("WEB_PAGE_SAVE_PATH_PREFIX"));
		setAttri("LOG_SAVE_PATH_PREFIX", p.getProperty("LOG_SAVE_PATH_PREFIX"));
		setAttri("EXPRESSION_FILE_PATH", p.getProperty("EXPRESSION_FILE_PATH"));
		setAttri("PROGRESSION_LENGTH", p.getProperty("PROGRESSION_LENGTH"));
		setAttri("SERIESE_FILE_PATH", p.getProperty("SERIESE_FILE_PATH"));
		
		// only for package:formulacalculate
		setAttri("TOTAL_PAGES_COUNT", p.getProperty("TOTAL_PAGES_COUNT"));
		setAttri("CALCULATE_TIME_OUT", p.getProperty("CALCULATE_TIME_OUT"));
		setAttri("JLINK_DIR", p.getProperty("JLINK_DIR"));
		setAttri("KENERL_ARGV", p.getProperty("KENERL_ARGV"));
		setAttri("SAMPLE_FILE_PATH", p.getProperty("SAMPLE_FILE_PATH"));
		setAttri("PROGRESSION_MAX_VALUE", p.getProperty("PROGRESSION_MAX_VALUE"));
		setAttri("FORMULA_CALCULATED_SAVE_PATH_PREFIX", p.getProperty("FORMULA_CALCULATED_SAVE_PATH_PREFIX"));
		setAttri("FORMULA_CALCULATE_LOG_PATH_PREFIX", p.getProperty("FORMULA_CALCULATE_LOG_PATH_PREFIX"));
		setAttri("FORMULA_STATICATICS_LOG_PREFIX", p.getProperty("FORMULA_STATICATICS_LOG_PREFIX"));
		setAttri("IS_DETAIL_CALCULATION_LOG", p.getProperty("IS_DETAIL_CALCULATION_LOG"));
		setAttri("BATCH_SINGLE_THREAD_ABILITY", p.getProperty("BATCH_SINGLE_THREAD_ABILITY"));

		// only for package:shorestuniqueprefix
		setAttri("SOURCE_FOR_DIVIDE_PATH", p.getProperty("SOURCE_FOR_DIVIDE_PATH"));
		setAttri("TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH", p.getProperty("TO_BE_APPENDED_SOURCE_FOR_DIVIDE_PATH"));
		setAttri("DIVIDE_SAVE_PATH_PREFIX", p.getProperty("DIVIDE_SAVE_PATH_PREFIX"));
		setAttri("FILE_PATH_LENGTH_LIMIT", p.getProperty("FILE_PATH_LENGTH_LIMIT"));
		setAttri("SUP_INIT_FILE_NAME", p.getProperty("SUP_INIT_FILE_NAME"));
		setAttri("SINGLE_FILE_ITEM_MAX_COUNT", p.getProperty("SINGLE_FILE_ITEM_MAX_COUNT"));

		// only for pakcage:util
		setAttri("INDEX_WIDTH", p.getProperty("INDEX_WIDTH"));
		setAttri("TOTAL_PAGES_COUNT_PATH", p.getProperty("TOTAL_PAGES_COUNT_PATH"));
	}

	public static void setAttri(String attriName, String attriValue) {
		attriMap.put(attriName, attriValue);
	}
	
	public static String getAttri(String attriName) {
		return attriMap.get(attriName);
	}
	
}
