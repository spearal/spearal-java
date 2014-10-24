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
package org.spearal.configuration;

import org.spearal.SpearalContext;
import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface PartialObjectFactory extends Configurable {

	public interface PartialObjectProxy {

		boolean $hasUndefinedProperties();
		boolean $isDefined(String propertyName);
		boolean $undefine(String propertyName);
		Property[] $getDefinedProperties();
	}
	
	public interface ExtendedPartialObjectProxy extends PartialObjectProxy {
		
		Class<?> $getActualClass();
	}
	
	public static class UndefinedPropertyException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public UndefinedPropertyException(String propertyName) {
			super("Property '" + propertyName + "' is undefined");
		}
	}

	Object instantiatePartial(SpearalContext context, Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException;
}
