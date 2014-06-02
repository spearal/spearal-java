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

import static org.spearal.impl.properties.AbstractProperty.AccessType.ObjectField;
import static org.spearal.impl.properties.AbstractProperty.AccessType.ObjectMethod;
import static org.spearal.impl.properties.AbstractProperty.AccessType.PrimitiveField;
import static org.spearal.impl.properties.AbstractProperty.AccessType.PrimitiveMethod;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.ExtendedSpearalEncoder;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractProperty implements Property {

	public static Class<?> getType(Field field, Method getter) {
		return (field != null ? field.getType() : getter.getReturnType());
	}

	public static Type getGenericType(Field field, Method getter) {
		return (field != null ? field.getGenericType() : getter.getGenericReturnType());
	}
	
	protected enum AccessType {
		PrimitiveField,
		ObjectField,
		PrimitiveMethod,
		ObjectMethod
	}
	
	private final String name;
	private final Field field;
	private final Method getter;
	private final Method setter;
	
	private final Class<?> type;
	private final Type genericType;
	private final AccessType getAccess;
	private final AccessType setAccess;

	public AbstractProperty(String name, Field field, Method getter, Method setter) {
		if (name == null || name.length() == 0)
			throw new IllegalArgumentException("Illegal property name: " + name);
		
		if (field != null)
			field.setAccessible(true);
		else if (getter == null)
			throw new IllegalArgumentException("Property '" + name + "' must have at least field or a getter");

		this.name = name;
		this.field = field;
		this.getter = getter;
		this.setter = setter;
		
		this.type = getType(field, getter);
		this.genericType = getGenericType(field, getter);
		this.getAccess = (field != null ?
			(type.isPrimitive() ? PrimitiveField : ObjectField) :
			(type.isPrimitive() ? PrimitiveMethod : ObjectMethod)
		);
		this.setAccess = (field != null ?
			(type.isPrimitive() ? PrimitiveField : ObjectField) :
			(type.isPrimitive() ? PrimitiveMethod : ObjectMethod)
		);
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
	public <T> T initValue(Object obj, SpearalContext context)
		throws InstantiationException, IllegalAccessException, InvocationTargetException {
		
		Object value = context.instantiate(this);
		setValue(obj, value);
		return (T)value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getValue(Object obj)
		throws IllegalAccessException, InvocationTargetException {
		
		if (getter != null)
			return (T)getter.invoke(obj);
		return (T)field.get(obj);
	}

	@Override
	public void setValue(Object obj, Object value)
		throws IllegalAccessException, InvocationTargetException {
		
		if (setter != null)
			setter.invoke(obj, value);
		if (field != null)
			field.set(obj, value);
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
	
	protected abstract void writePrimitiveField(ExtendedSpearalEncoder out, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void writeObjectField(ExtendedSpearalEncoder out, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void writePrimitiveMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
			throws IOException, IllegalAccessException, InvocationTargetException;

	protected abstract void writeObjectMethod(ExtendedSpearalEncoder out, Object obj, Method getter)
			throws IOException, IllegalAccessException, InvocationTargetException;
	
	@Override
	public void write(ExtendedSpearalEncoder out, Object obj)
		throws IOException, IllegalAccessException, InvocationTargetException {
		
		switch (getAccess) {
		
		case PrimitiveField:
			writePrimitiveField(out, obj, field);
			break;
			
		case ObjectField:
			writeObjectField(out, obj, field);
			break;
			
		case PrimitiveMethod:
			writePrimitiveMethod(out, obj, getter);
			break;
			
		case ObjectMethod:
			writeObjectMethod(out, obj, getter);
			break;
		}
	}

	protected abstract boolean readPrimitiveField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract boolean readObjectField(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract boolean readPrimitiveMethod(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Method setter)
			throws IOException, IllegalAccessException, InvocationTargetException;

	protected abstract boolean readObjectMethod(int parameterizedType, ExtendedSpearalDecoder in, Object obj, Method setter)
			throws IOException, IllegalAccessException, InvocationTargetException;
	
	@Override
	public void read(ExtendedSpearalDecoder in, Object obj, int parameterizedType)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		switch (setAccess) {
		
		case PrimitiveField:
			if (!readPrimitiveField(parameterizedType, in, obj, field))
				field.set(obj, in.getContext().convert(in.readAny(parameterizedType), genericType));
			break;
			
		case ObjectField:
			if (!readObjectField(parameterizedType, in, obj, field))
				field.set(obj, in.getContext().convert(in.readAny(parameterizedType), genericType));
			break;
			
		case PrimitiveMethod:
			if (setter == null)
				in.skipAny(parameterizedType);
			else if (!readPrimitiveMethod(parameterizedType, in, obj, setter))
				setter.invoke(obj, in.getContext().convert(in.readAny(parameterizedType), genericType));
			break;
			
		case ObjectMethod:
			if (setter == null)
				in.skipAny(parameterizedType);
			else if (!readObjectMethod(parameterizedType, in, obj, setter))
				setter.invoke(obj, in.getContext().convert(in.readAny(parameterizedType), genericType));
			break;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof AbstractProperty))
			return false;
		AbstractProperty p = (AbstractProperty)obj;
		return (
			name.equals(p.name) &&
			(field == null ? p.field == null : field.equals(p.field)) &&
			(getter == null ? p.getter == null : getter.equals(p.getter)) &&
			(setter == null ? p.setter == null : setter.equals(p.setter))
		);
	}

	@Override
	public String toString() {
		return getDeclaringClass().getName() + "." + name + " (" + getGenericType() + ")";
	}
}
