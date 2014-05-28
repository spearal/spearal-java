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
public class ShortConverter implements Converter {

	@Override
	public boolean canConvert(Class<?> cls, Type target) {
		return
			(target == Short.class || target == short.class) &&
			(cls == null || cls == String.class || cls == Boolean.class ||
				Number.class.isAssignableFrom(cls));
	}

	@Override
	public Object convert(Object o, Type target) {
		if (o == null)
			return (target == short.class ? Short.valueOf((short)0) : null);
		
		Class<?> cls = o.getClass();
		if (cls == Short.class)
			return o;
		if (Number.class.isAssignableFrom(cls))
			return Short.valueOf(((Number)o).shortValue());
		if (cls == String.class) {
			try {
				return Short.valueOf(Short.parseShort((String)o));
			}
			catch (NumberFormatException e) {
				return Short.valueOf((short)0);
			}
		}
		if (cls == Character.class)
			return Short.valueOf((short)((Character)o).charValue());
		if (cls == Boolean.class)
			return Short.valueOf((short)(((Boolean)o).booleanValue() ? 1 : 0));

		return Short.valueOf((short)1);
	}
}
