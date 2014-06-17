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
package org.spearal.test;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestDateTime extends AbstractSpearalTestUnit {
	
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	@Before
	public void setUp() throws Exception {
		// printStream = System.out;
	}

	@After
	public void tearDown() throws Exception {
		printStream = NULL_PRINT_STREAM;
	}

	@Test
	public void test() throws IOException {
		encodeDecode(new Date(), -1);
		encodeDecode(new Date(0L), -1);
		encodeDecode(new Date(Long.MAX_VALUE), -1);

		encodeDecode(new java.sql.Date(new Date().getTime()), -1);
		encodeDecode(new java.sql.Date(0L), -1);
		encodeDecode(new java.sql.Date(Long.MAX_VALUE), -1);

		encodeDecode(new java.sql.Time(new Date().getTime()), -1);
		encodeDecode(new java.sql.Time(0L), -1);
		encodeDecode(new java.sql.Time(Long.MAX_VALUE), -1);

		encodeDecode(new java.sql.Timestamp(new Date().getTime()), -1);
		encodeDecode(new java.sql.Timestamp(0L), -1);
		encodeDecode(new java.sql.Timestamp(Long.MAX_VALUE), -1);
		java.sql.Timestamp ts = new java.sql.Timestamp(new Date().getTime());
		ts.setNanos(999999999);
		encodeDecode(ts, -1);
		
		encodeDecode(Calendar.getInstance(UTC), -1);
	}
	
	private void encodeDecode(Date value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Timestamp))
			Assert.fail("Not a Timestamp: " + clone);
		
		Timestamp dateTime = (Timestamp)clone;
		if (value instanceof java.sql.Timestamp)
			Assert.assertEquals(value, dateTime);
		else {
			Assert.assertEquals(toCalendar(value).getTime(), toCalendar(dateTime).getTime());
		}
	}
	
	private void encodeDecode(Calendar value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Timestamp))
			Assert.fail("Not a Timestamp: " + clone);
		
		Assert.assertEquals(value.getTime(), clone);
	}
	
	private static Calendar toCalendar(Date date) {
		Calendar calendar = Calendar.getInstance(UTC);
		calendar.setTime(date);
		if (date instanceof java.sql.Date) {
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		if (date instanceof java.sql.Time) {
			calendar.set(Calendar.YEAR, 1970);
			calendar.set(Calendar.MONTH, 0);
			calendar.set(Calendar.DATE, 1);
			calendar.set(Calendar.MILLISECOND, 0);
		}
		return calendar;
	}
}
