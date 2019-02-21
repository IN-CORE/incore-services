/*******************************************************************************
 * Copyright (c) 2019 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Mozilla Public License v2.0 which accompanies this distribution,
 * and is available at https://www.mozilla.org/en-US/MPL/2.0/
 *******************************************************************************/
package ncsa.tools.common.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import ncsa.tools.common.NCSAConstants;

public class DateUtils
{
	private static Logger logger = Logger.getLogger(DateUtils.class);

	public static Date now()
	{
		return Calendar.getInstance().getTime();
	}

	public static Date setToTime(Date date, int hours, int minutes)
	{
		Calendar calendar = Calendar.getInstance();
		return calendar.getTime();
	}

	public static void setToTime(Calendar calendar, int hours, int minutes)
	{
		calendar.set(Calendar.HOUR_OF_DAY, hours);
		calendar.set(Calendar.MINUTE, minutes);
	}

	/**
	 * Calculates the number of days between two calendar days in a manner
	 * which is independent of the Calendar type used.
	 * 
	 * @param d1
	 *            The first date.
	 * @param d2
	 *            The second date.
	 * 
	 * @return The number of days between the two dates. Zero is
	 *         returned if the dates are the same, one if the dates are
	 *         adjacent, etc. The order of the dates
	 *         does not matter, the value returned is always >= 0.
	 *         If Calendar types of d1 and d2
	 *         are different, the result may not be accurate.
	 */
	public static int getDaysBetween(Calendar d1, Calendar d2)
	{
		// swap dates so that d1 is start and d2 is end
		if (d1.after(d2)) {
			Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}

		int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
		int y2 = d2.get(Calendar.YEAR);
		if (d1.get(Calendar.YEAR) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
				d1.add(Calendar.YEAR, 1);
			} while (d1.get(Calendar.YEAR) != y2);
		}
		return days;
	} // getDaysBetween()

	/**
	 * Calculates the number of minutes between two dates in a manner
	 * which is independent of the Calendar type used.
	 * 
	 * @param d1
	 *            The first date.
	 * @param d2
	 *            The second date.
	 * 
	 * @return The number of minutes between the two dates. Zero is
	 *         returned if the dates are the same, one if the dates are
	 *         adjacent, etc. The order of the dates
	 *         does not matter, the value returned is always >= 0.
	 *         If Calendar types of d1 and d2
	 *         are different, the result may not be accurate.
	 */
	public static double getMinutesBetween(Calendar d1, Calendar d2)
	{
		long d1Millis = d1.getTimeInMillis();
		long d2Millis = d2.getTimeInMillis();

		long diff = Math.abs(d1Millis - d2Millis);
		if (diff < 60000)
			return 0;

		return Math.ceil(diff / 60000);
	} // getMinutesBetween()

	/*
	 * /////////////////////////////////////////////////////////////////////////
	 * DATE & TIME //
	 * //////////////////////////////////////////////////////////////////////
	 */

	/**
	 * Uses current system time if timeString is <code>null</code>.
	 * 
	 * @param timeString
	 *            string representing time in milliseconds.
	 * @return corresponding Date object.
	 */
	public static Date getDateFromTimeString(String timeString)
	{
		long time = 0;
		if (timeString != null) {
			try {
				time = new Long(timeString).longValue();
			} catch (NumberFormatException nfe) {
			}
		} else {
			time = System.currentTimeMillis();
		}
		return new Date(time);
	} // getDateObject

	/**
	 * Uses current system time if millis < 0.
	 * 
	 * @param millis
	 *            time in milliseconds.
	 * @return corresponding Date object.
	 */
	public static Date getDate(long millis)
	{
		if (millis >= 0)
			return new Date(millis);
		return new Date(System.currentTimeMillis());
	} // getDateObject

	/**
	 * Uses current system time if dateTime is <code>null</code>.
	 * 
	 * @param dateTime
	 *            string representing date and time.
	 * @return corresponding wrapper object.
	 */
	public static Long getTimeInMillis(String dateTime)
	{
		if (dateTime != null) {
			Date d = getDateFromDateString(dateTime);
			return new Long(d.getTime());
		}
		return new Long(System.currentTimeMillis());
	} // getTimeInMillis

	/**
	 * Uses current system time if dateTime is <code>null</code>.
	 * 
	 * @param dateTime
	 *            string representing date and time.
	 * @param format
	 *            for dateTime string.
	 * @return corresponding wrapper object.
	 */
	public static Long getTimeInMillis(String dateTime, String format)
	{
		if (dateTime != null) {
			Date d = getDateFromDateString(dateTime, format);
			return new Long(d.getTime());
		}
		return new Long(System.currentTimeMillis());
	} // getTimeInMillis

	/**
	 * Uses current system time if millis < 0.
	 * 
	 * @param millis
	 *            time in milliseconds.
	 * @return corresponding wapper object.
	 */
	public static Long getTimeInMillis(long millis)
	{
		if (millis >= 0)
			return new Long(millis);
		return new Long(System.currentTimeMillis());
	} // getTimeInMillis

	/**
	 * Converts millisecond value to (default) formatted date string.
	 * 
	 * @param millis
	 *            date-time in milliseconds.
	 * @return formatted date-time.
	 */
	public static String getDateString(long millis)
	{
		DateFormat formatter = new SimpleDateFormat(NCSAConstants.DEFAULT_FORMAT);
		return getDateString(millis, formatter);
	} // getDateString

	public static String getDateString(long millis, String format)
	{
		DateFormat formatter = new SimpleDateFormat(format);
		return getDateString(millis, formatter);
	} // getDateString

	public static String getDateString(long millis, DateFormat formatter)
	{
		if (millis >= 0)
			return formatter.format(new Date(millis));
		return formatter.format(new Date(System.currentTimeMillis()));
	} // getDateString

	/**
	 * Converts Date object to String representation using the default formatter.
	 * 
	 * @param date
	 *            object.
	 * @return date-time string (yyyy/MM/dd HH:mm:ss).
	 */
	public static String getDateString(Date date)
	{
		DateFormat formatter = new SimpleDateFormat(NCSAConstants.DEFAULT_FORMAT);
		return formatter.format(date);
	} // getDateString

	/**
	 * Converts Date object to String representation using the given format
	 * string.
	 * 
	 * @param date
	 *            object.
	 * @param format
	 *            string initializer.
	 * @return date-time string.
	 */
	public static String getDateString(Date date, String format)
	{
		DateFormat formatter = new SimpleDateFormat(format);
		return formatter.format(date);
	} // dateTime

	/**
	 * Converts date-time String into Date object using the default formatter.
	 * 
	 * @param dateTime
	 *            string representation of date.
	 * @return date object.
	 */
	public static Date getDateFromDateString(String dateTime)
	{
		DateFormat formatter = new SimpleDateFormat(NCSAConstants.DEFAULT_FORMAT);
		try {
			return formatter.parse(dateTime);
		} catch (ParseException pe) {
			logger.warn("getDateFromDateString", pe);
			return null;
		}
	} // getDateFromDateString

	/**
	 * Converts date-time String into Date object using the default formatter.
	 * 
	 * @param dateTime
	 *            string representation of date.
	 * @param format
	 *            string initializer.
	 * @return date object.
	 */
	public static Date getDateFromDateString(String dateTime, String format)
	{
		DateFormat formatter = new SimpleDateFormat(format);
		try {
			return formatter.parse(dateTime);
		} catch (ParseException pe) {
			logger.warn("getDateFromDateString", pe);
			return null;
		}
	} // dateTime
}
