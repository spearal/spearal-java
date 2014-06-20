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
import org.spearal.test.model.SimpleEnum;

/**
 * @author Franck WOLFF
 */
public class TestEnum extends AbstractSpearalTestUnit {

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
		int length = SimpleEnum.class.getName().length();
		
		for (SimpleEnum e : SimpleEnum.values())
			encodeDecode(e, length + e.name().length() + 4);
		
		for (SimpleEnum e : SimpleEnum.values())
			encodeDecodeAsString(e, e.name().length() + 2);
	}
	
	private void encodeDecode(SimpleEnum value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		SimpleEnum clone = decode(data, SimpleEnum.class);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(value, clone);
	}
	
	private void encodeDecodeAsString(SimpleEnum value, int expectedSize) throws IOException {
		byte[] data = encode(value.name());
		SimpleEnum clone = decode(data, SimpleEnum.class);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(value, clone);
	}
}
