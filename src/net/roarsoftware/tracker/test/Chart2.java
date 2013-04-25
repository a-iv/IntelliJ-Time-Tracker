package net.roarsoftware.tracker.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Janni Kovacs
 */
public class Chart2 {

	public static void main(String[] args) {
		long time = 432 * 60 * 1000;
		DateFormat.getTimeInstance();
		System.out.println(DateFormat.getTimeInstance().format(new Date(time)));
		System.out.println(DateFormat.getTimeInstance(DateFormat.SHORT).format(time));
		System.out.println(new SimpleDateFormat("hh:mm").format(time));
	}
}
