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
import org.spearal.test.model.ChildBean;
import org.spearal.test.model.SimpleBean;

/**
 * @author Franck WOLFF
 */
public class TestBean extends AbstractSpearalTestUnit {

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
		encodeDecode(new ChildBean(), -1);
		
		encodeDecode(new ChildBean(-3, "parent", true, 0.001), -1);
		
		ChildBean bean = new ChildBean(56, "parent", false, 3.001);
		bean.getSimpleBeans().add(new SimpleBean(true, 3, 5.09, "abc"));
		bean.getSimpleBeans().add(new SimpleBean(false, -5, -10.09, "abc"));
		
		encodeDecode(bean, -1);
	}
	
	private void encodeDecode(Object value, int expectedSize) throws IOException {
		byte[] data = encode(value);
		Object clone = decode(data);
		
		if (expectedSize >= 0)
			Assert.assertEquals(expectedSize, data.length);
		Assert.assertEquals(value, clone);
	}
}
