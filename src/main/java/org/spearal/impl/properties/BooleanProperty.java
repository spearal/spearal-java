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
public class BooleanProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Boolean.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeBoolean(((Boolean)o).booleanValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == boolean.class || type == Boolean.class);
	}

	public BooleanProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
		
		checkType(boolean.class, Boolean.class);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeBoolean(field.getBoolean(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeBoolean(out, (Boolean)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeBoolean(((Boolean)getter.invoke(obj)).booleanValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeBoolean(out, (Boolean)getter.invoke(obj));
	}
	
	@Override
	protected void readPrimitiveField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
		case FALSE:
			field.setBoolean(obj, false);
			break;
		
		case TRUE:
			field.setBoolean(obj, true);
			break;
		
		default:
			Boolean value = readAnyConvert(parameterizedType, in);
			field.setBoolean(obj, (value != null ? value.booleanValue() : false));
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
			
		case FALSE:
			field.set(obj, Boolean.FALSE);
			break;
		
		case TRUE:
			field.set(obj, Boolean.TRUE);
			break;
		
		default:
			Boolean value = readAnyConvert(parameterizedType, in);
			field.set(obj, value);
			break;
		}
	}
	
	@Override
	protected void readPrimitiveMethod(int parameterizedType, ExtendedSpearalInput in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
		case FALSE:
			setter.invoke(obj, Boolean.FALSE);
			break;
		
		case TRUE:
			setter.invoke(obj, Boolean.TRUE);
			break;
		
		default:
			Boolean value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, (value != null ? value : Boolean.FALSE));
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
			
		case FALSE:
			setter.invoke(obj, Boolean.FALSE);
			break;
		
		case TRUE:
			setter.invoke(obj, Boolean.TRUE);
			break;
		
		default:
			Boolean value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, value);
			break;
		}
	}
	
	
	private static void writeBoolean(ExtendedSpearalOutput out, Boolean value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeBoolean(value.booleanValue());
	}
}
