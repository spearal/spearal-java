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
package org.spearal.configurable;

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
public interface PropertyFactory extends ConfigurableItem {
	
	public static interface Property {
		
		String getName();
		Class<?> getType();
		Type getGenericType();
		
		Field getField();
		Method getGetter();
		Method getSetter();
		
		Class<?> getDeclaringClass();
		
		<T> T initValue(Object obj, SpearalContext context)
			throws InstantiationException, IllegalAccessException, InvocationTargetException;
		<T> T getValue(Object obj)
			throws IllegalAccessException, InvocationTargetException;
		void setValue(Object obj, Object value)
			throws IllegalAccessException, InvocationTargetException;
		
		boolean isAnnotationPresent(Class<? extends Annotation> annotationClass);
		<A extends Annotation> A getAnnotation(Class<A> annotationClass);
		
		boolean isReadOnly();
		
		public void write(ExtendedSpearalEncoder out, Object obj)
			throws IOException, IllegalAccessException, InvocationTargetException;
			
		public void read(ExtendedSpearalDecoder in, Object obj, int type)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;
	}

	boolean canCreateProperty(Class<?> type);
	Property createProperty(String name, Field field, Method getter, Method setter);
}
