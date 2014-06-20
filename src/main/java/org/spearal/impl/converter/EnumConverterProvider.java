package org.spearal.impl.converter;

import java.lang.reflect.Type;

import org.spearal.configurable.ConverterProvider;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.util.TypeUtil;

public class EnumConverterProvider implements ConverterProvider {

	private final Converter<Enum<?>> converter;
	
	public EnumConverterProvider() {
		
		this.converter = new Converter<Enum<?>>() {

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
		};
	}

	@Override
	public Converter<?> getConverter(Class<?> valueClass, Type targetType) {
		return (TypeUtil.classOfType(targetType).isEnum() ? converter : null);
	}
}
