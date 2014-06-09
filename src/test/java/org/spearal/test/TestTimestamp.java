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
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestTimestamp extends AbstractSpearalTestUnit {

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
		encodeDecode(new Timestamp(new Date().getTime()), 13);
		encodeDecode(new Timestamp(0L), 13);
		encodeDecode(new Timestamp(Long.MAX_VALUE), 13);
	}
	
	private void encodeDecode(Timestamp value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Timestamp))
			Assert.fail("Not a Timestamp: " + clone);
		Assert.assertEquals(value, clone);
	}
}
