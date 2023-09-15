package nl.tue.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class TestTimeUtils {
	long twoEightteen = (2018 - 1970)*365*24*60*60*1000l;
	long twoNineteen = (2019 - 1970)*365*24*60*60*1000l;
	long aDay = 24*60*60*1000l;
	long anHour = 60*60*1000;
	long aMinute = 60*1000;
	long aSecond = 1000;

	@Test
	public void testTomorrow() {
		long tomorrow = TimeUtils.tomorrow();
		assertTrue(tomorrow > twoEightteen);
		assertTrue(tomorrow - System.currentTimeMillis() <= aDay);
		assertTrue(tomorrow % aDay == 0);
		
		String formattedTime = TimeUtils.formatTime(tomorrow);
		System.out.println(formattedTime);
		assertTrue(formattedTime.substring(formattedTime.length()-8).equals("00:00:00"));
	}

	@Test
	public void testAsTime() {
		long aDate = TimeUtils.parseDate("2018/08/16");
		
		assertTrue(aDate > twoEightteen);
		assertTrue(aDate < twoNineteen);
		assertTrue(aDate % aDay == 0);
	}

	@Test
	public void testFormatTime() {
		long aTime = TimeUtils.parseDate("2018/08/16");
		aTime += 13*anHour;
		aTime += 12*aMinute;
		aTime += 45*aSecond;

		String formattedTime = TimeUtils.formatTime(aTime);
		System.out.println(formattedTime);
		assertTrue(formattedTime.equals("2018/08/16 13:12:45"));
	}

	@Test
	public void testCurrentTime() {
		System.out.println("Current: " + TimeUtils.formatTime(TimeUtils.currentTime()));
	}

	@Test
	public void testToday() {
		System.out.println("Today 00:00: " + TimeUtils.formatTime(TimeUtils.today()));
	}

	@Test
	public void testYesterday() {
		System.out.println("Yesterday 00:00: " + TimeUtils.formatTime(TimeUtils.yesterday()));
	}
}
