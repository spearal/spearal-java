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
public class TestInteger extends AbstractSpearalTestUnit {

	@Test
	public void test() throws IOException {
		encodeDecode(Integer.MIN_VALUE, 5);
		encodeDecode(Integer.MIN_VALUE + 1, 5);
		
		encodeDecode(-0x01000000, 5);
		encodeDecode(-0x00FFFFFF, 4);
		
		encodeDecode(-0x00010000, 4);
		
		for (int i = -0xFFFF; i <= 0xFFFF; i++)
			encodeDecode(i, (Math.abs(i) <= 0xFF ? 2 : 3));
		
		encodeDecode(0x00010000, 4);
		
		encodeDecode(0x00FFFFFF, 4);
		encodeDecode(0x01000000, 5);
		
		encodeDecode(Integer.MAX_VALUE - 1, 5);
		encodeDecode(Integer.MAX_VALUE, 5);
	}
	
	private void encodeDecode(int i, int expectedSize) throws IOException {
		Integer value = Integer.valueOf(i);
		byte[] data = encode(value);
		Object clone = decode(data);
		
		Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Long))
			Assert.fail("Not a Long: " + clone);
		Assert.assertEquals(value.longValue(), ((Long)clone).longValue());
	}
}
