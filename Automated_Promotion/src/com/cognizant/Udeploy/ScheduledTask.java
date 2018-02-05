package com.cognizant.Udeploy;
import java.util.TimerTask;
import java.util.Date;

public class ScheduledTask extends TimerTask{
	Date now; // to display current time

	// Add your task here
	public void run() {
		now = new Date(); // initialize date
		//System.out.println("Time is :" + now); // Display current time
	}
}
