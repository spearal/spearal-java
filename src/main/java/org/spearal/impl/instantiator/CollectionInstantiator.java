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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import org.spearal.SpearalContext;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiatorProvider;
import org.spearal.configuration.PropertyInstantiatorProvider.PropertyInstantiator;
import org.spearal.configuration.TypeInstantiatorProvider;
import org.spearal.configuration.TypeInstantiatorProvider.TypeInstantiator;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class CollectionInstantiator implements
	TypeInstantiatorProvider, TypeInstantiator,
	PropertyInstantiatorProvider, PropertyInstantiator {

	@Override
	public TypeInstantiator getInstantiator(Type type) {
		return (canInstantiate(type) ? this : null);
	}

	@Override
	public Object instantiate(SpearalContext context, Type type, Object param) {
		Class<?> cls = TypeUtil.classOfType(type);
		int capacity = (param instanceof Integer ? ((Integer)param).intValue() : -1);

		if (cls.isInterface()) {
			if (cls.isAssignableFrom(ArrayList.class))
				return (capacity < 0 ? new ArrayList<Object>() : new ArrayList<Object>(capacity));
			if (cls.isAssignableFrom(HashSet.class))
				return (capacity < 0 ? new HashSet<Object>() : new HashSet<Object>(capacity));
			if (cls.isAssignableFrom(TreeSet.class))
				return new TreeSet<Object>();
            throw new IllegalArgumentException("Unsupported collection interface: " + cls);
        }
		
		context.getSecurizer().checkDecodable(type);
		
        try {
			return cls.newInstance();
		}
        catch (Exception e) {
			throw new RuntimeException("Could not create instance of: " + cls, e);
		}
	}

	@Override
	public PropertyInstantiator getInstantiator(Property property) {
		return (canInstantiate(property.getGenericType()) ? this : null);
	}

	@Override
	public Object instantiate(SpearalContext context, Property property, Object param) {
		return instantiate(context, property.getGenericType(), param);
	}

	private static boolean canInstantiate(Type type) {
		return Collection.class.isAssignableFrom(TypeUtil.classOfType(type));
	}
}
