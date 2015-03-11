package com.formulacalculate;

import com.wolfram.jlink.KernelLink;

public class InterruptTimer extends Thread{
	private long milliseconds;
	private KernelLink kernelLink;

	public InterruptTimer(long milliseconds, KernelLink kernelLink) {
		// at least 1 seconds
		this.milliseconds = milliseconds > 0 ? milliseconds: 1000;
		this.kernelLink = kernelLink;
	}
	
	public void run() {
		try {
			sleep(milliseconds);
		} catch (InterruptedException e) {
			return;
		}
		kernelLink.abandonEvaluation();
	}
}
