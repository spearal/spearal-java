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
package org.spearal.impl.property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.spearal.configuration.PropertyFactory;

/**
 * @author Franck WOLFF
 */
public class SimplePropertiesFactory implements PropertyFactory {
	
	@Override
	public Property createProperty(String name, Field field, Method getter, Method setter) {
		Class<?> type = AnyProperty.typeOf(field, getter);
		
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return new PrimitiveBooleanProperty(name, field, getter, setter);
			if (type == int.class)
				return new PrimitiveIntProperty(name, field, getter, setter);
			if (type == long.class)
				return new PrimitiveLongProperty(name, field, getter, setter);
			if (type == double.class)
				return new PrimitiveDoubleProperty(name, field, getter, setter);
		}
		else {
			if (type == String.class)
				return new StringProperty(name, field, getter, setter);
			if (Collection.class.isAssignableFrom(type))
				return new CollectionProperty(name, field, getter, setter);
			if (Map.class.isAssignableFrom(type))
				return new MapProperty(name, field, getter, setter);
		}
		
		return new AnyProperty(name, field, getter, setter);
	}
}
