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
package org.spearal.impl.properties;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.spearal.SpearalType;
import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalInput;
import org.spearal.impl.ExtendedSpearalOutput;

/**
 * @author Franck WOLFF
 */
public class EnumProperty extends AbstractNonPrimitiveProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeEnum((Enum<?>)o);
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return Enum.class.isAssignableFrom(type);
	}

	public EnumProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeEnum(out, (Enum<?>)field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeEnum(out, (Enum<?>)getter.invoke(obj));
	}
	
	@Override
	protected void readObjectField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			break;
			
		case ENUM:
			field.set(obj, in.readEnum(parameterizedType));
			break;
		
		default:
			Object value = readAnyConvert(parameterizedType, in);
			field.set(obj, value);
			break;
		}
	}

	@Override
	protected void readObjectMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, (Object)null);
			break;
			
		case ENUM:
			setter.invoke(obj, in.readEnum(parameterizedType));
			break;
		
		default:
			Object value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, value);
			break;
		}
	}

	private static void writeEnum(ExtendedSpearalOutput out, Enum<?> value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeEnum(value);
	}
}
