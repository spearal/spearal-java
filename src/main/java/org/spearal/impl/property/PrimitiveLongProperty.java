package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

public class PrimitiveLongProperty extends AbstractProperty {

	public PrimitiveLongProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			encoder.writeLong(field.getLong(holder));
		else
			encoder.writeLong(((Long)getter.invoke(holder)).longValue());
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.INTEGRAL)
				field.setLong(holder, decoder.readIntegral(parameterizedType));
			else
				field.setLong(holder, ((Long)decoder.readAny(parameterizedType, long.class)).longValue());
		}
		else if (setter != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.INTEGRAL)
				setter.invoke(holder, Long.valueOf(decoder.readIntegral(parameterizedType)));
			else
				setter.invoke(holder, decoder.readAny(parameterizedType, long.class));
		}
		else
			decoder.skipAny(parameterizedType);
	}
}
