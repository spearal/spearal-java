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
import java.lang.reflect.Proxy;

import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalInput;
import org.spearal.impl.ExtendedSpearalOutput;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class BeanProperty extends AbstractNonPrimitiveProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalOutput out, Object o) throws IOException {
				out.writeBean(o);
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return !(type.isAnnotation() || type.isPrimitive() || type.isArray() || type.isEnum() || Proxy.isProxyClass(type));
	}

	public BeanProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
		
		if (!canCreateProperty(getType(field, getter)))
			throw new RuntimeException("Not a Bean property: " + getType(field, getter) + " " + name);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeBean(out, field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeBean(out, getter.invoke(obj));
	}
	
	@Override
	protected void readObjectField(int parameterizedType, ExtendedSpearalInput in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			break;
			
		case BEAN:
			field.set(obj, in.readBean(parameterizedType));
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
			
		case BEAN:
			setter.invoke(obj, in.readBean(parameterizedType));
			break;
		
		default:
			Object value = readAnyConvert(parameterizedType, in);
			setter.invoke(obj, value);
			break;
		}
	}

	private static void writeBean(ExtendedSpearalOutput out, Object value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeBean(value);
	}
}
