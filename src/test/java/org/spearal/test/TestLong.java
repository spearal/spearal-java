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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestLong extends AbstractSpearalTestUnit {

	@Test
	public void test() throws IOException {
		encodeDecode(Long.MIN_VALUE, 9);
		encodeDecode(Long.MIN_VALUE + 1, 9);
		
		encodeDecode(-0x0100000000000000L, 9);
		encodeDecode(-0x00FFFFFFFFFFFFFFL, 8);
		
		encodeDecode(-0x0001000000000000L, 8);
		encodeDecode(-0x0000FFFFFFFFFFFFL, 7);
		
		encodeDecode(-0x0000010000000000L, 7);
		encodeDecode(-0x000000FFFFFFFFFFL, 6);
		
		encodeDecode(-0x0000000100000000L, 6);
		encodeDecode(-0x00000000FFFFFFFFL, 5);
		
		encodeDecode(-0x0000000001000000L, 5);
		encodeDecode(-0x0000000000FFFFFFL, 4);
		
		encodeDecode(-0x0000000000010000L, 4);
		
		for (long i = -0xFFFFL; i <= 0xFFFFL; i++)
			encodeDecode(i, (Math.abs(i) <= 0xFFL ? 2 : 3));
		
		encodeDecode(0x0000000000010000L, 4);
		
		encodeDecode(0x0000000000FFFFFFL, 4);
		encodeDecode(0x0000000001000000L, 5);
		
		encodeDecode(0x00000000FFFFFFFFL, 5);
		encodeDecode(0x0000000100000000L, 6);
		
		encodeDecode(0x000000FFFFFFFFFFL, 6);
		encodeDecode(0x0000010000000000L, 7);
		
		encodeDecode(0x0000FFFFFFFFFFFFL, 7);
		encodeDecode(0x0001000000000000L, 8);
		
		encodeDecode(0x00FFFFFFFFFFFFFFL, 8);
		encodeDecode(0x0100000000000000L, 9);
		
		encodeDecode(Long.MAX_VALUE - 1, 9);
		encodeDecode(Long.MAX_VALUE, 9);
	}
	
	private void encodeDecode(long i, int expectedSize) throws IOException {
		Long value = Long.valueOf(i);
		byte[] data = encode(value);
		Object clone = decode(data);
		
		Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Long))
			Assert.fail("Not a Long: " + clone);
		Assert.assertEquals(value.longValue(), ((Long)clone).longValue());
	}
}
