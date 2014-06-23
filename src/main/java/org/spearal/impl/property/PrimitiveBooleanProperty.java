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

import static org.spearal.impl.SpearalIType.ITYPE_FALSE;
import static org.spearal.impl.SpearalIType.ITYPE_TRUE;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

/**
 * @author Franck WOLFF
 */
public class PrimitiveBooleanProperty extends AnyProperty {

	public PrimitiveBooleanProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		encoder.writeBoolean(
			field != null ?
			field.getBoolean(holder) :
			((Boolean)getter.invoke(holder)).booleanValue()
		);
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