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
package org.spearal.impl.converter;

import java.lang.reflect.Type;

import org.spearal.configurable.Converter;

/**
 * @author Franck WOLFF
 */
public class LongConverter implements Converter {

	@Override
	public boolean canConvert(Class<?> cls, Type target) {
		return (target == Long.class || target == long.class);
	}

	@Override
	public Object convert(Object o, Type target) {
		if (o == null)
			return (target == long.class ? Long.valueOf(0L) : null);
		
		Class<?> cls = o.getClass();
		if (cls == Long.class)
			return o;
		if (Number.class.isAssignableFrom(cls))
			return Long.valueOf(((Number)o).longValue());
		if (cls == String.class) {
			try {
				return Long.valueOf(Long.parseLong((String)o));
			}
			catch (NumberFormatException e) {
				return Integer.valueOf(0);
			}
		}
		if (cls == Character.class)
			return Long.valueOf(((Character)o).charValue());
		if (cls == Boolean.class)
			return Long.valueOf(((Boolean)o).booleanValue() ? 1L : 0L);

		return Long.valueOf(1L);
	}
}
