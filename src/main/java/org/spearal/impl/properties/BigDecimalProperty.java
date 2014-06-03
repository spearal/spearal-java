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
import java.math.BigDecimal;

import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class BigDecimalProperty extends AbstractNonPrimitiveProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeBigDecimal((BigDecimal)o);
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == BigDecimal.class);
	}

	public BigDecimalProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeBigDecimal(out, (BigDecimal)field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeBigDecimal(out, (BigDecimal)getter.invoke(obj));
	}
	
	@Override
	protected boolean readObjectField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			return true;
			
		case INTEGRAL:
			field.set(obj, BigDecimal.valueOf(in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			field.set(obj, new BigDecimal(in.readBigIntegral(parameterizedType)));
			return true;
			
		case FLOATING:
			double floating = in.readFloating(parameterizedType);
			if (Double.isNaN(floating) || Double.isInfinite(floating))
				field.set(obj, null);
			else
				field.set(obj, new BigDecimal(Double.toString(floating)));
			return true;
			
		case BIG_FLOATING:
			field.set(obj, in.readBigFloating(parameterizedType));
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
			setter.invoke(obj, BigDecimal.valueOf(in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			setter.invoke(obj, new BigDecimal(in.readBigIntegral(parameterizedType)));
			return true;
			
		case FLOATING:
			double floating = in.readFloating(parameterizedType);
			if (Double.isNaN(floating) || Double.isInfinite(floating))
				setter.invoke(obj, (Object)null);
			else
				setter.invoke(obj, new BigDecimal(Double.toString(floating)));
			return true;
			
		case BIG_FLOATING:
			setter.invoke(obj, in.readBigFloating(parameterizedType));
			return true;
		
		default:
			return false;
		}
	}
	
	private static void writeBigDecimal(ExtendedSpearalEncoder out, BigDecimal value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeBigDecimal(value);
	}
}
