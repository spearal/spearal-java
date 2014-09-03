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

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.spearal.SpearalDecoder;
import org.spearal.SpearalEncoder;
import org.spearal.DefaultSpearalFactory;
import org.spearal.SpearalFactory;
import org.spearal.configuration.AliasStrategy;
import org.spearal.impl.SpearalDecoderImpl.ClassNotFound;
import org.spearal.test.model.AliasedSimpleBean;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestAliasedBean extends AbstractSpearalTestUnit {

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
		SimpleBean bean = new SimpleBean(true, 1, 0.1, "blabla");
		
		SpearalFactory serverFactory = new DefaultSpearalFactory();
		serverFactory.getContext().configure(new AliasStrategy() {
			
			@Override
			public String alias(Class<?> cls) {
				return cls.getSimpleName();
			}
			
			@Override
			public String unalias(String aliasedClassName) {
				return "org.spearal.test.model." + aliasedClassName;
			}
		});
		
		SpearalFactory clientFactory = new DefaultSpearalFactory();
		clientFactory.getContext().configure(new AliasStrategy() {
			
			@Override
			public String alias(Class<?> cls) {
				return cls.getSimpleName().substring("Aliased".length());
			}
			
			@Override
			public String unalias(String aliasedClassName) {
				return "org.spearal.test.model.Aliased" + aliasedClassName;
			}
		});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalEncoder encoder = serverFactory.newEncoder(baos);
		encoder.writeAny(bean);
		
		byte[] bytes = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalDecoder decoder = clientFactory.newDecoder(bais);
		decoder.printAny(clientFactory.newPrinter(printStream));
		printStream.println();
		
		bais.reset();
		decoder = clientFactory.newDecoder(bais);
		AliasedSimpleBean clientBean = decoder.readAny(AliasedSimpleBean.class);
		
		Assert.assertEquals(bean.isBooleanValue(), clientBean.isBooleanValue());
		Assert.assertEquals(bean.getIntValue(), clientBean.getIntValue());
		Assert.assertEquals(bean.getDoubleValue(), clientBean.getDoubleValue(), 0.0);
		Assert.assertEquals(bean.getStringValue(), clientBean.getStringValue());
		
		baos = new ByteArrayOutputStream();
		encoder = clientFactory.newEncoder(baos);
		encoder.writeAny(clientBean);
		
		bytes = baos.toByteArray();
		bais = new ByteArrayInputStream(bytes);
		decoder = serverFactory.newDecoder(bais);
		decoder.printAny(serverFactory.newPrinter(printStream));
		printStream.println();
		
		bais.reset();
		decoder = serverFactory.newDecoder(bais);
		SimpleBean serverBean = decoder.readAny(SimpleBean.class);
		
		Assert.assertEquals(bean.isBooleanValue(), serverBean.isBooleanValue());
		Assert.assertEquals(bean.getIntValue(), serverBean.getIntValue());
		Assert.assertEquals(bean.getDoubleValue(), serverBean.getDoubleValue(), 0.0);
		Assert.assertEquals(bean.getStringValue(), serverBean.getStringValue());
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void testClassNotFoundBean() throws IOException {
		SimpleBean bean = new SimpleBean(true, 1, 0.1, "blabla");
		
		SpearalFactory serverFactory = new DefaultSpearalFactory();
		serverFactory.getContext().configure(new AliasStrategy() {
			
			@Override
			public String alias(Class<?> cls) {
				return "org.error." + cls.getSimpleName();
			}
			
			@Override
			public String unalias(String aliasedClassName) {
				return aliasedClassName;
			}
		});
		
		SpearalFactory clientFactory = new DefaultSpearalFactory();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalEncoder encoder = serverFactory.newEncoder(baos);
		encoder.writeAny(bean);
		
		byte[] bytes = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalDecoder decoder = clientFactory.newDecoder(bais);
		decoder.printAny(clientFactory.newPrinter(printStream));
		printStream.println();
		
		bais.reset();
		decoder = clientFactory.newDecoder(bais);
		ClassNotFound clientBean = (ClassNotFound)decoder.readAny();
		
		Assert.assertEquals(
			clientBean.getClassNotFoundDescription(),
			"org.error.SimpleBean#booleanValue,doubleValue,intValue,stringValue"
		);
		Assert.assertEquals(bean.isBooleanValue(), clientBean.get("booleanValue"));
		Assert.assertEquals(Long.valueOf(bean.getIntValue()), clientBean.get("intValue"));
		Assert.assertEquals(bean.getDoubleValue(), clientBean.get("doubleValue"));
		Assert.assertEquals(bean.getStringValue(), clientBean.get("stringValue"));
		
		bais.reset();
		decoder = clientFactory.newDecoder(bais);
		SimpleBean copy = decoder.readAny(SimpleBean.class);
		Assert.assertEquals(bean, copy);
	}
}
