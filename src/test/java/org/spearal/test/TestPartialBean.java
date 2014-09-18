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
import org.spearal.test.model.AliasedAlteredSimpleBean;
import org.spearal.test.model.ChildBean;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestPartialBean extends AbstractSpearalTestUnit {

	@Before
	public void setUp() throws Exception {
		printStream = System.out;
	}

	@After
	public void tearDown() throws Exception {
		printStream = NULL_PRINT_STREAM;
	}
	
	@SuppressWarnings("boxing")
	@Test
	public void test() throws IOException {
		ChildBean bean = new ChildBean(56, "parent", false, 3.001);
		bean.getSimpleBeans().add(new SimpleBean(true, 3, 5.09, "abc"));
		bean.getSimpleBeans().add(new SimpleBean(false, -5, -10.09, "abc"));
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalFactory factory = new DefaultSpearalFactory();
		SpearalEncoder encoder = factory.newEncoder(baos);
		encoder.getPropertyFilter().add(ChildBean.class, "childBooleanProperty", "parentStringProperty");
		encoder.writeAny(bean);
		
		Object result = decodeCheckPartial(new DefaultSpearalFactory(), baos.toByteArray(), null);
		
		if (!(result instanceof PartialObjectProxy))
			Assert.fail("Not a PartialObjectProxy: " + result);
		if (!(result instanceof ChildBean))
			Assert.fail("Not a ChildBean: " + result);
		Assert.assertEquals(bean.isChildBooleanProperty(), ((ChildBean)result).isChildBooleanProperty());
		Assert.assertEquals(bean.getParentStringProperty(), ((ChildBean)result).getParentStringProperty());
		try {
			((ChildBean)result).getChildDoubleProperty();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		try {
			((ChildBean)result).getParentIntProperty();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		try {
			((ChildBean)result).getSimpleBeans();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}


		baos = new ByteArrayOutputStream();
		encoder = factory.newEncoder(baos);
		encoder.getPropertyFilter().add(ChildBean.class, "childBooleanProperty", "simpleBeans", "parentStringProperty");
		encoder.getPropertyFilter().add(SimpleBean.class, "booleanValue", "stringValue");
		encoder.writeAny(bean);
		
		result = decodeCheckPartial(new DefaultSpearalFactory(), baos.toByteArray(), null);

		if (!(result instanceof PartialObjectProxy))
			Assert.fail("Not a PartialObjectProxy: " + result);
		if (!(result instanceof ChildBean))
			Assert.fail("Not a ChildBean: " + result);

		PartialObjectProxy partial = (PartialObjectProxy)result;
		Assert.assertNotNull(partial.$getContext());
		Assert.assertTrue(partial.$isDefined("childBooleanProperty"));
		Assert.assertTrue(partial.$isDefined("simpleBeans"));
		Assert.assertTrue(partial.$isDefined("parentStringProperty"));
		Assert.assertEquals(3, partial.$getDefinedProperties().length);
		
		Assert.assertEquals(bean.isChildBooleanProperty(), ((ChildBean)result).isChildBooleanProperty());
		Assert.assertEquals(bean.getParentStringProperty(), ((ChildBean)result).getParentStringProperty());
		try {
			((ChildBean)result).getChildDoubleProperty();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		try {
			((ChildBean)result).getParentIntProperty();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
		}
		
		Assert.assertEquals(bean.getSimpleBeans().size(), ((ChildBean)result).getSimpleBeans().size());
	}

	@Test
	public void testAltered() throws IOException {
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
				return "org.spearal.test.model.AliasedAltered" + aliasedClassName;
			}
		});

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalEncoder encoder = serverFactory.newEncoder(baos);
		encoder.writeAny(bean);
		
		AliasedAlteredSimpleBean clientBean = decodeCheckPartial(clientFactory, baos.toByteArray(), AliasedAlteredSimpleBean.class);
		
		if (!(clientBean instanceof PartialObjectProxy))
			Assert.fail("Not a PartialObjectProxy: " + clientBean);
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
		
		Assert.assertTrue(decoder.containsPartialObjects());

		return (T)result;
	}
}
