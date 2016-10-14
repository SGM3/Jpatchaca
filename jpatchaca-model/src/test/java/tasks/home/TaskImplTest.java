package tasks.home;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import basic.SystemClock;
import basic.SystemClockImpl;
import periods.impl.PeriodManagerImpl;
import periods.impl.PeriodsFactoryImpl;
import tasks.tasks.tests.MockTaskName;

public class TaskImplTest {

	private SystemClock systemClock;
	private TaskImpl task;

	@Before
	public void setup() {
		systemClock = new SystemClockImpl();
		task = new TaskImpl(new MockTaskName("foo"), systemClock, 0.0, new PeriodManagerImpl(), new PeriodsFactoryImpl());
	}

	@Test
	public void testStopTask() {
		setTime(0l);
		task.start();
		setTime(60000l);
		task.stop();

		assertEquals(60000l, task.totalTimeInMillis());
	}

	@Test
	public void testStopBeforeStarted() {
		setTime(0l);
		task.start();
		setTime(42l);
		task.stop(43l);

		assertEquals(0l, task.totalTimeInMillis());
	}

	private void setTime(final long time) {
		systemClock.setTime(time);
	}
}
