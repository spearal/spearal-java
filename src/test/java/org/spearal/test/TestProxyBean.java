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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spearal.DefaultSpearalFactory;
import org.spearal.SpearalDecoder;
import org.spearal.SpearalEncoder;
import org.spearal.SpearalFactory;
import org.spearal.configuration.AliasStrategy;
import org.spearal.configuration.PartialObjectFactory.PartialObjectProxy;
import org.spearal.configuration.PartialObjectFactory.UndefinedPropertyException;
import org.spearal.test.model.ProxyBean;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestProxyBean extends AbstractSpearalTestUnit {

	@Before
	public void setUp() throws Exception {
		// printStream = System.out;
	}

	@After
	public void tearDown() throws Exception {
		printStream = NULL_PRINT_STREAM;
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void test() throws IOException {
		SimpleBean bean = new SimpleBean(true, 56, 78.893, "bla");
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalFactory serverFactory = new DefaultSpearalFactory();
		SpearalEncoder encoder = serverFactory.newEncoder(baos);
		encoder.getPropertyFilter().add(SimpleBean.class, "intValue", "stringValue");
		encoder.writeAny(bean);
		
		SpearalFactory clientFactory = new DefaultSpearalFactory();
		clientFactory.getContext().configure(new AliasStrategy() {
			@Override
			public String alias(Class<?> cls) {
				return cls.getName().replace("Proxy", "Simple");
			}
			
			@Override
			public String unalias(String aliasedClassName) {
				return aliasedClassName.replace("Simple", "Proxy");
			}
		});
		Object result = decodeCheckPartial(clientFactory, baos.toByteArray(), ProxyBean.class);
		
		if (!(result instanceof ProxyBean))
			Assert.fail("Not a ProxyBean: " + result);
		if (!(result instanceof PartialObjectProxy))
			Assert.fail("Not a PartialObjectProxy: " + result);
		Assert.assertEquals(bean.getIntValue(), ((ProxyBean)result).getIntValue());
		Assert.assertEquals(bean.getStringValue(), ((ProxyBean)result).getStringValue());
		try {
			((ProxyBean)result).isBooleanValue();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		((ProxyBean)result).setBooleanValue(true);
		Assert.assertEquals(true, ((ProxyBean)result).isBooleanValue());
		
		baos = new ByteArrayOutputStream();
		encoder = clientFactory.newEncoder(baos);
		encoder.writeAny(result);
		
		SpearalDecoder decoder = serverFactory.newDecoder(new ByteArrayInputStream(baos.toByteArray()));
		Object result2 = decoder.readAny();
		Assert.assertTrue(result2 instanceof PartialObjectProxy);
		
		try {
			((ProxyBean)result).getDoubleValue();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		((ProxyBean)result).setDoubleValue(4.53);
		Assert.assertEquals(4.53, ((ProxyBean)result).getDoubleValue(), 0.01);
		
		baos = new ByteArrayOutputStream();
		encoder = clientFactory.newEncoder(baos);
		encoder.writeAny(result);
		
		decoder = serverFactory.newDecoder(new ByteArrayInputStream(baos.toByteArray()));
		result2 = decoder.readAny();
		Assert.assertTrue(result2 instanceof SimpleBean);
	}
	
	@SuppressWarnings("unchecked")
	private <T> T decodeCheckPartial(SpearalFactory factory, byte[] bytes, Type targetType) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalDecoder decoder = factory.newDecoder(bais);
		decoder.printAny(factory.newPrinter(printStream));
		printStream.println();
		
		bais.reset();
		decoder = factory.newDecoder(bais);
		Object result = decoder.readAny(targetType);

		return (T)result;
	}
}
