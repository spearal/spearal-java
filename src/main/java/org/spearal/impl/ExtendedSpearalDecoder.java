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
package org.spearal.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.spearal.SpearalDecoder;
import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface ExtendedSpearalDecoder extends SpearalDecoder {

	Object readAny(int parameterizedType) throws IOException;
	Object readAny(int parameterizedType, Type targetType) throws IOException;
	void skipAny(int parameterizedType) throws IOException;
	
	SpearalDateTime readDateTime(int parameterizedType) throws IOException;
	
	long readIntegral(int parameterizedType) throws IOException;
	
	BigInteger readBigIntegral(int parameterizedType) throws IOException;
	
	double readFloating(int parameterizedType) throws IOException;
	
	BigDecimal readBigFloating(int parameterizedType) throws IOException;

	String readString(int parameterizedType) throws IOException;
	
	byte[] readByteArray(int parameterizedType) throws IOException;
	
	Collection<?> readCollection(int parameterizedType, Type targetType) throws IOException;
	void readCollection(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;

	Map<?, ?> readMap(int parameterizedType, Type targetType) throws IOException;
	void readMap(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;
	
	Enum<?> readEnum(int parameterizedType, Type targetType) throws IOException;

	Class<?> readClass(int parameterizedType, Type targetType) throws IOException;
	
	Object readBean(int parameterizedType, Type targetType) throws IOException;
}
