package com.rawdataprocess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.util.Util;


public class FileParser extends Thread {
	private String filePath;
	private int startID;
	private int endID;
	private FileWriter sampleDataFileWriter;
	private FileWriter expressionFileWriter;
	
	final String sampleFilePathPrefix = "D:\\sample\\data";
	final String expressionFilePathPrefix = "D:\\expr\\expr";
	final String parseLogPath = "D:\\parseLog\\";
	
	FileParser(String filePath, int startID, int endID){
		this.filePath = filePath;
		this.startID = startID;
		this.endID = endID;
		
		String sampleDataFile = sampleFilePathPrefix + new Integer(startID / 10000).toString();
		String expressionFile = expressionFilePathPrefix + new Integer(startID / 10000).toString();
		try {
			sampleDataFileWriter = new FileWriter(new File(sampleDataFile), true);
			expressionFileWriter = new FileWriter(new File(expressionFile), true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		parseAllFiles();
	}
	public void parseAllFiles(){
		File logFile = null;
		FileWriter fw = null;
		try {
			logFile = new File(parseLogPath + new Integer(startID/10000).toString());
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			fw = new FileWriter(logFile, true);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		for (int i = startID; i <= endID; i++){
			ParseResult res = parseFile(i);
			try {
				switch (res) {
				case SAMPLE_AND_MATHEMATICA:
					fw.write(new Integer(i).toString() + " : " + "has sample and expression\n");
					break;
				case FORMATERROR:
					fw.write(new Integer(i).toString() + " : " + "file format error\n");
					break;
				case READFAIL:
					fw.write(new Integer(i).toString() + " : " + "failed when reading file\n");
					break;
				case NO_MATHEMATICA:
					fw.write(new Integer(i).toString() + " : " + "has no expression\n");
					break;
				case NO_SAMPLE_AND_MATHEMATICA:
					fw.write(new Integer(i).toString() + " : " + "has no sample and expression\n");
					break;
				}
				fw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			fw.close();
			sampleDataFileWriter.close();
			expressionFileWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public enum ParseResult {
		SAMPLE_AND_MATHEMATICA, FORMATERROR, READFAIL, NO_MATHEMATICA, NO_SAMPLE_AND_MATHEMATICA
    }
	
	public ParseResult parseFile(int fileID) {
		int idLength = 6;
		String fileFullName = Util.getFileNameFromID(fileID, idLength);
		String fileName = filePath + fileFullName;
		File file = new File(fileName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;    
            
            //------Get sample data------
            String sampleMarker = "<table cellspacing=\"0\" cellpadding=\"2\" cellborder=\"0\">"; 
            while ((tempString = reader.readLine()) != null) {
            	if(tempString.indexOf(sampleMarker) != -1) {
            		break;
            	}
            }           
            if (tempString == null) {
            	return ParseResult.FORMATERROR;
            }
            
            for (int i = 0; i < 4 && (tempString = reader.readLine()) != null; i++) {
            	//read 4 lines
            }
            if (tempString == null) {
            	return ParseResult.FORMATERROR;
            }
            
            String sample = null;             
            String startTag = "<tt>";
            String endTag = "</tt>";
            int sampleStartIndex = tempString.indexOf(startTag);
            boolean hasSample = true;
            if (sampleStartIndex == -1) {
            	hasSample = false;
            } else {
            	sample = tempString.substring(sampleStartIndex + startTag.length()).trim();
                sample = sample.substring(0, sample.length() - endTag.length());
                //System.out.println(sample);
                //write to file              
                Util.write(sampleDataFileWriter, fileFullName + ":" + sample + "\n");             
            }

            //------Get MATHEMATICA------
            String mathematicaMarker = "<font size=-2>MATHEMATICA</font>";
            while ((tempString = reader.readLine()) != null) {
            	if(tempString.indexOf(mathematicaMarker) != -1) {
            		break;
            	}
            }     
            //has no MATHEMATICA
            if (tempString == null) {
            	if (hasSample) {
            		return ParseResult.NO_MATHEMATICA;
            	} else {
            		return ParseResult.NO_SAMPLE_AND_MATHEMATICA;
            	}
            }
            
            for (int i = 0; i < 3 && (tempString = reader.readLine()) != null; i++) {
            	//read 3 lines
            }
            if (tempString == null) {
            	return ParseResult.FORMATERROR;
            }
            
            String emptyStr = "";
            tempString = tempString.trim();
            String expression = null;
            while (tempString.compareTo(emptyStr) != 0) {
            	int expressionStartIndex = tempString.indexOf(startTag);
            	expression = tempString.substring(expressionStartIndex + startTag.length()).trim();
            	expression = expression.substring(0, expression.length() - endTag.length());
            	//System.out.println(expression);
            	Util.write(expressionFileWriter, fileFullName + ":" + expression + "\n"); 
            	
            	//find next MATHEMATICA
            	for (int i = 0; i < 2 && (tempString = reader.readLine()) != null; i++) {
                	//read 2 lines
                }
                if (tempString == null) {
                	return ParseResult.FORMATERROR;
                }
            	tempString = tempString.trim();
            }
           

            reader.close();
            return ParseResult.SAMPLE_AND_MATHEMATICA;
        } catch (IOException e) {
            e.printStackTrace();
            return ParseResult.READFAIL;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }		
	}
}
