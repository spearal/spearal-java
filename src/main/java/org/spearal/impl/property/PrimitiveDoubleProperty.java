package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

public class PrimitiveDoubleProperty extends AbstractProperty {

	public PrimitiveDoubleProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			encoder.writeDouble(field.getDouble(holder));
		else
			encoder.writeDouble(((Double)getter.invoke(holder)).doubleValue());
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.FLOATING)
				field.setDouble(holder, decoder.readFloating(parameterizedType));
			else
				field.setDouble(holder, ((Double)decoder.readAny(parameterizedType, double.class)).doubleValue());
		}
		else if (setter != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.FLOATING)
				setter.invoke(holder, Double.valueOf(decoder.readFloating(parameterizedType)));
			else
				setter.invoke(holder, decoder.readAny(parameterizedType, double.class));
		}
		else
			decoder.skipAny(parameterizedType);
	}
}
