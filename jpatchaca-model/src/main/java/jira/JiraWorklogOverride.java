package jira;

import static jira.JiraUtil.humanFormat;
import static jira.JiraUtil.minutesFormatString;
import static jira.JiraUtil.minutesFormatStringValidate;

import java.util.HashMap;
import java.util.Map;

import periods.Period;

public class JiraWorklogOverride {
	
	private Map<Period, String> overrides = new HashMap<Period, String>();

	public void overrideTimeSpentForPeriod(String timeSpent, Period period) {
		minutesFormatStringValidate(timeSpent);
		overrides.put(period, timeSpent);
	}
	
	public String getHumanFormatDuration(Period period) {
		if (overrides.containsKey(period))
			return overrides.get(period);
		
		return humanFormat(period.totalMilliseconds()).trim();		
	}

	public int getMinutesDuration(Period period) {
		if (overrides.containsKey(period))
			return minutesFormatString(overrides.get(period));
		
		return period.totalMinutes();
	}
}
