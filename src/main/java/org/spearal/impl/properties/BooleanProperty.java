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
public class BooleanProperty extends AbstractProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return type == Boolean.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeBoolean(((Boolean)o).booleanValue());
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == boolean.class || type == Boolean.class);
	}

	public BooleanProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeBoolean(field.getBoolean(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeBoolean(out, (Boolean)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeBoolean(((Boolean)getter.invoke(obj)).booleanValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeBoolean(out, (Boolean)getter.invoke(obj));
	}
	
	@Override
	protected boolean readPrimitiveField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
		case FALSE:
			field.setBoolean(obj, false);
			return true;
		
		case TRUE:
			field.setBoolean(obj, true);
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
			
		case FALSE:
			field.set(obj, Boolean.FALSE);
			return true;
		
		case TRUE:
			field.set(obj, Boolean.TRUE);
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
		case FALSE:
			setter.invoke(obj, Boolean.FALSE);
			return true;
		
		case TRUE:
			setter.invoke(obj, Boolean.TRUE);
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
			
		case FALSE:
			setter.invoke(obj, Boolean.FALSE);
			return true;
		
		case TRUE:
			setter.invoke(obj, Boolean.TRUE);
			return true;
		
		default:
			return false;
		}
	}
	
	
	private static void writeBoolean(ExtendedSpearalEncoder out, Boolean value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeBoolean(value.booleanValue());
	}
}
