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

import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalInput;
import org.spearal.impl.ExtendedSpearalOutput;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class CharArrayProperty extends AbstractNonPrimitiveProperty {

	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeString(String.valueOf((char[])o));
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return type == char[].class;
	}

	public CharArrayProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
		
		checkType(char[].class);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {

		writeCharArray(out, (char[])field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeCharArray(out, (char[])getter.invoke(obj));
	}

	@Override
	protected void readObjectField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			break;
		
		case STRING:
			field.set(obj, stringToCharArray(in.readString(parameterizedType)));
			break;
		
		default:
			field.set(obj, readAnyConvert(parameterizedType, in));
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
		
		case STRING:
			setter.invoke(obj, stringToCharArray(in.readString(parameterizedType)));
			break;
		
		default:
			setter.invoke(obj, readAnyConvert(parameterizedType, in));
			break;
		}
	}
	
	private static void writeCharArray(ExtendedSpearalOutput out, char[] value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeString(String.valueOf(value));
	}
	
	private static char[] stringToCharArray(String value) {
		return (value != null ? value.toCharArray() : null);
	}
}
