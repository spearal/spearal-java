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

import org.junit.Assert;
import org.junit.Test;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class TestSpearalType {

	@Test
	public void test() {
		int parameterizedType = Integer.MIN_VALUE;
		try {
			SpearalType.valueOf(parameterizedType);
			Assert.fail("Should throw an exception: " + parameterizedType);
		}
		catch (IllegalArgumentException e) {
		}
		
		parameterizedType = 0x00 - 1;
		try {
			SpearalType.valueOf(parameterizedType);
			Assert.fail("Should throw an exception: " + parameterizedType);
		}
		catch (IllegalArgumentException e) {
		}
		
		for (int i = 0x00; i <= 0x0f; i++) {
			try {
				SpearalType type = SpearalType.valueOf(i);
				Assert.assertEquals(i, type.id());
			}
			catch (IllegalArgumentException e) {
				Assert.assertNull(findTypeWithId(i));
			}
		}
		
		for (int i = 0x10; i <= 0xff; i++) {
			try {
				SpearalType type = SpearalType.valueOf(i);
				Assert.assertEquals((i & 0xf0), type.id());
			}
			catch (IllegalArgumentException e) {
				Assert.assertNull(findTypeWithId(i & 0xf0));
			}
		}
		
		parameterizedType = 0xff + 1;
		try {
			SpearalType.valueOf(parameterizedType);
			Assert.fail("Should throw an exception: " + parameterizedType);
		}
		catch (IllegalArgumentException e) {
		}
		
		parameterizedType = Integer.MAX_VALUE;
		try {
			SpearalType.valueOf(parameterizedType);
			Assert.fail("Should throw an exception: " + parameterizedType);
		}
		catch (IllegalArgumentException e) {
		}
	}
	
	private static SpearalType findTypeWithId(int id) {
		for (SpearalType type : SpearalType.values()) {
			if (type.id() == id)
				return type;
		}
		return null;
	}
}
