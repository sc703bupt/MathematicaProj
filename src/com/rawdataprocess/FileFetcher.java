package com.rawdataprocess;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.*;

import com.util.Constant;
import com.util.Util;


public class FileFetcher extends Thread{
	private int startID;
	private int endID;
	FileFetcher(int startID, int endID) {
		this.startID = startID;
		this.endID = endID;
	}
	
	public void run() {
		fetchAllFiles();
	}
	
	public static boolean httpDownload(String httpUrl,String saveFile) {
        int byteread = 0;

        URL url = null;
		try {
			url = new URL(httpUrl);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return false;
		}

        try {
            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(saveFile);

            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                fs.write(buffer, 0, byteread);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
	
	public void fetchAllFiles() {		

		File logFile = null;
		FileWriter fw = null;
		try {
			logFile = new File(Constant.WEB_PAGE_SAVE_PATH_PREFIX + new Integer(startID/10000).toString());
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			fw = new FileWriter(logFile, true);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		for (int i = startID; i <= endID; i++){
			String fileName = Util.getIndexFromID(i);
			String httpUrl = Constant.OEIS_URL_PREFIX + fileName;
			String savedFilePath = Constant.WEB_PAGE_SAVE_PATH_PREFIX + fileName;
			boolean isGood = FileFetcher.httpDownload(httpUrl, savedFilePath);
			try {
				if(isGood) {
					fw.write("Download "+ fileName + " successfully\n");
					fw.flush();
				} else {
					fw.write("Download "+ fileName + " failed\n");
					fw.flush();
				}
			} catch (IOException e) {
				System.out.println(e.toString());
			}
		}
		try {
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
