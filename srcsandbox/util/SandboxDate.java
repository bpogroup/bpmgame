package util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.tue.util.TimeUtils;

public class SandboxDate {

	public static void main(String[] args) {		
		System.out.println(TimeUtils.tomorrow());
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(TimeUtils.tomorrow());
		System.out.println("Current Date Time : " + dateFormat.format(cal.getTime()));
	}

}
