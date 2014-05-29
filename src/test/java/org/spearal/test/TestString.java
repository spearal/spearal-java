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
public class TestString extends AbstractSpearalTestUnit {

	@Test
	public void test() throws IOException {

		// -- Empty String --
		
		encodeDecode("", 2);
		
		// -- String of length 1 (all UTF8 chars) --
		
		for (char c = 0; c <= 0x7F; c++)
			encodeDecode(String.valueOf(c), 3);
		for (char c = 0x80; c <= 0x7FF; c++)
			encodeDecode(String.valueOf(c), 4);
		for (char c = 0x800; c <= 0xD7FF; c++)
			encodeDecode(String.valueOf(c), 5);
		// Skip 0xD800...0xDFFF (illegal UTF8 chars)
		for (char c = 0xE000; c < 0xFFFF; c++)
			encodeDecode(String.valueOf(c), 5);
		encodeDecode(String.valueOf((char)0xFFFF), 5);
		
		encodeDecode(String.valueOf(Character.toChars(0x10000)), 8);
		for (int i = 0x10000; i <= 0x10FFFF; i++)
			encodeDecode(String.valueOf(Character.toChars(i)), 8);
		encodeDecode(String.valueOf(Character.toChars(0x10FFFF)), 8);
		
		// -- All UTF8 chars in a single String --
		
		StringBuilder sb = new StringBuilder(2160639);
		for (int i = 0; i < 0xD800; i++)
			sb.append((char)i);
		// Skip 0xD800...0xDFFF (illegal UTF8 chars)
		for (int i = 0xE000; i < 0xFFFF; i++)
			sb.append((char)i);
		for (int i = 0x10000; i <= 0x10FFFF; i++)
			sb.append(String.valueOf(Character.toChars(i)));
		encodeDecode(sb.toString(), 6479745);
	}
	
	private void encodeDecode(String value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof String))
			Assert.fail("Not a String: " + clone);
		Assert.assertEquals(value, clone);
	}
}
