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
package org.spearal.impl.property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.ExtendedSpearalDecoder;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractProperty implements Property {

	public static Class<?> typeOf(Field field, Method getter) {
		if (field != null)
			return field.getType();
		return getter.getReturnType();
	}

	public static Type genericTypeOf(Field field, Method getter) {
		return (field != null ? field.getGenericType() : getter.getGenericReturnType());
	}
	
	protected final String name;
	protected final Field field;
	protected final Method getter;
	protected final Method setter;
	
	protected final Class<?> type;
	protected final Type genericType;

	public AbstractProperty(String name, Field field, Method getter, Method setter) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Illegal property name: " + name);
		
		if (field != null)
			field.setAccessible(true);
		else if (getter == null)
			throw new IllegalArgumentException("Property '" + name + "' must have at least a field or a getter");

		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;
		
		this.type = typeOf(field, getter);
		this.genericType = genericTypeOf(field, getter);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getType() {
		return type;
	}

	@Override
	public Type getGenericType() {
		return genericType;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public Method getGetter() {
		return getter;
	}

	@Override
	public Method getSetter() {
		return setter;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return (field != null ? field.getDeclaringClass() : getter.getDeclaringClass());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T initValue(ExtendedSpearalDecoder decoder, Object holder)
		throws InstantiationException, IllegalAccessException, InvocationTargetException {

		Object value = decoder.getContext().instantiate(decoder, this);
		setValue(decoder.getContext(), holder, value);
		return (T)value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(SpearalContext context, Object holder)
		throws IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			return (T)field.get(holder);
		return (T)getter.invoke(holder);
	}

	@Override
	public void setValue(SpearalContext context, Object holder, Object value)
		throws IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			field.set(holder, value);
		else if (setter != null)
			setter.invoke(holder, value);
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		A annotation = (field != null ? field.getAnnotation(annotationClass) : null);
		if (annotation == null) {
			if (getter != null)
				annotation = getter.getAnnotation(annotationClass);
			if (annotation == null && setter != null)
				annotation = setter.getAnnotation(annotationClass);
		}
		return annotation;
	}

	@Override
	public boolean isReadOnly() {
		return (field == null && setter == null);
	}
}
