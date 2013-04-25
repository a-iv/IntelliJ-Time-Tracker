package net.roarsoftware.tracker.test;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * @author Janni Kovacs
 */
public class TaskTest {
	public static void main(String[] args) {
		Task t = new Task("wambo", null, null, null);
		Day today = Day.today();
		t.addDuration(today.move(-3), 1000);
		t.addDuration(today.move(-2), 2000);
		t.addDuration(today.move(-1), 8000);
		t.addDuration(today, 5000);
		System.out.println(today.move(-1));
		System.out.println(today);
		System.out.println("t.getTotalDuration() = " + t.getTotalDuration());
		System.out.println("t.getDurationInBetween(today, today) = " + t.getDurationInBetween(today.move(-12), today.move(30)));
	}
}
