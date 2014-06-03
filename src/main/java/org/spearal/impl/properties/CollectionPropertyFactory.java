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
package org.spearal.impl.properties;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.spearal.configurable.ObjectWriterProvider;
import org.spearal.configurable.PropertyFactory;

/**
 * @author Franck WOLFF
 */
public class CollectionPropertyFactory implements PropertyFactory, ObjectWriterProvider {
	
	@Override
	public ObjectWriter getWriter(Class<?> type) {
		if (CollectionProperty.canCreateWriter(type))
			return CollectionProperty.createWriter();
		if (ArrayProperty.canCreateWriter(type))
			return ArrayProperty.createWriter();
		return null;
	}

	@Override
	public boolean canCreateProperty(Class<?> type) {
		return (
			CollectionProperty.canCreateProperty(type) ||
			ArrayProperty.canCreateProperty(type)
		);
	}

	@Override
	public Property createProperty(String name, Field field, Method getter, Method setter) {
		if (CollectionProperty.canCreateProperty(AbstractProperty.getType(field, getter)))
			return new CollectionProperty(name, field, getter, setter);
		return new ArrayProperty(name, field, getter, setter);
	}
}
