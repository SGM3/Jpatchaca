package jira;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;

import jira.exception.JiraException;
import jira.issue.JiraIssue;

public class JiraUtil {

	public static final String TIME_SPENT_PATTERN = "[0-9]+h [0-9]+m";

	public static String humanFormat(final double hours) {
		final int truncatedHours = ((int) hours);
		final int minutes = ((int) ((hours - truncatedHours) * 60));
		return truncatedHours + "h " + minutes + "m";
	}

	public static String humanFormat(final long millis) {
		return humanFormat((double) millis / DateUtils.MILLIS_PER_HOUR);
	}

	public static String getIssueDescription(JiraIssue issue) {
		Validate.notNull(issue);
		return String.format("[%s] %s", issue.getKey(), issue.getSummary());
	}

	public static Integer minutesFormatString(String time) {
		minutesFormatStringValidate(time);
		
		String[] hourMinute = time.trim().toLowerCase().split(" ");
		Integer hour = Integer.valueOf(hourMinute[0].replaceAll("h", ""));
		Integer minute = Integer.valueOf(hourMinute[1].replaceAll("m", ""));
		
		return (hour * 60) + minute;
	}

	public static void minutesFormatStringValidate(String time) {
		if (time == null || !time.trim().toLowerCase().matches(TIME_SPENT_PATTERN))
			throw new JiraException("Format Invalid " + time + " (correct format example 1h 30m)");
	}
}
