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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestDouble extends AbstractSpearalTestUnit {

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

		// -- Various double values --
		
		encodeDecode(Double.NEGATIVE_INFINITY, 9);
		encodeDecode(-Double.MAX_VALUE, 9);
		encodeDecode(-Math.PI, 9);
		encodeDecode(-Math.E, 9);
		encodeDecode(-Double.MIN_VALUE, 9);
		encodeDecode(-0.0, 9);
		encodeDecode(Double.NaN, 9);
		encodeDecode(0.0, 2);
		encodeDecode(Double.MIN_VALUE, 9);
		encodeDecode(Math.E, 9);
		encodeDecode(Math.PI, 9);
		encodeDecode(Double.MAX_VALUE, 9);
		encodeDecode(Double.POSITIVE_INFINITY, 9);
		
		// -- Integral values encoded as Long --
		
		for (long i = 0x01L; i <= 0xFFL; i++) {
			encodeDecode(-i, 2);
			encodeDecode(i, 2);
		}
		
		long min = 0x100L;
		long max = 0xFFFFL;
		for (int i = 3; i < 8; i++) {
			encodeDecode(-min, i);
			encodeDecode(min, i);
			
			encodeDecode(-max, i);
			encodeDecode(max, i);
			
			min = (min << 8);
			max = (max << 8) | 0xFFL;
		}
		
		encodeDecode(-0x000FFFFFFFFFFFFFL, 8);
		encodeDecode( 0x000FFFFFFFFFFFFFL, 8);
		
		// -- Integral values as Double --
		
		encodeDecode(-0x0010000000000000L, 9);
		encodeDecode( 0x0010000000000000L, 9);
		
		encodeDecode(-0x00FFFFFFFFFFFFFFL, 9);
		encodeDecode( 0x00FFFFFFFFFFFFFFL, 9);
		
		encodeDecode(-0x0FFFFFFFFFFFFFFFL, 9);
		encodeDecode( 0x0FFFFFFFFFFFFFFFL, 9);

		encodeDecode(Long.MIN_VALUE, 9);
		encodeDecode(Long.MAX_VALUE, 9);
		
		// -- Values with 4 decimals max, variable integral length --
		
		encodeDecode(-0.1, 2);
		encodeDecode( 0.1, 2);
		
		encodeDecode(-0.2, 2);
		encodeDecode( 0.2, 2);
		
		encodeDecode(-0.255, 2);
		encodeDecode( 0.255, 2);
		
		encodeDecode(-0.256, 3);
		encodeDecode( 0.256, 3);
		
		encodeDecode(-0.3, 3);
		encodeDecode( 0.3, 3);
		
		encodeDecode(-0.4, 3);
		encodeDecode( 0.4, 3);
		
		encodeDecode(-0.9, 3);
		encodeDecode( 0.9, 3);
		
		encodeDecode(-0.999, 3);
		encodeDecode( 0.999, 3);
		
		encodeDecode(-4294967.295, 5);
		encodeDecode( 4294967.295, 5);
		
		// -- Values with 4 decimals max, plain double --
		
		encodeDecode(-4294967.296, 9);
		encodeDecode( 4294967.296, 9);
	}
	
	private void encodeDecode(double i, int expectedSize) throws IOException {
		Double value = Double.valueOf(i);
		byte[] data = encode(value);
		Double clone = decode(data, Double.class);
		
		if (expectedSize > 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(
			Double.doubleToLongBits(value.doubleValue()),
			Double.doubleToLongBits(clone.doubleValue())
		);
	}
}
