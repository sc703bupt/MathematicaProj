package com.rawdataprocess;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.io.*;

import com.config.Config;
import com.util.Util;


public class FileFetcher extends Thread{
	private int startID;
	private int endID;
	private Semaphore semp;
	private ArrayList<Integer> failedFileList;
	public FileFetcher(int startID, int endID, Semaphore semp) {
		this.startID = startID;
		this.endID = endID;
		this.semp = semp;
		this.failedFileList = new ArrayList<Integer>();
	}
	
	public void run() {
		try {
			semp.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		fetchAllFiles();
		semp.release();
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
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
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
		System.out.println("Start to fetch files from " + startID + " to " + endID);
		File logFile = null;
		FileWriter fw = null;
		try {			
			logFile = new File(Config.getAttri("WEB_PAGE_SAVE_PATH_PREFIX") + 
					"log_" + new Integer(startID) + "_" + new Integer(endID));
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			fw = new FileWriter(logFile, true);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		for (int i = startID; i <= endID; i++){
			String fileName = Util.getIndexFromID(i);
			String httpUrl = Config.getAttri("OEIS_URL_PREFIX") + fileName;
			String savedFilePath = Config.getAttri("WEB_PAGE_SAVE_PATH_PREFIX")+ fileName;
			boolean isGood = FileFetcher.httpDownload(httpUrl, savedFilePath);

			try {
				if(isGood) {
					fw.write("Download "+ fileName + " successfully\n");
					fw.flush();
				} else {
					failedFileList.add(i);
					fw.write("Download "+ fileName + " failed\n");
					System.out.println("Download "+ fileName + " failed");	
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
		
		if (failedFileList.isEmpty()) {
			System.out.println("No exception when fetching files from " + startID + " to " + endID);
		} else {
			fetchFailedFiles();
		}
	}

	private void fetchFailedFiles() {
		// TODO Auto-generated method stub
		System.out.println("Start to fetch failed files between " + startID + " and " + endID);
		File logFile = null;
		FileWriter fw = null;
		try {			
			logFile = new File(Config.getAttri("WEB_PAGE_SAVE_PATH_PREFIX") + 
					"log_retry_" + new Integer(startID) + "_" + new Integer(endID));
			if (!logFile.exists()) {
				logFile.createNewFile();
			}
			
			fw = new FileWriter(logFile, true);
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		
		for (int i = 0; i < failedFileList.size(); i++){
			String fileName = Util.getIndexFromID(failedFileList.get(i));
			String httpUrl = Config.getAttri("OEIS_URL_PREFIX") + fileName;
			String savedFilePath = Config.getAttri("WEB_PAGE_SAVE_PATH_PREFIX")+ fileName;
			boolean isGood = FileFetcher.httpDownload(httpUrl, savedFilePath);
			try {
				if(isGood) {
					fw.write("Retry to download "+ fileName + " successfully\n");
					fw.flush();
				} else {
					System.out.println("Retry to download "+ fileName + " failed");
					fw.write("Retry to download "+ fileName + " failed\n");
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
