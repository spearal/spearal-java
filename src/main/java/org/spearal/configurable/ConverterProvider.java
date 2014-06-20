package org.spearal.configurable;

import java.lang.reflect.Type;

import org.spearal.impl.ExtendedSpearalDecoder;

public interface ConverterProvider extends Configurable {

	public interface Converter<T> {
		
		T convert(ExtendedSpearalDecoder decoder, Object value, Type targetType);
	}
	
	Converter<?> getConverter(Class<?> valueClass, Type targetType);
}
