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
import java.math.BigInteger;

import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalType;

/**
 * @author Franck WOLFF
 */
public class BigIntegerProperty extends AbstractNonPrimitiveProperty {
	
	public static boolean canCreateWriter(Class<?> type) {
		return canCreateProperty(type);
	}
	
	public static ObjectWriter createWriter() {
		return new ObjectWriter() {
			@Override
			public void write(ExtendedSpearalEncoder out, Object o) throws IOException {
				out.writeBigInteger((BigInteger)o);
			}
		};
	}
	
	public static boolean canCreateProperty(Class<?> type) {
		return (type == BigInteger.class);
	}

	public BigIntegerProperty(String name, Field field, Method getter, Method setter) {
		super(name, field, getter, setter);
	}

	@Override
	protected void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		writeBigInteger(out, (BigInteger)field.get(obj));
	}

	@Override
	protected void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
		throws IOException, IllegalAccessException, InvocationTargetException {

		writeBigInteger(out, (BigInteger)getter.invoke(obj));
	}
	
	@Override
	protected boolean readObjectField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
		throws IOException, IllegalAccessException {
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			field.set(obj, null);
			return true;
			
		case INTEGRAL:
			field.set(obj, BigInteger.valueOf(in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			field.set(obj, in.readBigIntegral(parameterizedType));
			return true;
			
		case FLOATING:
			field.set(obj, BigInteger.valueOf((long)in.readFloating(parameterizedType)));
			return true;
			
		case BIG_FLOATING:
			field.set(obj, in.readBigFloating(parameterizedType).toBigInteger());
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
			setter.invoke(obj, BigInteger.valueOf(in.readIntegral(parameterizedType)));
			return true;
			
		case BIG_INTEGRAL:
			setter.invoke(obj, in.readBigIntegral(parameterizedType));
			return true;
			
		case FLOATING:
			setter.invoke(obj, BigInteger.valueOf((long)in.readFloating(parameterizedType)));
			return true;
			
		case BIG_FLOATING:
			setter.invoke(obj, in.readBigFloating(parameterizedType).toBigInteger());
			return true;
		
		default:
			return false;
		}
	}
	
	private static void writeBigInteger(ExtendedSpearalEncoder out, BigInteger value) throws IOException {
		if (value == null)
			out.writeNull();
		else
			out.writeBigInteger(value);
	}
}
