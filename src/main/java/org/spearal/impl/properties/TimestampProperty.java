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
import java.sql.Timestamp;

import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class TimestampProperty extends AbstractNonPrimitiveProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeTimestamp((Timestamp)o);
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return Timestamp.class.isAssignableFrom(type);
	}

	public TimestampProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeTimestamp(out, (Timestamp)field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeTimestamp(out, (Timestamp)getter.invoke(obj));
	}
	
	@Override
	protected boolean readObjectField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			return true;
			
		case DATE:
			if (getGenericType() == Timestamp.class) {
				field.set(obj, new Timestamp(in.readDate(parameterizedType).getTime()));
				return true;
			}
			break;
			
		case TIMESTAMP:
			if (getGenericType() == Timestamp.class) {
				field.set(obj, in.readTimestamp(parameterizedType));
				return true;
			}
			break;
		
		default:
			break;
		}
		
		return false;
	}

	@Override
	protected boolean readObjectMethod(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Method setter)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			setter.invoke(obj, (Object)null);
			return true;
			
		case DATE:
			if (getGenericType() == Timestamp.class) {
				setter.invoke(obj, new Timestamp(in.readDate(parameterizedType).getTime()));
				return true;
			}
			break;
			
		case TIMESTAMP:
			if (getGenericType() == Timestamp.class) {
				setter.invoke(obj, in.readTimestamp(parameterizedType));
				return true;
			}
			break;
		
		default:
			break;
		}

		return false;
	}
	
	
	private static void writeTimestamp(ExtendedSpearalEncoder out, Timestamp value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeTimestamp(value);
	}
}
