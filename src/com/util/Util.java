package com.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import com.config.Config;

public class Util {
	public static String getIndexFromID(int id) {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for (int i = sb.length(); i < Integer.parseInt(Config.getAttri("INDEX_WIDTH")); i++) {
			sb.insert(0, "0");
		}
		sb.insert(0, 'A');
		return sb.toString();
	}
	
	public static int getIDFromIndex(String index) {
		for (int i = 1; i <= index.length()-1; i++) {
			if (index.charAt(i) != '0') {
				return new Integer(index.substring(i)).intValue();
			}
		}
		return 0;
	}
	
	public static String getIndexFromItem(String item) {
		return item.substring(0, Integer.parseInt(Config.getAttri("INDEX_WIDTH")) + 1);
	}
	
	public static String getContentFromItem(String item) {
		return item.substring(Integer.parseInt(Config.getAttri("INDEX_WIDTH")) + 2);
	}
	
	public static void write(FileWriter writer, String content) {          
        try {               
        	writer.write(content);               
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public static boolean isNumber(String content) {
		if (content == null) {
			return false;
		}
		content = content.trim();
		if (content.equals("")) {
			return false;
		}
		for (int i = 0; i <= content.length() - 1; i++) {
			if (i == 0 && content.charAt(i) == '-') {
				continue;
			}
			char c = content.charAt(i);
			if (!(c >= '0' && c <= '9')) {
				return false;
			}
		}
		return true;
	}
	
	// delete all sub-files and sub-folders, then the folder itself
	// Note: it works well for inputing a single file name
	public static void delFolder(String absFolderName) throws IOException {
	    File file = new File(absFolderName);
	    if (!file.exists()) {
	    	throw new IOException("not exist.");
	    }
	    if (file.isFile()) {
	    	file.delete();
	    	return;
	    }
	    if (file.isDirectory()) {
	    	String[] fileList = file.list();
	 	    for (int i = 0; i <= fileList.length-1; i++) {
	 	    	// generate absFileName
	 	    	String absFileName;
	 	    	if (absFolderName.endsWith("\\")) {
	 	    		absFileName = absFolderName + fileList[i];
	 	        } else {
	 	        	absFileName = absFolderName + "\\" + fileList[i];
	 	        }
	 	    	delFolder(absFileName);
	 	    }
	 	    file.delete();
	    }
	    return;
	}
	
	// copy one file to a specified place
	public static boolean copyFile(String absSourceFilePath, String absDestFilePath) {
		File sourceFile = new File(absSourceFilePath);
		File destFile = new File(absDestFilePath);
		if (!sourceFile.exists()) {
			return false;
		}
		if (destFile.exists()) {
			try {
				Util.delFolder(absDestFilePath);
				destFile.createNewFile();
			} catch (IOException e) {
				return false;
			}
		}
        FileInputStream fi = null;
        FileOutputStream fo = null;
        FileChannel in = null;
        FileChannel out = null;
        try {
            fi = new FileInputStream(sourceFile);
            fo = new FileOutputStream(destFile);
            in = fi.getChannel();
            out = fo.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            return false;
        } finally {
            try {
            	in.close();
                fi.close();
                out.close();
                fo.close();                
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
	
	// delete srcFile by default
	public static void appendFile(File destFile, File srcFile) throws IOException {
		FileWriter destFileWriter = new FileWriter(destFile, true); // append mode
		BufferedReader srcFileBufferedReader = new BufferedReader(new FileReader(srcFile));
		String oneLine = null;
		while ((oneLine = srcFileBufferedReader.readLine()) != null) {
			destFileWriter.write(oneLine + "\n");
		}
		destFileWriter.close();
		srcFileBufferedReader.close();
		srcFile.delete();
	}
	
	public static int getTotalPageCountFromFile() {
		File totalPageNumberFile = new File(Config.getAttri("TOTAL_PAGES_COUNT_PATH")); 
		BufferedReader tpnReader = null;
		try {
			tpnReader = new BufferedReader(new FileReader(totalPageNumberFile));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		int totalPageCount = 0;
		try {
			totalPageCount = Integer.parseInt(tpnReader.readLine());
			tpnReader.close();
		} catch (NumberFormatException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return totalPageCount;
	}
	
	public static void setTotalPageCount(int number) {
		try {
			File file = new File(Config.getAttri("TOTAL_PAGES_COUNT_PATH"));
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			FileWriter fw = new FileWriter(file);
			fw.write(number + "");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	// cut progression by the upper bound PROGRESSION_MAX_VALUE and PROGRESSION_LENGTH
	public static String progressionCutter(String progression) {
		String content = Util.getContentFromItem(progression);
		String index = Util.getIndexFromItem(progression);
		String[] progressionNumbers = content.trim().split(", ");
		List<BigInteger> progressionNumberList = new ArrayList<BigInteger>();
		int progressionMaxValue = Integer.parseInt(Config.getAttri("PROGRESSION_MAX_VALUE"));
		int progressionMaxLength = Integer.parseInt(Config.getAttri("PROGRESSION_LENGTH"));
		int currentLength = 0;
		for (String numberStr : progressionNumbers) {
			if (!Util.isNumber(numberStr.trim())) {
				continue;
			}
			BigInteger number = new BigInteger(numberStr);
			if (number.compareTo(BigInteger.valueOf(progressionMaxValue)) == 1
				|| currentLength > progressionMaxLength) {
				break;
			}
			progressionNumberList.add(number);
			currentLength++;
		}
		String cuttedProgressionStr = progressionNumberList.toString();
		return index + ":" + cuttedProgressionStr.substring(1, cuttedProgressionStr.length()-1);
	}
}
