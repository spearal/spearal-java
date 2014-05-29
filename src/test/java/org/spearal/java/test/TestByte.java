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
package org.spearal.java.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestByte extends AbstractSpearalTestUnit {

	@Test
	public void test() throws IOException {
		for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
			Byte value = Byte.valueOf((byte)i);
			byte[] data = encode(value);
			Object clone = decode(data);

			Assert.assertEquals(2, data.length);
			if (!(clone instanceof Long))
				Assert.fail("Not a Long: " + clone);
			Assert.assertEquals(value.longValue(), ((Long)clone).longValue());
		}
	}
}
