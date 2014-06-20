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
package org.spearal.impl.instantiator;

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;

import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.configurable.PropertyInstantiator;
import org.spearal.configurable.TypeInstantiator;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class ClassInstantiator implements TypeInstantiator, PropertyInstantiator {

	@Override
	public boolean canInstantiate(Type type) {
		Class<?> cls = TypeUtil.classOfType(type);
		return !(
			cls == Class.class ||
			cls.isInterface() ||
			cls.isPrimitive() ||
			cls.isEnum() ||
			cls.isAnnotation() ||
			Proxy.isProxyClass(cls)
		);
	}

	@Override
	public Object instantiate(ExtendedSpearalDecoder decoder, Type type) {
		Class<?> cls = TypeUtil.classOfType(type);
        try {
			return cls.newInstance();
		}
        catch (Exception e) {
			throw new RuntimeException("Could not create instance of: " + cls, e);
		}
	}

	@Override
	public boolean canInstantiate(Property property) {
		return canInstantiate(property.getGenericType());
	}

	@Override
	public Object instantiate(ExtendedSpearalDecoder decoder, Property property) {
		return instantiate(decoder, property.getGenericType());
	}
}
