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
package org.spearal.impl.descriptor;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.configuration.FilteredBeanDescriptorFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.util.ClassDescriptionUtil;

/**
 * @author Franck WOLFF
 */
public class FilteredBeanDescriptorFactoryImpl implements FilteredBeanDescriptorFactory {
	
	private static class FilteredBeanDescriptorImpl implements FilteredBeanDescriptor {

		private final String description;
		private final Property[] properties;
		
		public FilteredBeanDescriptorImpl(String description, Property[] properties) {
			this.description = description;
			this.properties = properties;
		}

		@Override
		public String getDescription() {
			return description;
		}

		@Override
		public Property[] getProperties() {
			return properties;
		}

		@Override
		public boolean isCacheable() {
			return true;
		}
	}

	@Override
	public FilteredBeanDescriptor createDescription(SpearalContext context, SpearalPropertyFilter filter, Object value) {
		Class<?> type = value.getClass();
		Property[] properties = filter.get(type);
		String description = ClassDescriptionUtil.createAliasedDescription(context, type, properties);
		return new FilteredBeanDescriptorImpl(description, properties);
	}
}
