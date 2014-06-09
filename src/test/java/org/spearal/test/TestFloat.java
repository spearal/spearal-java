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
public class TestFloat extends AbstractSpearalTestUnit {

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
		encodeDecode(Float.NEGATIVE_INFINITY, 9, Double.class);
		encodeDecode(-Float.MAX_VALUE, 9, Double.class);
		encodeDecode(-Float.MIN_VALUE, 9, Double.class);
		encodeDecode(-0.0f, 9, Double.class);
		encodeDecode(Float.NaN, 9, Double.class);
		encodeDecode(0.0f, 2, Long.class);
		encodeDecode(Float.MIN_VALUE, 9, Double.class);
		encodeDecode(Float.MAX_VALUE, 9, Double.class);
		encodeDecode(Float.POSITIVE_INFINITY, 9, Double.class);
	}
	
	private void encodeDecode(float i, int expectedSize, Class<?> expectedClass) throws IOException {
		Float value = Float.valueOf(i);
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize > 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(expectedClass, clone.getClass());
		if (clone instanceof Long)
			clone = Double.valueOf(((Long)clone).doubleValue());
		Assert.assertEquals(
			Float.floatToIntBits(value.floatValue()),
			Float.floatToIntBits(((Double)clone).floatValue())
		);
	}
}
