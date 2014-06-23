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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.util.UnmodifiableArray;

/**
 * @author Franck WOLFF
 */
public class SpearalPropertyFilterImpl implements SpearalPropertyFilter {

	private final SpearalContext context;
	private final Map<Class<?>, Collection<Property>> propertiesMap;
	
	public SpearalPropertyFilterImpl(SpearalContext context) {
		this.context = context;
		this.propertiesMap = new HashMap<Class<?>, Collection<Property>>();
	}

	@Override
	public void add(Class<?> cls, String... propertyNames) {
		if (propertyNames == null || propertyNames.length == 0) {
			UnmodifiableArray<Property> empty = UnmodifiableArray.empty();
			this.propertiesMap.put(cls, empty);
		}
		else {
			Set<String> propertyNamesSet = new HashSet<String>(Arrays.asList(propertyNames));
			Collection<Property> properties = context.getProperties(cls);
			List<Property> selectedProperties = new ArrayList<Property>(propertyNames.length);
			
			for (Property property : properties) {
				if (propertyNamesSet.contains(property.getName()))
					selectedProperties.add(property);
			}
			
			this.propertiesMap.put(cls, UnmodifiableArray.of(selectedProperties.toArray(new Property[0])));
		}
		
	}

	@Override
	public Collection<Property> get(Class<?> cls) {
		Collection<Property> properties = propertiesMap.get(cls);
		if (properties == null) {
			properties = context.getProperties(cls);
			this.propertiesMap.put(cls, properties);
		}
		return properties;
	}
}
