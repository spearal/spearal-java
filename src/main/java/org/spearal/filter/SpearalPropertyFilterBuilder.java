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
package org.spearal.filter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.impl.util.ClassDescriptionUtil;

/**
 * Utility class to build property filters
 * 
 * @author William DRAI
 */
public class SpearalPropertyFilterBuilder {
	
	private final Map<Class<?>, String[]> propertyFiltersByClass = new LinkedHashMap<Class<?>, String[]>();
	
	/**
	 * Init a filter instance for the properties of the specified class
	 * @param entityClass entity class
	 * @param propertyNames name
	 * @return property filter builder
	 */
	public static SpearalPropertyFilterBuilder of(Class<?> entityClass, String... propertyNames) {
		SpearalPropertyFilterBuilder propertyFilter = new SpearalPropertyFilterBuilder();
		return propertyFilter.and(entityClass, propertyNames);
	}
	
	/**
	 * Append properties of the specified class to the current filter
	 * @param entityClass entity class
	 * @param propertyNames name
	 * @return property filter builder
	 */
	public SpearalPropertyFilterBuilder and(Class<?> entityClass, String... propertyNames) {
		propertyFiltersByClass.put(entityClass, propertyNames);
		return this;
	}
	
	/**
	 * Apply the current filters to the specified SpearalPropertyFilter
	 * @param propertyFilter Spearal property filter
	 */
	public void apply(SpearalPropertyFilter propertyFilter) {
		for (Entry<Class<?>, String[]> pf : propertyFiltersByClass.entrySet()) {
			propertyFilter.add(pf.getKey(), pf.getValue());
		}
	}
	
	/**
	 * Convert the current filters to a String representation for use in a http header
	 * @param context context 
	 * @return list of strings
	 */
	public List<Object> toHeaders(SpearalContext context) {
		List<Object> headers = new ArrayList<Object>();
		for (Entry<Class<?>, String[]> pf : propertyFiltersByClass.entrySet())
			headers.add(ClassDescriptionUtil.createAliasedDescription(context, pf.getKey(), pf.getValue()));		
		return headers;
	}
	
	/**
	 * Convert String representations of filters to a builder
	 * @param context context
	 * @param headers list of headers (for example coming from http)
	 * @return property filter builder
	 */
	public static SpearalPropertyFilterBuilder fromHeaders(SpearalContext context, List<String> headers) {
		SpearalPropertyFilterBuilder propertyFilters = null;
		for (String header : headers) {
			String classNames = ClassDescriptionUtil.classNames(header);
			Class<?> filterClass = context.loadClass(classNames, null);
			String[] propertyNames = ClassDescriptionUtil.splitPropertyNames(header);
			if (propertyFilters == null)
				propertyFilters = of(filterClass, propertyNames);
			else
				propertyFilters.and(filterClass, propertyNames);
		}
		return propertyFilters;
	}
}
