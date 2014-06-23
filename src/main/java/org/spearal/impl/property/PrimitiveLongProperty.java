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
public class PrimitiveLongProperty extends AnyProperty {

	public PrimitiveLongProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		encoder.writeLong(
			field != null ?
			field.getLong(holder) :
			((Long)getter.invoke(holder)).longValue()
		);
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
