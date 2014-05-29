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

import java.lang.reflect.Type;
import java.util.Collection;

import org.spearal.configurable.ConfigurableItem;
import org.spearal.configurable.PropertyFactory;
import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.configurable.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface SpearalContext {

	void initStandardConfigurables();
	
	void prependConfigurableItem(ConfigurableItem item);
	
	String getClassNameAlias(String className);
	
	Class<?> loadClass(String className)
		throws ClassNotFoundException;
	
	Collection<Property> getProperties(Class<?> cls);
	
	Object instantiate(Type type)
		throws InstantiationException, IllegalAccessException;
	Object instantiate(Property property)
		throws InstantiationException, IllegalAccessException;
	Object instantiatePartial(Class<?> cls, Collection<Property> partialProperties)
		throws InstantiationException, IllegalAccessException;
	
	Object convert(Object o, Type target);
	
	ObjectWriter getWriter(Class<?> type);
	
	PropertyFactory getPropertyFactory(Class<?> type);
}
