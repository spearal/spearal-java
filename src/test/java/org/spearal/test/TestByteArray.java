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
public class TestByteArray extends AbstractSpearalTestUnit {

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
		encodeDecode(new byte[0], 2);
		
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
			encodeDecode(new byte[]{(byte)i}, 3);

		byte[] bytes = new byte[256];
		int j = 0;
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++)
			bytes[j++] = (byte)i;
		encodeDecode(bytes, bytes.length + 3);
		
		bytes = new byte[1234567];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte)i;
		encodeDecode(bytes, bytes.length + 4);
	}
	
	private void encodeDecode(byte[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		byte[] clone = decode(data, byte[].class);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertArrayEquals(value, clone);
	}
}
