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
public class ShortProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Short.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeShort(((Short)o).shortValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == short.class || type == Short.class);
	}

	public ShortProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeShort(field.getShort(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeShort(out, (Short)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeShort(((Short)getter.invoke(obj)).shortValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeShort(out, (Short)getter.invoke(obj));
	}
	
	@Override
	protected boolean readPrimitiveField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.setShort(obj, (short)0);
			return true;
			
		case INTEGRAL:
			field.setShort(obj, (short)in.readIntegral(parameterizedType));
			return true;
			
		case FLOATING:
			field.setShort(obj, (short)in.readFloating(parameterizedType));
			return true;
		
		default:
			return false;
		}
	}
	
	@Override
	protected boolean readObjectField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			return true;
			
		case INTEGRAL:
			field.set(obj, Short.valueOf((short)in.readIntegral(parameterizedType)));
			return true;
			
		case FLOATING:
			field.set(obj, Short.valueOf((short)in.readFloating(parameterizedType)));
			return true;
		
		default:
			return false;
		}
	}

	@Override
	protected boolean readPrimitiveMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, Short.valueOf((short)0));
			return true;
		
		case INTEGRAL:
			setter.invoke(obj, Short.valueOf((short)in.readIntegral(parameterizedType)));
			return true;
			
		case FLOATING:
			setter.invoke(obj, Short.valueOf((short)in.readFloating(parameterizedType)));
			return true;
		
		default:
			return false;
		}
	}

	@Override
	protected boolean readObjectMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, (Object)null);
			return true;
			
		case INTEGRAL:
			setter.invoke(obj, Short.valueOf((short)in.readIntegral(parameterizedType)));
			return true;
			
		case FLOATING:
			setter.invoke(obj, Short.valueOf((short)in.readFloating(parameterizedType)));
			return true;
		
		default:
			return false;
		}
	}
	
	private static void writeShort(ExtendedSpearalOutput out, Short value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeShort(value.shortValue());
	}
}
