package nl.tue.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Time is measured throughout the game in milliseconds since 1970.
 * This library provides some convenience functions for handling the time.
 * Timezones are ignored. 
 *
 */
public class TimeUtils {

	public static final long MINUTES_025 = 150000;
	public static final long MINUTES_05 = 300000;
	public static final long MINUTES_075 = 450000;
	public static final long MINUTES_10 = 600000;
	public static final long MINUTES_15 = 900000;
	public static final long MINUTES_20 = 120000;
	public static final long MINUTES_25 = 1500000;
	public static final long MINUTES_30 = 1800000;
	public static final long HOUR = 3600000;
	public static final long DAY = 86400000;
	
	public static double simulationTimeInHours(long simulationTime) {
		return ((double)simulationTime)/3600000.0;
	}

	public static double simulationTimeInHours(double simulationTime) {
		return ((double)simulationTime)/3600000.0;
	}

	/**
	 * Returns tomorrow at 00:00 in milliseconds since 1 jan 1970. 
	 * 
	 * @return milliseconds since 1 jan 1970
	 */
	public static long tomorrow() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		cal.add(Calendar.DATE, 1);
				
		return cal.getTimeInMillis();
	}

	/**
	 * Returns today at 00:00 in milliseconds since 1 jan 1970. 
	 * 
	 * @return milliseconds since 1 jan 1970
	 */
	public static long today() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
				
		return cal.getTimeInMillis();
	}

	/**
	 * Returns yesterday at 00:00 in milliseconds since 1 jan 1970. 
	 * 
	 * @return milliseconds since 1 jan 1970
	 */
	public static long yesterday() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		
		cal.add(Calendar.DATE, -1);
				
		return cal.getTimeInMillis();
	}

	/**
	 * Takes a date formatted as yyyy/MM/dd and returns it in milliseconds since 1970
	 * 
	 * @param formattedDate a date formatted as yyyy/MM/dd 
	 * @return milliseconds since 1970
	 */
	public static Long parseDate(String formattedDate) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			return format.parse(formattedDate).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}    
	}

	/**
	 * Takes a datetime formatted as yyyy/MM/dd HH:mm and returns it in milliseconds since 1970
	 * 
	 * @param formattedDateTime a date formatted as yyyy/MM/dd HH:mm 
	 * @return milliseconds since 1970
	 */
	public static Long parseDateTime(String formattedDateTime) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));

		try {
			return format.parse(formattedDateTime).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}    
	}

	/**
	 * Formats the given time (in milliseconds since 1970). 
	 * 
	 * @param time time in milliseconds
	 * @return text
	 */
	public static String formatTime(long time) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));		

		cal.setTimeInMillis(time);
		return dateFormat.format(cal.getTime());
	}

	/**
	 * Formats the given date (in milliseconds since 1970). 
	 * 
	 * @param date time in milliseconds
	 * @return text
	 */
	public static String formatDate(long date) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));		

		cal.setTimeInMillis(date);
		return dateFormat.format(cal.getTime());
	}

	/**
	 * Returns the current time at the locale in (in milliseconds since 1970).
	 * 
	 * @return time in milliseconds
	 */
	public static long currentTime() {
		Calendar cal = Calendar.getInstance();
		long offset = TimeZone.getDefault().getRawOffset() + TimeZone.getDefault().getDSTSavings();
		return cal.getTimeInMillis() + offset;
	}

}
