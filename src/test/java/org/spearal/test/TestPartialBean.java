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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spearal.SpearalEncoder;
import org.spearal.SpearalFactory;
import org.spearal.partial.PartialObjectProxy;
import org.spearal.partial.UndefinedPropertyException;
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
		SpearalFactory factory = new SpearalFactory();
		SpearalEncoder encoder = factory.newEncoder(baos);
		encoder.getRequest().addPropertyFilter(ChildBean.class, "childBooleanProperty", "parentStringProperty");
		encoder.writeAny(bean);
		
		Object result = decode(baos.toByteArray());
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
		encoder.getRequest().addPropertyFilter(ChildBean.class, "childBooleanProperty", "simpleBeans", "parentStringProperty");
		encoder.getRequest().addPropertyFilter(SimpleBean.class, "booleanValue", "stringValue");
		encoder.writeAny(bean);
		
		result = decode(baos.toByteArray());
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
		
		Assert.assertEquals(bean.getSimpleBeans().size(), ((ChildBean)result).getSimpleBeans().size());
	}
}
