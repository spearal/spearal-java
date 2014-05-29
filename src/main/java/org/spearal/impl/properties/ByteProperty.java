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
public class ByteProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Byte.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeByte(((Byte)o).byteValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == byte.class || type == Byte.class);
	}

	public ByteProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeByte(field.getByte(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeByte(out, (Byte)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeByte(((Byte)getter.invoke(obj)).byteValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeByte(out, (Byte)getter.invoke(obj));
	}
	
	@Override
	protected void readPrimitiveField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.setByte(obj, (byte)0);
			break;
			
		case INTEGRAL:
			field.setByte(obj, (byte)in.readIntegral(parameterizedType));
			break;
		
		default:
			Byte value = readAnyConvert(parameterizedType, in);
			field.setByte(obj, (value != null ? value.byteValue() : (byte)0));
			break;
		}
	}
	
	@Override
	protected void readObjectField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			break;
			
		case INTEGRAL:
			field.set(obj, Byte.valueOf((byte)in.readIntegral(parameterizedType)));
			break;
		
		default:
			Byte value = readAnyConvert(parameterizedType, in);
			field.set(obj, value);
			break;
		}
	}

	@Override
	protected void readPrimitiveMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, Byte.valueOf((byte)0));
			break;
		
		case INTEGRAL:
			setter.invoke(obj, Byte.valueOf((byte)in.readIntegral(parameterizedType)));
			break;
		
		default:
			Byte value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, (value != null ? value : Byte.valueOf((byte)0)));
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
			
		case INTEGRAL:
			setter.invoke(obj, Byte.valueOf((byte)in.readIntegral(parameterizedType)));
			break;
		
		default:
			Byte value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, value);
			break;
		}
	}
	
	private static void writeByte(ExtendedSpearalOutput out, Byte value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeByte(value.byteValue());
	}
}
