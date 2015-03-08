package com.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Util {
	public static String getIndexFromID(int id) {
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for (int i = sb.length(); i < Constant.INDEX_WIDTH; i++) {
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
	
	public static void write(FileWriter writer, String content) {          
        try {               
        	writer.write(content);               
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public static boolean appendOneRow(FileWriter fw, String key, String content) {
		if (fw == null || key == null || content == null) {
			return false;
		}
		
		try {
			fw.write(key + ":" + content);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
		
	public static List<String> readContentByRange(FileReader fr, String key, int startId, int endId) {
		//TODO(shenchen):impl
		return null;
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
}
