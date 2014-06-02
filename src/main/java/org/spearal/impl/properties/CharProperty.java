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
public class CharProperty extends AbstractProperty {

	public static boolean canCreateWriter(Class<?> type) {
		return type == Character.class;
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeString(String.valueOf(((Character)o).charValue()));
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return type == char.class || type == Character.class;
	}

	public CharProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writePrimitiveField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		out.writeChar(field.getChar(obj));
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeCharacter(out, (Character)field.get(obj));
	}

	@Override
	protected void writePrimitiveMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		out.writeChar(((Character)getter.invoke(obj)).charValue());
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeCharacter(out, (Character)getter.invoke(obj));
	}
	
	@Override
	protected boolean readPrimitiveField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.setChar(obj, '\0');
			return true;
			
		case STRING:
			field.setChar(obj, stringToChar(in.readString(parameterizedType)));
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
			
		case STRING:
			field.set(obj, stringToCharacter(in.readString(parameterizedType)));
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
			setter.invoke(obj, Character.valueOf('\0'));
			return true;
		
		case STRING:
			setter.invoke(obj, stringToCharacter(in.readString(parameterizedType)));
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
			
		case STRING:
			setter.invoke(obj, stringToCharacter(in.readString(parameterizedType)));
			return true;
		
		default:
			return false;
		}
	}
	
	private static void writeCharacter(ExtendedSpearalEncoder out, Character value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeChar(value.charValue());
	}
	
	private static char stringToChar(String value) {
		if (value == null || value.length() == 0)
			return '\0';
		return value.charAt(0);
	}
	
	private static Character stringToCharacter(String value) {
		if (value == null)
			return null;
		if (value.length() == 0)
			return Character.valueOf('\0');
		return Character.valueOf(value.charAt(0));
	}
}
