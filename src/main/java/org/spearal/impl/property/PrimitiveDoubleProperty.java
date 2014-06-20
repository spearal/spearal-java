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
package org.spearal.impl.property;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
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
