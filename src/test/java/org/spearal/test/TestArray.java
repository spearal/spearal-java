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
import java.util.Collection;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestArray extends AbstractSpearalTestUnit {

	@Before
	public void setUp() throws Exception {
		// printStream = System.out;
	}

	@After
	public void tearDown() throws Exception {
		printStream = NULL_PRINT_STREAM;
	}

	@Test
	public void testBooleans() throws IOException {
		encodeDecode(new boolean[0], 2);
		encodeDecode(new boolean[]{ true }, 3);
		encodeDecode(new boolean[]{ false, true }, 4);
	}
	
	private void encodeDecode(boolean[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Boolean))
				Assert.fail("Not a Boolean: " + item);
			if (value[i++])
				Assert.assertTrue(((Boolean)item).booleanValue());
			else
				Assert.assertFalse(((Boolean)item).booleanValue());
		}
	}

	@Test
	public void testShorts() throws IOException {
		encodeDecode(new short[0], 2);
		encodeDecode(new short[]{ 1 }, 4);
		encodeDecode(new short[]{ Short.MIN_VALUE, Short.MAX_VALUE }, 8);
		
		short[] array = new short[8000];
		for (int i = 0; i < array.length; i++)
			array[i] = (short)i;
		encodeDecode(array, 23747);
	}
	
	private void encodeDecode(short[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Long))
				Assert.fail("Not a Long: " + item);
			Assert.assertEquals(value[i++], ((Long)item).longValue());
		}
	}

	@Test
	public void testInts() throws IOException {
		encodeDecode(new int[0], 2);
		encodeDecode(new int[]{ 1 }, 4);
		encodeDecode(new int[]{ Integer.MIN_VALUE, Integer.MAX_VALUE }, 12);
		
		int[] array = new int[8000];
		for (int i = 0; i < array.length; i++)
			array[i] = i;
		encodeDecode(array, 23747);
	}
	
	private void encodeDecode(int[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Long))
				Assert.fail("Not a Long: " + item);
			Assert.assertEquals(value[i++], ((Long)item).longValue());
		}
	}

	@Test
	public void testLongs() throws IOException {
		encodeDecode(new long[0], 2);
		encodeDecode(new long[]{ 1 }, 4);
		encodeDecode(new long[]{ Long.MIN_VALUE, Long.MAX_VALUE }, 20);
		
		long[] array = new long[8000];
		for (int i = 0; i < array.length; i++)
			array[i] = i;
		encodeDecode(array, 23747);
	}
	
	private void encodeDecode(long[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Long))
				Assert.fail("Not a Long: " + item);
			Assert.assertEquals(value[i++], ((Long)item).longValue());
		}
	}

	@Test
	public void testFloats() throws IOException {
		encodeDecode(new float[0], 2);
		encodeDecode(new float[]{ 1.0f }, 4);
		encodeDecode(new float[]{
			Float.NEGATIVE_INFINITY, Float.NaN,
			Float.MIN_VALUE, Float.MAX_VALUE }, 38);
		
		float[] floats = new float[2048];
		for (int i = 0; i < floats.length; i++)
			floats[i] = i;
		
		encodeDecode(floats, -1);
	}
	
	private void encodeDecode(float[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Double)) {
				if (item instanceof Long)
					item = Double.valueOf(((Long)item).doubleValue());
				else
					Assert.fail("Not a Double: " + item);
			}
			Assert.assertEquals(
				Double.doubleToLongBits(value[i++]),
				Double.doubleToLongBits(((Double)item).doubleValue())
			);
		}
	}

	@Test
	public void testDoubles() throws IOException {
		encodeDecode(new double[0], 2);
		encodeDecode(new double[]{ 1.0f }, 4);
		encodeDecode(new double[]{
			Double.NEGATIVE_INFINITY, Double.NaN,
			Double.MIN_VALUE, Double.MAX_VALUE }, 38);
		
		double[] doubles = new double[2048];
		for (int i = 0; i < doubles.length; i++)
			doubles[i] = i;
		
		encodeDecode(doubles, -1);
	}
	
	private void encodeDecode(double[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			if (!(item instanceof Double)) {
				if (item instanceof Long)
					item = Double.valueOf(((Long)item).doubleValue());
				else
					Assert.fail("Not a Double: " + item);
			}
			Assert.assertEquals(
				Double.doubleToLongBits(value[i++]),
				Double.doubleToLongBits(((Double)item).doubleValue())
			);
		}
	}

	@Test
	public void testIntsDim2() throws IOException {
		encodeDecode(new int[][]{}, 2);
		encodeDecode(new int[][]{ null }, 3);
		encodeDecode(new int[][]{ {} }, 4);
		encodeDecode(new int[][]{ { 0 } }, 6);
		encodeDecode(new int[][]{ { 0, 1 }, null }, 9);
		encodeDecode(new int[][]{ { 0, 1 }, null, { 2, 3, 4, 5, 6, 7, 8, 9, 10 } }, 29);
	}
	
	private void encodeDecode(int[][] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		
		Collection<?> coll = (Collection<?>)clone;
		Assert.assertEquals(value.length, coll.size());
		
		int i = 0;
		for (Object item : coll) {
			int[] subValue = value[i++];
			if (subValue == null) {
				Assert.assertNull(item);
				continue;
			}
			if (!(item instanceof Collection))
				Assert.fail("Not a Collection: " + item);
			
			Collection<?> subColl = (Collection<?>)item;
			Assert.assertEquals(subValue.length, subColl.size());
			
			int j = 0;
			for (Object subItem : subColl) {
				if (!(subItem instanceof Long))
					Assert.fail("Not a Long: " + subItem);
				Assert.assertEquals(subValue[j++], ((Long)subItem).longValue());
			}
		}
	}

	@Test
	public void testObjects() throws IOException {
		encodeDecode(new Object[0], 2);
		encodeDecode(new Object[]{ null }, 3);
		encodeDecode(new Object[]{ Long.valueOf(678), "abcdef", new Date() }, -1);
	}
	
	private void encodeDecode(Object[] value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!(clone instanceof Collection))
			Assert.fail("Not a Collection: " + clone);
		Assert.assertArrayEquals(value, ((Collection<?>)clone).toArray());
	}
}
