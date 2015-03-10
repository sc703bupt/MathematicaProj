package com.formulacalculate;

import com.wolfram.jlink.KernelLink;

public class InterruptTimer extends Thread{
	private long milliseconds;
	private KernelLink kernelLink;
	private String currentCalculatingIndex;
	
	public InterruptTimer(long milliseconds, KernelLink kernelLink, String index) {
		// at least 1 seconds
		this.milliseconds = milliseconds > 0 ? milliseconds: 1000;
		this.kernelLink = kernelLink;
		this.currentCalculatingIndex = index;
	}
	
	public void run() {
		try {
			sleep(milliseconds);
		} catch (InterruptedException e) {
			return;
		}
		kernelLink.abandonEvaluation();
		System.out.println("[TIMEOUT]:" + currentCalculatingIndex);
	}
}
