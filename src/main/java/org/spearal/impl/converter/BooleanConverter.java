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
public class BooleanConverter implements Converter {

	@Override
	public boolean canConvert(Class<?> cls, Type target) {
		return (target == Boolean.class || target == boolean.class);
	}

	@Override
	public Object convert(Object o, Type target) {
		if (o == null)
			return (target == boolean.class ? Boolean.FALSE : null);

		Class<?> cls = o.getClass();
		if (cls == Boolean.class)
			return o;
		if (cls == String.class)
			return Boolean.valueOf(Boolean.parseBoolean((String)o));
		if (Number.class.isAssignableFrom(cls))
			return Boolean.valueOf(((Number)o).intValue() != 0);
		if (cls == Character.class)
			return Boolean.valueOf(((Character)o).charValue() != '\0');

		return Boolean.TRUE;
	}
}
