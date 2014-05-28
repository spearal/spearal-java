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
public class StringPropertyFactory implements PropertyFactory, ObjectWriterProvider {

	@Override
	public ObjectWriter getWriter(Class<?> type) {
		if (StringProperty.canCreateWriter(type))
			return StringProperty.createWriter();
		if (CharArrayProperty.canCreateWriter(type))
			return CharArrayProperty.createWriter();
		if (CharProperty.canCreateWriter(type))
			return CharProperty.createWriter();
		if (CharacterArrayProperty.canCreateWriter(type))
			return CharacterArrayProperty.createWriter();
		return null;
	}

	@Override
	public boolean canCreateProperty(Class<?> type) {
		return (
			StringProperty.canCreateProperty(type) ||
			CharArrayProperty.canCreateProperty(type) ||
			CharProperty.canCreateProperty(type) ||
			CharacterArrayProperty.canCreateProperty(type)
		);
	}

	@Override
	public Property createProperty(String name, Field field, Method getter, Method setter) {
		Class<?> type = AbstractProperty.getType(field, getter);
		if (StringProperty.canCreateProperty(type))
			return new StringProperty(name, field, getter, setter);
		if (CharArrayProperty.canCreateProperty(type))
			return new CharArrayProperty(name, field, getter, setter);
		if (CharProperty.canCreateProperty(type))
			return new CharProperty(name, field, getter, setter);
		// CharacterArrayProperty.canCreateProperty(type)
		return new CharacterArrayProperty(name, field, getter, setter);
	}
}
