package com.util;

public class Constant {
	public final static int TOTAL_PAGES_COUNT = 255728;
	public final static int FOLDER_LEVEL_FOR_DIVIDE = 3;
	public final static int INDEX_WIDTH = 6; // not including leading 'A'
	public final static long CALCULATE_TIME_OUT = 10000; // 10s
	public final static int PROGRESSION_LENGTH = 110;
	
	public final static boolean IS_DETAIL_CALCULATION_LOG = false;
	
	public final static String OEIS_URL_PREFIX = "http://oeis.org/";
	public final static String JLINK_DIR = "c:\\Program Files\\Wolfram Research\\Mathematica\\9.0\\SystemFiles\\Links\\JLink";
	public final static String KENERL_ARGV = "-linkmode launch -linkname 'c:\\Program Files\\Wolfram Research\\Mathematica\\9.0\\mathkernel.exe'";

	public final static String SAMPLE_FILE_PATH = "C:\\MyEclipseProj\\MathematicaProj\\data\\sample";
	public final static String EXPRESSION_FILE_PATH = "C:\\MyEclipseProj\\MathematicaProj\\data\\expression";
	public final static String REPLACED_EXPRESSION_FILE_PATH = "C:\\MyEclipseProj\\MathematicaProj\\data\\replaceExpr";

	public final static String FORMULA_CALCULATED_SAVE_PATH_PREFIX = "c:\\MathOutput\\FormulaCalculated";
	public final static String FORMULA_CALCULATE_LOG_PATH_PREFIX = "c:\\MathOutput\\";
	public final static String FORMULA_STATICATICS_LOG_PREFIX = "c:\\MathOutput\\Statistics";
	
	public final static String SOURCE_FOR_DIVIDE_PATH = "c:\\MathOutput\\FormulaCalculated_1_1000_1";
	public final static String DIVIDE_SAVE_PATH_PREFIX = "c:\\MathOutput\\";
	
	public final static String DEFAULT_LEAF_FILE = "LeafFile";
}
