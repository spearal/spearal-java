package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

public class PrimitiveIntProperty extends AbstractProperty {

	public PrimitiveIntProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			encoder.writeInt(field.getInt(holder));
		else
			encoder.writeInt(((Integer)getter.invoke(holder)).intValue());
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.INTEGRAL)
				field.setInt(holder, (int)decoder.readIntegral(parameterizedType));
			else
				field.setInt(holder, ((Integer)decoder.readAny(parameterizedType, int.class)).intValue());
		}
		else if (setter != null) {
			if (SpearalType.valueOf(parameterizedType) == SpearalType.INTEGRAL)
				setter.invoke(holder, Integer.valueOf((int)decoder.readIntegral(parameterizedType)));
			else
				setter.invoke(holder, decoder.readAny(parameterizedType, int.class));
		}
		else
			decoder.skipAny(parameterizedType);
	}
}
