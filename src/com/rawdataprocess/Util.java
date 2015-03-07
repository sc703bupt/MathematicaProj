package com.rawdataprocess;
import java.io.FileWriter;
import java.io.IOException;


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
}
