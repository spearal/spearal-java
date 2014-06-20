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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestBigDecimal extends AbstractSpearalTestUnit {

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
		encodeDecode(newBigDecimal(8000, 256, true), 4007);
		encodeDecode(newBigDecimal(256, 256, true), 134);
		
		encodeDecode(new BigDecimal(Double.toString(-Double.MAX_VALUE)), 12);
		
		encodeDecode(BigDecimal.TEN.negate(), 5);
		encodeDecode(BigDecimal.ONE.negate(), 5);
		encodeDecode(BigDecimal.ZERO.negate(), 5);
		encodeDecode(BigDecimal.ZERO, 5);
		encodeDecode(BigDecimal.ONE, 5);
		encodeDecode(BigDecimal.TEN, 5);

		encodeDecode(new BigDecimal(Double.toString(Double.MAX_VALUE)), 12);

		encodeDecode(newBigDecimal(256, 256, false), 134);
		encodeDecode(newBigDecimal(8000, 256, false), 4007);
	}

	private static BigDecimal newBigDecimal(int length, int scale, boolean negate) {
		char[] chars = new char[length];
		Arrays.fill(chars, 'f');
		BigInteger value = new BigInteger(String.valueOf(chars), 16);
		if (negate)
			value = value.negate();
		return new BigDecimal(value, scale);
	}
	
	private void encodeDecode(BigDecimal value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		BigDecimal clone = decode(data, BigDecimal.class);
		
		if (expectedSize > 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(value, clone);
	}
}
