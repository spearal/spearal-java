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
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Franck WOLFF
 */
public class TestProxy extends AbstractSpearalTestUnit {
	
	public static interface Nameable {
		
		String getName();
		void setName(String name);
	}
	
	public static class NameableInvocationHandler implements InvocationHandler {

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
			if ("getName".equals(method.getName()))
				return "John Doo";
			return null;
		}
	}
	
	@Test
	public void test() throws IOException {
		Object proxy = Proxy.newProxyInstance(
			getClass().getClassLoader(),
			new Class<?>[]{ Nameable.class, Serializable.class },
			new NameableInvocationHandler()
		);
		
		Object clone = encodeDecode(proxy, -1);
		if (!(clone instanceof Nameable))
			Assert.fail("Not a Nameable: " + clone);
		
		Nameable nameable = (Nameable)clone;
		Assert.assertEquals("John Doo", nameable.getName());
	}
	
	private Object encodeDecode(Object value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		if (!Proxy.isProxyClass(clone.getClass()))
			Assert.fail("Not a Proxy: " + clone);
		return clone;
	}
}
