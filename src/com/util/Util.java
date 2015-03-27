package com.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
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
	
	public static String getIndexFromItem(String item) {
		return item.substring(0, Constant.INDEX_WIDTH + 1);
	}
	
	public static String getContentFromItem(String item) {
		return item.substring(Constant.INDEX_WIDTH + 2);
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
		for (int i = 0; i <= content.length() - 1; i++) {
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
}
