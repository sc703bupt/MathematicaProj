package com.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Util {
	public static String getFileNameFromID(int id, int idLength) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		sb.append(id);
		for (int i = sb.length(); i < idLength; i++) {
			sb.insert(0, "0");
		}
		sb.insert(0, 'A');
		return sb.toString();
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
    public static void delFolder(String absFolderName) {
    	try {
    		delAllFile(absFolderName);
    		File myFilePath = new File(absFolderName);
    		myFilePath.delete();
    	} catch (Exception e) {
    		e.printStackTrace(); 
    	}
    }

    // called by delFolder
	private static void delAllFile(String absFolderName) throws IOException {
	    File file = new File(absFolderName);
	    if (!file.exists() || !file.isDirectory()) {
	    	throw new IOException("absFolderName is not exist or not a directory name.");
	    }
	    String[] fileList = file.list();
	    File absFile = null;
	    for (int i = 0; i <= fileList.length-1; i++) {
	    	// generate absFile
	    	if (absFolderName.endsWith("\\")) {
	    		absFile = new File(absFolderName + fileList[i]);
	        } else {
	        	absFile = new File(absFolderName + "\\" + fileList[i]);
	        }
	    	
	    	// if single file, just delete
	        if (absFile.isFile()) {
	        	absFile.delete();
	        	continue;
	        }
	        
	        // if directory, recursively delete
	        if (absFile.isDirectory()) {
	        	delAllFile(absFolderName + "\\" + fileList[i]);
	            delFolder(absFolderName + "\\" + fileList[i]);
	        }
	    }
	}
}
