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
package org.spearal.configuration;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.spearal.SpearalContext;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

/**
 * @author Franck WOLFF
 */
public interface PropertyFactory extends Repeatable {
	
	public static final Property[] ZERO_PROPERTIES = new Property[0];
	
	public interface Property {
		
		String getName();
		Class<?> getType();
		Type getGenericType();
		
		Field getField();
		Method getGetter();
		Method getSetter();
		
		Class<?> getDeclaringClass();
		
		Object init(ExtendedSpearalDecoder decoder, Object holder)
			throws InstantiationException, IllegalAccessException, InvocationTargetException;
		
		Object get(SpearalContext context, Object holder)
			throws IllegalAccessException, InvocationTargetException;
		
		void set(SpearalContext context, Object holder, Object value)
			throws IllegalAccessException, InvocationTargetException;
		
		boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
		<A extends Annotation> A getAnnotation(Class<A> annotationClass);
		
		boolean isReadOnly();
		
		public void write(ExtendedSpearalEncoder encoder, Object holder)
			throws IOException, IllegalAccessException, InvocationTargetException;
			
		public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;
	}

	Property createProperty(String name, Field field, Method getter, Method setter);
}
