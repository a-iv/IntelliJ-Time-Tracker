package net.roarsoftware.tracker.core.filters;

import net.roarsoftware.tracker.model.Day;
import net.roarsoftware.tracker.model.Task;

/**
 * A task filter that accepts all tasks that started or ended in the given time frame (inclusive start and end date).
 * The Filter also accepts Tasks lying only partially in the given time frame.
 * For these tasks applications such as the report generator may only consider work done in the given time frame.
 *
 * @author Janni Kovacs
 */
public class DateFilter implements TaskFilter {

	private Day start, end;

	public DateFilter(Day start, Day end) {
		this.start = start;
		this.end = end;
	}

	public Day getEnd() {
		return end;
	}

	public void setEnd(Day end) {
		this.end = end;
	}

	public Day getStart() {
		return start;
	}

	public void setStart(Day start) {
		this.start = start;
	}

	public boolean contains(Day day) {
		return (start.before(day) || start.sameDay(day)) && (end.after(day) || end.sameDay(day));
	}

	public boolean accept(Task t) {
		/**
		 * wenn finished == null dann so als ob finished = today
		 * alle möglichkeiten:
		 * - started == null
		 * - started < begin, finished < begin
		 * started < begin, begin < finished < end
		 * started < begin finished > end
		 */
		Day started = t.getDateStarted();
		Day finished = t.getDateFinished();

		if (started == null) // wenn der task noch nicht gestarted wurde wird er nicht akzeptiert
			return false;
		if(contains(started)) // wenn das startdatum im filter range liegt wird er akzeptiert
			return true;
		if(finished != null && contains(finished)) // wenn das enddatum im filter range liegt wird akzeptiert
			return true;
		if (started.before(start)) { // wenn das startdatum vor dem filterrange liegt
			if (finished == null) // und der task noch nicht beendet ist, akzeptiert
				return true;
			else if (finished.after(start) || finished.sameDay(start)) // oder das enddatum in oder nach dem range liegt, akzeptiert
				return true;
		}
		return false;
	}

}
