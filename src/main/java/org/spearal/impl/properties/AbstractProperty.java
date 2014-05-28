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
import java.util.Arrays;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.ExtendedSpearalInput;
import org.spearal.impl.ExtendedSpearalOutput;
import org.spearal.impl.SpearalType;

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
	
	protected final String name;
	protected final Field field;
	protected final Method getter;
	protected final Method setter;
	
	protected final Class<?> type;
	protected final Type genericType;
	protected final AccessType getAccess;
	protected final AccessType setAccess;

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
	
	protected void checkType(Class<?>...supportedTypes) {
		for (Class<?> supportedType : supportedTypes) {
			if (supportedType == type)
				return;
		}
		throw new IllegalArgumentException(
			"Unsupported type " + type + " for property '" + name +
			"' (should be in " + Arrays.toString(supportedTypes) + ")"
		);	
	}
	
	protected void throwUnsupportedTypeException(Object obj, int parameterizedType) {
		throw new UnsupportedOperationException(
			"Cannot set value of type " + SpearalType.valueOf(parameterizedType) +
			" in property " + obj.getClass().getName() + "." + name
		);
	}
	
	protected void checkSetter(Object obj) {
		if (setter == null)
			throw new UnsupportedOperationException(obj.getClass().getName() + "." + name + " is read-only");
	}
	
	protected abstract void writePrimitiveField(ExtendedSpearalOutput out, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void writeObjectField(ExtendedSpearalOutput out, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void writePrimitiveMethod(ExtendedSpearalOutput out, Object obj, Method getter)
			throws IOException, IllegalAccessException, InvocationTargetException;

	protected abstract void writeObjectMethod(ExtendedSpearalOutput out, Object obj, Method getter)
			throws IOException, IllegalAccessException, InvocationTargetException;
	
	@Override
	public void write(ExtendedSpearalOutput out, Object obj)
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

	protected abstract void readPrimitiveField(int type, ExtendedSpearalInput in, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void readObjectField(int type, ExtendedSpearalInput in, Object obj, Field field)
			throws IOException, IllegalAccessException;

	protected abstract void readPrimitiveMethod(int type, ExtendedSpearalInput in, Object obj, Method setter)
			throws IOException, IllegalAccessException, InvocationTargetException;

	protected abstract void readObjectMethod(int type, ExtendedSpearalInput in, Object obj, Method setter)
			throws IOException, IllegalAccessException, InvocationTargetException;

	@SuppressWarnings("unchecked")
	protected <T> T readAnyConvert(int parameterizedType, ExtendedSpearalInput in)
		throws IOException {
		
		return (T)in.getContext().convert(in.readAny(parameterizedType), genericType);
	}
	
	@Override
	public void read(ExtendedSpearalInput in, Object obj, int type)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		switch (setAccess) {
		
		case PrimitiveField:
			readPrimitiveField(type, in, obj, field);
			break;
			
		case ObjectField:
			readObjectField(type, in, obj, field);
			break;
			
		case PrimitiveMethod:
			if (setter == null)
				in.skipAny(type);
			else
				readPrimitiveMethod(type, in, obj, setter);
			break;
			
		case ObjectMethod:
			if (setter == null)
				in.skipAny(type);
			else
				readObjectMethod(type, in, obj, setter);
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
