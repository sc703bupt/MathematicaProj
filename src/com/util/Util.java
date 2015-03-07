package com.util;
import java.io.BufferedReader;
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
}
