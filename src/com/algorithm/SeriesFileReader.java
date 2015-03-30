package com.algorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class SeriesFileReader {
	public static ArrayList<String> readSeriesFromFile(String filePath) {
		ArrayList<String> list = new ArrayList<String>();
		File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String temp = null;
            String [] seriesNumbers = null;
            while ((temp = reader.readLine()) != null) {
            	StringBuilder sb = new StringBuilder();
            	seriesNumbers = temp.split(",");
            	sb.append(seriesNumbers[0]);
            	for (int i = 1; i < seriesNumbers.length; i++) {
            		if (seriesNumbers[i].length() >= 8) {
            			break;
            		}
            		sb.append(","+seriesNumbers[i]);
            	}
            	list.add(sb.toString());
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        return list;
	}
}
