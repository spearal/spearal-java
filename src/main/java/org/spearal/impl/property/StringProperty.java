package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

public class StringProperty extends AbstractProperty {

	public StringProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		String value = (String)(field != null ? field.get(holder) : getter.invoke(holder));
		if (value == null)
			encoder.writeNull();
		else
			encoder.writeString(value);
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			field.set(holder, decoder.readAny(parameterizedType, String.class));
		else if (setter != null)
			setter.invoke(holder, decoder.readAny(parameterizedType, String.class));
		else
			decoder.skipAny(parameterizedType);
	}
}
