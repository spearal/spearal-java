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

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

/**
 * @author Franck WOLFF
 */
public class AnyProperty implements Property {

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
	
	private final int hash;

	public AnyProperty(String name, Field field, Method getter, Method setter) {
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
		
		int hash = name.hashCode();
		hash += (31 * hash) + (field != null ? field.hashCode() : 0);
		hash += (31 * hash) + (getter != null ? getter.hashCode() : 0);
		hash += (31 * hash) + (setter != null ? setter.hashCode() : 0);
		this.hash = hash;
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
	public boolean hasField() {
		return field != null;
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public boolean hasGetter() {
		return getter != null;
	}

	@Override
	public Method getGetter() {
		return getter;
	}

	@Override
	public boolean hasSetter() {
		return setter != null;
	}

	@Override
	public Method getSetter() {
		return setter;
	}

	@Override
	public Class<?> getDeclaringClass() {
		return (field != null ? field.getDeclaringClass() : getter.getDeclaringClass());
	}

	@Override
	public Object init(ExtendedSpearalDecoder decoder, Object holder)
		throws InstantiationException, IllegalAccessException, InvocationTargetException {

		Object value = decoder.getContext().instantiate(this);
		set(holder, value);
		return value;
	}

	@Override
	public Object get(Object holder)
		throws IllegalAccessException, InvocationTargetException {
		
		return (field != null ? field.get(holder) : getter.invoke(holder));
	}

	@Override
	public void set(Object holder, Object value)
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

	@Override
	public void write(ExtendedSpearalEncoder encoder, Object holder)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		encoder.writeAny(field != null ? field.get(holder) : getter.invoke(holder));
	}
	
	@Override
	public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (field != null)
			field.set(holder, decoder.readAny(parameterizedType, genericType));
		else if (setter != null)
			setter.invoke(holder, decoder.readAny(parameterizedType, genericType));
		else
			decoder.skipAny(parameterizedType);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj.getClass() != getClass())
			return false;
		Property p = (Property)obj;
		return (
			name.equals(p.getName()) &&
			(field != null ? field.equals(p.getField()) : p.getField() == null) &&
			(getter != null ? getter.equals(p.getGetter()) : p.getGetter() == null) &&
			(setter != null ? setter.equals(p.getSetter()) : p.getSetter() == null)
		);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + name +
			" {field=" + field + ", getter=" + getter + ", setter=" + setter + "}";
	}
}
