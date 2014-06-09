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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestMap extends AbstractSpearalTestUnit {

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
		
		encodeDecode(new HashMap<String, Object>(), -1);

		Map<String, SimpleBean> map = new HashMap<String, SimpleBean>();
		map.put("abc", new SimpleBean(true, 3, -0.1, "abc"));
		encodeDecode(map, -1);
		
		map = new HashMap<String, SimpleBean>();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			map.put("key" + i, new SimpleBean(
				random.nextBoolean(),
				random.nextInt(),
				random.nextDouble(),
				"string" + i
			));
		}
		encodeDecode(map, -1);
	}
	
	private void encodeDecode(Map<?, ?> value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Map))
			Assert.fail("Not a Map: " + clone);
		Assert.assertEquals(value, clone);
	}
}
