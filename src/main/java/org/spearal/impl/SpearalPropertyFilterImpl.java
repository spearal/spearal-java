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
package org.spearal.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public class SpearalPropertyFilterImpl implements SpearalPropertyFilter {

	private final SpearalContext context;
	private final Map<Class<?>, Property[]> propertiesMap;
	
	public SpearalPropertyFilterImpl(SpearalContext context) {
		this.context = context;
		this.propertiesMap = new HashMap<Class<?>, Property[]>();
	}

	@Override
	public void add(Class<?> cls, String... propertyNames) {
		if (propertyNames == null)
			propertyNames = new String[0];

		Set<String> propertyNamesSet = new HashSet<String>(propertyNames.length);
		for (String propertyName : propertyNames)
			propertyNamesSet.add(propertyName);
		
		String[] unfilterableProperties = context.getUnfilterableProperties(cls);
		if (unfilterableProperties != null) {
			for (String propertyName : unfilterableProperties)
				propertyNamesSet.add(propertyName);
		}
		
		Property[] properties = context.getProperties(cls).clone();
		for (int i = 0; i < properties.length; i++) {
			if (!propertyNamesSet.contains(properties[i].getName()))
				properties[i] = null;
		}
		this.propertiesMap.put(cls, properties);
	}

	@Override
	public Property[] get(Class<?> cls) {
		Property[] properties = propertiesMap.get(cls);
		if (properties == null) {
			properties = context.getProperties(cls);
			this.propertiesMap.put(cls, properties);
		}
		return properties;
	}
}
