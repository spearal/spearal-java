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
public class TestEnum extends AbstractSpearalTestUnit {

	private enum DummyEnum {
		A,
		BC,
		DEF,
		GHIJ,
		KLMNOPQRSTUVWXYZ
	}
	
	@Test
	public void test() throws IOException {
		int length = DummyEnum.class.getName().length();
		for (DummyEnum e : DummyEnum.values())
			encodeDecode(e, length + e.name().length() + 4);
	}
	
	private void encodeDecode(Enum<?> value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Enum))
			Assert.fail("Not an Enum: " + clone);
		Assert.assertEquals(value, clone);
	}
}
