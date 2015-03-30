package com.rawdataprocess;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.config.Config;
import com.util.Util;


public class FileParser extends Thread {
	private String filePath;
	private int startID;
	private int endID;
	private FileWriter sampleDataFileWriter;
	private FileWriter expressionFileWriter;
	private FileWriter logFileWriter;
			
	public FileParser(int startID, int endID){
		this.filePath = Config.getInstance().getWEB_PAGE_SAVE_PATH_PREFIX();
		this.startID = startID;
		this.endID = endID;
	}
	
	void init () {
		String sampleDataFile = Config.getInstance().getSAMPLE_FILE_SAVE_PATH_PREFIX() + this.getName();
		String expressionFile = Config.getInstance().getEXPRESSION_FILE_SAVE_PATH_PREFIX() + this.getName();
		try {
			sampleDataFileWriter = new FileWriter(new File(sampleDataFile), true);
			expressionFileWriter = new FileWriter(new File(expressionFile), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		File logFile = null;
		try {
			logFile = new File(Config.getInstance().getFILE_PARSER_LOG_SAVE_PATH_PREFIX() + this.getName());
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			logFileWriter = new FileWriter(logFile, true);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
	}
	
	
	void finalizeFunc() {
		try {
			logFileWriter.close();
			sampleDataFileWriter.close();
			expressionFileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		parseAllFiles();
	}
	
	public void parseAllFiles(){
		init();
				
		for (int i = startID; i <= endID; i++){
			ParseResult res = parseFile(i);
			try {
				switch (res) {
				case SAMPLE_AND_MATHEMATICA:
					logFileWriter.write(new Integer(i).toString() + " : " + "has sample and expression\n");
					break;
				case FORMATERROR:
					logFileWriter.write(new Integer(i).toString() + " : " + "file format error\n");
					break;
				case READFAIL:
					logFileWriter.write(new Integer(i).toString() + " : " + "failed when reading file\n");
					break;
				case NO_MATHEMATICA:
					logFileWriter.write(new Integer(i).toString() + " : " + "has no expression\n");
					break;
				case NO_SAMPLE_AND_MATHEMATICA:
					logFileWriter.write(new Integer(i).toString() + " : " + "has no sample and expression\n");
					break;
				}
				logFileWriter.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		finalizeFunc();
	}
	
	public enum ParseResult {
		SAMPLE_AND_MATHEMATICA, FORMATERROR, READFAIL, NO_MATHEMATICA, NO_SAMPLE_AND_MATHEMATICA
    }
	
	public ParseResult parseFile(int fileID) {
		String fileFullName = Util.getIndexFromID(fileID);
		//String fileName = filePath + fileFullName;
		return parseFile(filePath, fileFullName);
	}
	
	public ParseResult parseFile(String filePath, String fileFullName) {
		File file = new File(filePath + fileFullName);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;    
            
            //------Get sample data------
            final String sampleMarker = "<table cellspacing=\"0\" cellpadding=\"2\" cellborder=\"0\">"; 
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
            final String startTag = "<tt>";
            final String endTag = "</tt>";
            int sampleStartIndex = tempString.indexOf(startTag);
            boolean hasSample = true;
            if (sampleStartIndex == -1) {
            	hasSample = false;
            } else {
            	sample = tempString.substring(sampleStartIndex + startTag.length()).trim();
                sample = sample.substring(0, sample.length() - endTag.length());
                //remove <wbr> tag. <wbr> appears when very large number exists in sample data
                sample = sample.replaceAll("<wbr>", "");
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
            String partOfExpression = null;
            String expression = "";
            while (tempString.compareTo(emptyStr) != 0) {
            	int expressionStartIndex = tempString.indexOf(startTag);
            	partOfExpression = tempString.substring(expressionStartIndex + startTag.length()).trim();
            	partOfExpression = partOfExpression.substring(0, partOfExpression.length() - endTag.length());
            	partOfExpression = SymbolReplacer.replaceSymbolForOneItem(partOfExpression);
            	partOfExpression = SymbolReplacer.replaceDataRangeForOneItem(partOfExpression);  
            	expression += partOfExpression;
            	/*By default, very expression ends up with (* XXX *). 
            	We use this pattern to deal with expressions that have multiple lines*/
            	if (partOfExpression.endsWith("*)")) {
            		Util.write(expressionFileWriter, fileFullName + ":" + expression + "\n");
            		expression = "";
            	}
            	 
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
