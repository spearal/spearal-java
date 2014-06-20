package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

public class CollectionProperty extends AbstractProperty {

	public CollectionProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		encoder.writeCollection((Collection<?>)getValue(encoder.getContext(), holder));
	}

	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (SpearalType.valueOf(parameterizedType) == SpearalType.COLLECTION)
			decoder.readCollection(parameterizedType, holder, this);
		else
			setValue(decoder.getContext(), holder, decoder.readAny(parameterizedType, genericType));
	}
}
