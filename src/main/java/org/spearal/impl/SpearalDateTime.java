/**
 * == @Spearal ==>
 * 
 * Copyright (C) 2014 Franck WOLFF & William DRAI (http://www.spearal.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spearal.impl;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Franck WOLFF
 */
public class SpearalDateTime {
	
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	
	public final boolean hasDate;
	public final boolean hasTime;
	
	public final int year;
	public final int month;
	public final int date;
	public final int hours;
	public final int minutes;
	public final int seconds;
	public final int nanoseconds;
	
	public static SpearalDateTime forDate(int year, int month, int date) {
		return new SpearalDateTime(year, month, date, 0, 0, 0, 0, true, false);
	}
	
	public static SpearalDateTime forTime(int hours, int minutes, int seconds) {
		return new SpearalDateTime(0, 0, 0, hours, minutes, seconds, 0, false, true);
	}
	
	public static SpearalDateTime forTime(int hours, int minutes, int seconds, int nanoseconds) {
		return new SpearalDateTime(0, 0, 0, hours, minutes, seconds, nanoseconds, false, true);
	}
	
	public static SpearalDateTime forDate(Date date) {
		GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
		calendar.setTimeInMillis(date.getTime());
		return new SpearalDateTime(
			calendar.get(Calendar.YEAR),
			calendar.get(Calendar.MONTH) + 1,
			calendar.get(Calendar.DATE),
			calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.SECOND),
			calendar.get(Calendar.MILLISECOND) * 1000000,
			true, true
		);
	}
	
	public static SpearalDateTime forSQLDate(java.sql.Date date) {
		GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
		calendar.setTimeInMillis(date.getTime());
		return new SpearalDateTime(
			calendar.get(Calendar.YEAR),
			calendar.get(Calendar.MONTH) + 1,
			calendar.get(Calendar.DATE),
			0, 0, 0, 0, true, false
		);
	}
	
	public static SpearalDateTime forSQLTime(Time time) {
		GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
		calendar.setTimeInMillis(time.getTime());
		return new SpearalDateTime(
			0, 0, 0,
			calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.SECOND),
			0, false, true
		);
	}
	
	public static SpearalDateTime forSQLTimestamp(Timestamp timestamp) {
		GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
		calendar.setTimeInMillis(timestamp.getTime());
		return new SpearalDateTime(
			calendar.get(Calendar.YEAR),
			calendar.get(Calendar.MONTH) + 1,
			calendar.get(Calendar.DATE),
			calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.SECOND),
			timestamp.getNanos(),
			true, true
		);
	}
	
	public static SpearalDateTime forCalendar(Calendar calendar) {
		if (!(calendar instanceof GregorianCalendar)) {
			calendar = new GregorianCalendar(UTC, Locale.US);
			calendar.setTimeInMillis(calendar.getTimeInMillis());
		}
		return forGregorianCalendar((GregorianCalendar)calendar);
	}
	
	public static SpearalDateTime forGregorianCalendar(GregorianCalendar calendar) {
		calendar.setTimeZone(UTC);

		int year = calendar.get(Calendar.YEAR);
		if (calendar.get(Calendar.ERA) == GregorianCalendar.BC)
			year = -year;
		
		return new SpearalDateTime(
			year,
			calendar.get(Calendar.MONTH) + 1,
			calendar.get(Calendar.DATE),
			calendar.get(Calendar.HOUR_OF_DAY),
			calendar.get(Calendar.MINUTE),
			calendar.get(Calendar.SECOND),
			calendar.get(Calendar.MILLISECOND) * 1000000,
			true, true
		);
	}
	
	@SuppressWarnings("boxing")
	public SpearalDateTime(
		int year, int month, int date,
		int hours, int minutes, int seconds, int nanoseconds,
		boolean hasDate, boolean hasTime) {
		
		if (hasDate) {
			if (year < -999999999 || year > 999999999)
				throw new IllegalArgumentException("Illegal year: " + year);
			if (month < 1 || month > 12)
				throw new IllegalArgumentException("Illegal month: " + month);
			if (date < 1 || date > 31)
				throw new IllegalArgumentException("Illegal date: " + date);
		}
		else {
			year = 0;
			month = 0;
			date = 0;
		}
		
		if (hasTime) {
			if (hours < 0 || hours > 23) {
				if (hours == 24 && !(minutes == 0 && seconds == 0 && nanoseconds == 0))
					throw new IllegalArgumentException(String.format("Illegal time: %02d:%02d:%02d.%09d", hours, minutes, seconds, nanoseconds));
				throw new IllegalArgumentException("Illegal hours: " + hours);
			}
			if (minutes < 0 || minutes > 59)
				throw new IllegalArgumentException("Illegal minutes: " + minutes);
			if (seconds < 0 || seconds > 59)
				throw new IllegalArgumentException("Illegal seconds: " + seconds);
			if (nanoseconds < 0 || nanoseconds > 999999999)
				throw new IllegalArgumentException("Illegal nanoseconds: " + nanoseconds);
		}
		else {
			hours = 0;
			minutes = 0;
			seconds = 0;
			nanoseconds = 0;
		}
		
		this.hasDate = hasDate;
		this.hasTime = hasTime;
		
		this.year = year;
		this.month = month;
		this.date = date;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.nanoseconds = nanoseconds;
	}
	
	public GregorianCalendar toGregorianCalendar() {
		GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
		
		calendar.clear();

		if (hasDate) {
			int year = this.year;
			if (year < 0) {
				year = -year;
				calendar.set(Calendar.ERA, GregorianCalendar.BC);
			}
			calendar.set(Calendar.YEAR, year);
			calendar.set(Calendar.MONTH, month - 1);
			calendar.set(Calendar.DATE, date);
		}
		
		if (hasTime) {
			calendar.set(Calendar.HOUR_OF_DAY, hours);
			calendar.set(Calendar.MINUTE, minutes);
			calendar.set(Calendar.SECOND, seconds);
			calendar.set(Calendar.MILLISECOND, nanoseconds / 1000000);
		}
		
		return calendar;
	}
	
	public Date toDate() {
		return toGregorianCalendar().getTime();
	}
	
	public java.sql.Date toSQLDate() {
		return new java.sql.Date(toGregorianCalendar().getTime().getTime());
	}
	
	public Time toSQLTime() {
		return new Time(toGregorianCalendar().getTime().getTime());
	}
	
	public Timestamp toSQLTimestamp() {
		Timestamp timestamp = new Timestamp(toGregorianCalendar().getTime().getTime());
		timestamp.setNanos(nanoseconds);
		return timestamp;
	}

	@SuppressWarnings("boxing")
	@Override
	public String toString() {
		return String.format(
			"%+010d-%02d-%02dT%02d:%02d:%02d.%09dZ",
			year, month, date,
			hours, minutes, seconds, nanoseconds
		);
	}
}
