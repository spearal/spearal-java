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

import org.spearal.configuration.ConverterProvider;
import org.spearal.configuration.ConverterProvider.Converter;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class EnumConverter implements ConverterProvider, Converter<Enum<?>> {

	@Override
	public Converter<?> getConverter(Class<?> valueClass, Type targetType) {
		return (TypeUtil.classOfType(targetType).isEnum() ? this : null);
	}

	@Override
	public Enum<?> convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
		if (value == null)
			return null;
		
		if (value.getClass() == targetType)
			return (Enum<?>)value;
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		Class<Enum> targetEnumType = (Class<Enum>)TypeUtil.classOfType(targetType);
		
		try {
			@SuppressWarnings("unchecked")
			Enum<?> enumValue = Enum.valueOf(targetEnumType, value.toString());
			return enumValue;
		}
		catch (Exception e) {
			return null;
		}
	}
}
