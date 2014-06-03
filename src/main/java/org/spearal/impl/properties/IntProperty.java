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
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class IntProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Integer.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeInt(((Integer)o).intValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == int.class || type == Integer.class);
	}

	public IntProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeInt(field.getInt(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeInteger(out, (Integer)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeInt(((Integer)getter.invoke(obj)).intValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeInteger(out, (Integer)getter.invoke(obj));
	}
	
	@Override
	protected boolean readPrimitiveField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.setInt(obj, 0);
			return true;
			
		case INTEGRAL:
			field.setInt(obj, (int)in.readIntegral(parameterizedType));
			return true;
			
		case BIG_INTEGRAL:
			field.setInt(obj, in.readBigIntegral(parameterizedType).intValue());
			return true;
			
		case FLOATING:
			field.setInt(obj, (int)in.readFloating(parameterizedType));
			return true;
			
		case BIG_FLOATING:
			field.setInt(obj, in.readBigFloating(parameterizedType).intValue());
			return true;
		
		default:
			return false;
		}
	}
	
	@Override
	protected boolean readObjectField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			return true;
			
		case INTEGRAL:
			field.set(obj, Integer.valueOf((int)in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			field.set(obj, Integer.valueOf(in.readBigIntegral(parameterizedType).intValue()));
			return true;
			
		case FLOATING:
			field.set(obj, Integer.valueOf((int)in.readFloating(parameterizedType)));
			return true;
			
		case BIG_FLOATING:
			field.set(obj, Integer.valueOf(in.readBigFloating(parameterizedType).intValue()));
			return true;
		
		default:
			return false;
		}
	}

	@Override
	protected boolean readPrimitiveMethod(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, Integer.valueOf(0));
			return true;
		
		case INTEGRAL:
			setter.invoke(obj, Integer.valueOf((int)in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			setter.invoke(obj, Integer.valueOf(in.readBigIntegral(parameterizedType).intValue()));
			return true;
			
		case FLOATING:
			setter.invoke(obj, Integer.valueOf((int)in.readFloating(parameterizedType)));
			return true;
			
		case BIG_FLOATING:
			setter.invoke(obj, Integer.valueOf(in.readBigFloating(parameterizedType).intValue()));
			return true;
		
		default:
			return false;
		}
	}

	@Override
	protected boolean readObjectMethod(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, (Object)null);
			return true;
			
		case INTEGRAL:
			setter.invoke(obj, Integer.valueOf((int)in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			setter.invoke(obj, Integer.valueOf(in.readBigIntegral(parameterizedType).intValue()));
			return true;
			
		case FLOATING:
			setter.invoke(obj, Integer.valueOf((int)in.readFloating(parameterizedType)));
			return true;
			
		case BIG_FLOATING:
			setter.invoke(obj, Integer.valueOf(in.readBigFloating(parameterizedType).intValue()));
			return true;
		
		default:
			return false;
		}
	}
	
	private static void writeInteger(ExtendedSpearalEncoder out, Integer value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeInt(value.intValue());
	}
}
