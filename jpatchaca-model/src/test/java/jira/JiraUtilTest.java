package jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import jira.exception.JiraException;
import jira.issue.JiraIssue;
import jira.issue.JiraIssueData;

public class JiraUtilTest {

	@Test
	public void humanFormatDouble() {
		assertEquals("1h 0m", JiraUtil.humanFormat(1D));
		assertEquals("25h 0m", JiraUtil.humanFormat(25D));
	}

	@Test
	public void humanFormatLong() {
		assertEquals("0h 1m", JiraUtil.humanFormat(60000L));
		assertEquals("1h 0m", JiraUtil.humanFormat(3600000L));
	}

	@Test
	public void getIssueDescription() {
		JiraIssueData data = new JiraIssueData();
		data.setKey("KEY");
		data.setSummary("SUMMARY");
		assertEquals("[KEY] SUMMARY", JiraUtil.getIssueDescription(new JiraIssue(data)));
	}

	@Test
	public void minutesFormatString() {
		assertEquals(Integer.valueOf(1), JiraUtil.minutesFormatString("0h 1m"));
		assertEquals(Integer.valueOf(60), JiraUtil.minutesFormatString("0h 60m"));
		assertEquals(Integer.valueOf(6500), JiraUtil.minutesFormatString("100h 500m"));
		assertEquals(Integer.valueOf(1060), JiraUtil.minutesFormatString("1h 1000m"));
		assertEquals(Integer.valueOf(1000), JiraUtil.minutesFormatString("0h 1000m"));
		assertEquals(Integer.valueOf(600000), JiraUtil.minutesFormatString("10000H 0m"));
	}

	@Test
	public void minutesFormatStringError() {
		minutesFormatStringError(null);
		minutesFormatStringError("");
		minutesFormatStringError(" ");
		minutesFormatStringError("0h");
		minutesFormatStringError("0m");
		minutesFormatStringError("h m");
		minutesFormatStringError("hm");
		minutesFormatStringError("0h  0m");
		minutesFormatStringError("0h0m");
		minutesFormatStringError("ah bm");
		minutesFormatStringError("0h -1m");
	}

	private void minutesFormatStringError(String time) {
		try {
			assertEquals(Integer.valueOf(1), JiraUtil.minutesFormatString(time));
			fail("Expect Format Invalid " + time);
		} catch (JiraException e) {
			assertEquals("Format Invalid " + time + " (correct format example 1h 30m)", e.getMessage());
		}
	}
}
