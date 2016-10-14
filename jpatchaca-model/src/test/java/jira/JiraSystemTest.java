package jira;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.joda.time.DateTime;
import org.junit.Test;

import jira.exception.JiraException;
import jira.service.JiraMock;
import periods.Period;
import tasks.persistence.MockEventsConsumer;
import tasks.tasks.Tasks;

public class JiraSystemTest {

	@Test
	public void testAddWorklog(){
		Period period = createPeriod("0h 60m");
		addWorklogFor(period);
		assertEquals(Integer.valueOf(60), timeLogged());
	}

	@Test
	public void testAddWorklogWithOverride(){
		Period period = createPeriod("1h 0m");
		overrideWorkLog(period, "2h 0m");
		addWorklogFor(period);
		assertEquals(Integer.valueOf(120), timeLogged());
	}
	
	@Test
	public void testAddWorklogOverrideWrongFormat(){
		Period period = createPeriod("1h 0m");
		
		try {
			overrideWorkLog(period, "1:30");
			fail("Expect Format Invalid 1:30 (correct format example 1h 30m)");
		} catch (JiraException e) {
			assertEquals("Format Invalid 1:30 (correct format example 1h 30m)", e.getMessage());
		}
	}
	
	@Test
	public void testAddWorklogZeroMinutes(){
		Period period = createPeriod("1h 0m");
		overrideWorkLog(period, "0h 0m");
		
		try {
			addWorklogFor(period);
			fail("Expect Period Invalid For Issue key 0h 0m");
		} catch (JiraException e) {
			assertEquals("Period Invalid For Issue key 0h 0m", e.getMessage());
		}
	}

	private final JiraMock jira = new JiraMock();
	private final JiraWorklogOverride  jiraWorklogOverride = new JiraWorklogOverride(); 
	private final JiraSystemImpl jiraSystem = new JiraSystemImpl(jira, new MockEventsConsumer(), new Tasks(), jiraWorklogOverride);

	private void overrideWorkLog(Period period, String timeSpent) {
		jiraWorklogOverride.overrideTimeSpentForPeriod(timeSpent, period);
	}
	
	private Integer timeLogged() {
		return jira.timeLoggedFor("key");
	}

	private void addWorklogFor(Period period) {
		jiraSystem.logWorkOnIssue(period, "key");
	}

	private Period createPeriod(String time) {
		DateTime startTime = new DateTime();
		DateTime endTime = startTime.plusMinutes(JiraUtil.minutesFormatString(time));		
		return new Period(startTime.toDate(), endTime.toDate());
	}
}
