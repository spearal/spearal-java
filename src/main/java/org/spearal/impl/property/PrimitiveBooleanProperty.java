package org.spearal.impl.property;

import static org.spearal.impl.SpearalIType.ITYPE_FALSE;
import static org.spearal.impl.SpearalIType.ITYPE_TRUE;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

public class PrimitiveBooleanProperty extends AbstractProperty {

	public PrimitiveBooleanProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			encoder.writeBoolean(field.getBoolean(holder));
		else
			encoder.writeBoolean(((Boolean)getter.invoke(holder)).booleanValue());
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null) {
			if (parameterizedType == ITYPE_TRUE)
				field.setBoolean(holder, true);
			else if (parameterizedType == ITYPE_FALSE)
				field.setBoolean(holder, false);
			else
				field.setBoolean(holder, ((Boolean)decoder.readAny(parameterizedType, boolean.class)).booleanValue());
		}
		else if (setter != null) {
			if (parameterizedType == ITYPE_TRUE)
				setter.invoke(holder, Boolean.TRUE);
			else if (parameterizedType == ITYPE_FALSE)
				setter.invoke(holder, Boolean.FALSE);
			else
				setter.invoke(holder, decoder.readAny(parameterizedType, boolean.class));
		}
		else
			decoder.skipAny(parameterizedType);
	}
}
