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
package org.spearal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;

import org.spearal.configuration.Configurable;
import org.spearal.configuration.Securizer;
import org.spearal.configuration.CoderProvider.Coder;
import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface SpearalContext {

	void configure(Configurable configurable);
	void configure(Configurable configurable, boolean append);
	
	Securizer getSecurizer();
	
	String getClassNameAlias(String className);
	
	Class<?> loadClass(String...classNames)
		throws SecurityException;
	
	Collection<Property> getProperties(Class<?> cls);
	
	Object instantiate(SpearalDecoder decoder, Type type)
		throws InstantiationException, IllegalAccessException;
	Object instantiate(SpearalDecoder decoder, Property property)
		throws InstantiationException, IllegalAccessException;
	Object instantiatePartial(SpearalDecoder decoder, Class<?> cls, Collection<Property> partialProperties)
		throws InstantiationException, IllegalAccessException;
	
	Coder getCoder(Class<?> valueClass);
	
	Object convert(SpearalDecoder decoder, Object value, Type targetType);
	
	Property createProperty(String name, Field field, Method getter, Method setter);
}
