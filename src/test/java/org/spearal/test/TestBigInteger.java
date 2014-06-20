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
import java.math.BigInteger;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestBigInteger extends AbstractSpearalTestUnit {

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
		encodeDecode(newBigInteger(8000, true), 4004);
		encodeDecode(newBigInteger(256, true), 131);
		
		encodeDecode(BigInteger.valueOf(Long.MIN_VALUE).subtract(BigInteger.ONE), 11);
		
		encodeDecode(BigInteger.valueOf(Long.MIN_VALUE), 9);
		encodeDecode(BigInteger.valueOf(Long.MIN_VALUE + 1), 9);
		encodeDecode(BigInteger.TEN.negate(), 2);
		encodeDecode(BigInteger.ONE.negate(), 2);
		encodeDecode(BigInteger.ZERO.negate(), 2);
		encodeDecode(BigInteger.ZERO, 2);
		encodeDecode(BigInteger.ONE, 2);
		encodeDecode(BigInteger.TEN, 2);
		encodeDecode(BigInteger.valueOf(Long.MAX_VALUE - 1), 9);
		encodeDecode(BigInteger.valueOf(Long.MAX_VALUE), 9);
		
		encodeDecode(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE), 11);

		encodeDecode(newBigInteger(256, false), 131);
		encodeDecode(newBigInteger(8000, false), 4004);
	}
	
	private static BigInteger newBigInteger(int length, boolean negate) {
		char[] chars = new char[length];
		Arrays.fill(chars, 'f');
		BigInteger value = new BigInteger(String.valueOf(chars), 16);
		return (negate ? value.negate() : value);
	}
	
	private void encodeDecode(BigInteger value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		BigInteger clone = decode(data, BigInteger.class);
		
		if (expectedSize > 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(value, clone);
	}
}
