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
public class DoubleProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Double.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeDouble(((Double)o).doubleValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == double.class || type == Double.class);
	}

	public DoubleProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
		
		checkType(double.class, Double.class);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeDouble(field.getDouble(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeDouble(out, (Double)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeDouble(((Double)getter.invoke(obj)).doubleValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeDouble(out, (Double)getter.invoke(obj));
	}
	
	@Override
	protected void readPrimitiveField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.setDouble(obj, 0.0);
			break;
			
		case FLOATING:
			field.setDouble(obj,in.readFloating(parameterizedType));
			break;
		
		default:
			Double value = readAnyConvert(parameterizedType, in);
			field.setDouble(obj, (value != null ? value.doubleValue() : 0.0));
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
			
		case FLOATING:
			field.set(obj, Double.valueOf(in.readFloating(parameterizedType)));
			break;
		
		default:
			Double value = readAnyConvert(parameterizedType, in);
			field.set(obj, value);
			break;
		}
	}

	@Override
	protected void readPrimitiveMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, Double.valueOf(0.0));
			break;
		
		case FLOATING:
			setter.invoke(obj, Double.valueOf(in.readFloating(parameterizedType)));
			break;
		
		default:
			Double value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, (value != null ? value : Double.valueOf(0.0)));
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
			
		case FLOATING:
			setter.invoke(obj, Double.valueOf(in.readFloating(parameterizedType)));
			break;
		
		default:
			Double value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, value);
			break;
		}
	}
	
	private static void writeDouble(ExtendedSpearalOutput out, Double value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeDouble(value.doubleValue());
	}
}
