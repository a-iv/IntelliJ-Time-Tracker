package net.roarsoftware.tracker.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import net.roarsoftware.tracker.ui.TimeFormatter;

/**
 * Represents a Day and provides utility methods.
 *
 * @author Janni Kovacs
 */
public class Day implements Comparable<Day> {

	private Calendar date;

	public Day() {
		this(new Date());
	}

	public Day(Date d) {
		date = new GregorianCalendar();
		date.setTime(d);
		date.set(Calendar.MILLISECOND, 0);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MINUTE, 0);
		date.set(Calendar.HOUR_OF_DAY, 0);
	}

	public Day(long time) {
		date = new GregorianCalendar();
		date.setTimeInMillis(time);
	}

	public long getTime() {
		return date.getTimeInMillis();
	}


	/**
	 * Returns true if this Day is before the parameter.
	 *
	 * @param day
	 * @return
	 */
	public boolean before(Day day) {
		return getTime() < day.getTime();
	}

	/**
	 * Returns true if this Day is after the parameter.
	 * @param day
	 * @return
	 */
	public boolean after(Day day) {
		return getTime() > day.getTime();
	}

	public boolean sameDay(Day day) {
		return day.getTime() == getTime();
	}

	public boolean isToday() {
		return sameDay(new Day());
	}

	public static Day today() {
		return new Day();
	}

	public Date getDate() {
		return date.getTime();
	}

	/**
	 * Returns the total number of days covered in the range between this day (inclusive) and the passed day (inclusive).
	 * The returned number is always at least 1 (if this.sameDay(day))
	 * @param day
	 * @return
	 */
	public int totalDays(Day day) {
		return 1 + (int) ((Math.abs(getTime() - day.getTime())) / (24 * 60 * 60 * 1000));
	}

	/**
	 * Returns if both days are the same.
	 * @param o
	 * @return
	 */
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Day day = (Day) o;
		return this.sameDay(day);
	}

	public int hashCode() {
		return date.hashCode();
	}

	// test methods:

	public Day yesterday() {
		return move(-1);
	}

	public Day move(int num) {
		Calendar c = new GregorianCalendar();
		c.setTime(date.getTime());
		c.roll(Calendar.DAY_OF_YEAR, num);
		return new Day(c.getTime());
	}

	@Override
	public String toString() {
//		return getDate().toString();
		return TimeFormatter.formatDateShort(getDate());
	}

	public int compareTo(Day o) {
		return date.compareTo(o.date);
	}

}
