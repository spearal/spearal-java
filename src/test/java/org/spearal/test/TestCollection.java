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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestCollection extends AbstractSpearalTestUnit {

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
		encodeDecode(new ArrayList<Object>(), -1);
		
		encodeDecode(Arrays.asList("abc", "def", "abc"), -1);
		
		encodeDecode(Arrays.asList(
			new SimpleBean(true, 3, -0.1, "abc"),
			new SimpleBean(false, -4, 70.1, "def"),
			new SimpleBean(false, -576, -56.001, "ghi")
		), -1);
		
		List<SimpleBean> collection = new ArrayList<SimpleBean>();
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			collection.add(new SimpleBean(
				random.nextBoolean(),
				random.nextInt(),
				random.nextDouble(),
				"string" + i
			));
		}
		for (int i = 0; i < 20; i++)
			collection.add(collection.get(i));
		encodeDecode(collection, -1);
	}
	
	private void encodeDecode(Collection<?> value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		Assert.assertEquals(value, clone);
	}
}
