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

public interface PropertyFactory extends Configurable {
	
	public static interface Property {
		
		String getName();
		Class<?> getType();
		Type getGenericType();
		
		Field getField();
		Method getGetter();
		Method getSetter();
		
		Class<?> getDeclaringClass();
		
		<T> T initValue(ExtendedSpearalDecoder decoder, Object holder)
			throws InstantiationException, IllegalAccessException, InvocationTargetException;
		
		<T> T getValue(SpearalContext context, Object holder)
			throws IllegalAccessException, InvocationTargetException;
		
		void setValue(SpearalContext context, Object holder, Object value)
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
