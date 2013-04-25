package net.roarsoftware.tracker.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Janni Kovacs
 */
public class TimeFormatter {

	private static final int ONE_DAY = 86400000;
	private static final int ONE_HOUR = 3600000;

	private static DateFormat formatMinutes = new SimpleDateFormat("mm:ss");
	private static DateFormat formatHours = DateFormat.getTimeInstance();
	private static DateFormat dateFormat = DateFormat.getDateInstance();
	private static DateFormat dateFormatShort = new SimpleDateFormat("dd.MM");

	static {
		TimeZone tz = TimeZone.getTimeZone("GMT");
		formatHours.setTimeZone(tz);
		formatMinutes.setTimeZone(tz);
	}

	private TimeFormatter() {
	}

	public static String format(long time) {
		if (time < ONE_HOUR) { // 1 hour
			return formatMinutes.format(new Date(time));
		} else if (time > ONE_DAY) { // 1 day
			int days = (int) (time / ONE_DAY);
			long rest = time % ONE_DAY;
			return days + " days " + formatHours.format(new Date(rest));
		} else {
			return formatHours.format(new Date(time));
		}
	}

	public static long parse(String s) {
		try {
			return formatHours.parse(s).getTime();
		} catch (ParseException e) {
			return -1;
		}
	}

	public static String formatDate(Date date) {
		return dateFormat.format(date);
	}

	public static String formatDateShort(Date date) {
		return dateFormatShort.format(date);
	}
}
