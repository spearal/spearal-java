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
package org.spearal.impl.loader;

import java.io.Serializable;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.spearal.loader.ClassNotFoundProxy;
import org.spearal.loader.TypeLoader;

/**
 * @author Franck WOLFF
 */
public class TypeLoaderImpl implements TypeLoader {

	private final ClassLoader classLoader;
	private final Class<?> classNotFoundProxyClass;
	
	private final ConcurrentMap<String, Class<?>> classesCache;
	
	public TypeLoaderImpl() {
		this(TypeLoaderImpl.class.getClassLoader());
	}
	
	public TypeLoaderImpl(ClassLoader classLoader) {
		this.classLoader = classLoader;
		
		this.classNotFoundProxyClass = Proxy.getProxyClass(
			TypeLoaderImpl.class.getClassLoader(),
			new Class<?>[]{ ClassNotFoundProxy.class }
		);
		
		this.classesCache = new ConcurrentHashMap<String, Class<?>>();
	}

	@Override
	public Class<?> loadClass(String... classNames) throws SecurityException {
		if (classNames == null || classNames.length == 0)
			return classNotFoundProxyClass;
		return (classNames.length == 1 ? loadClass(classNames[0]) : loadClasses(classNames));
	}
	
	private Class<?> loadClass(String className) throws SecurityException {
		Class<?> cls = classesCache.get(className);
		
		if (cls == null) {
			try {
				cls = Class.forName(className, true, classLoader);
				if (cls.isInterface()) {
					if (Serializable.class.isAssignableFrom(cls))
						cls = Proxy.getProxyClass(classLoader, new Class<?>[]{ cls });
					else
						cls = Proxy.getProxyClass(classLoader, new Class<?>[]{ cls, Serializable.class });
				}
			}
			catch (ClassNotFoundException e) {
				cls = classNotFoundProxyClass;
			}
			
			Class<?> previous = classesCache.putIfAbsent(className, cls);
			if (previous != null)
				cls = previous;
		}

		return cls;
	}
	
	private Class<?> loadClasses(String[] classNames) throws SecurityException {
		String key = key(classNames);
		Class<?> cls = classesCache.get(key);
		
		if (cls == null) {
			List<Class<?>> interfaces = new ArrayList<Class<?>>(classNames.length);
			
			boolean serializable = false;
			for (int i = 0; i < classNames.length; i++) {
				if (isDuplicate(classNames, i))
					continue;
				
				Class<?> inter;
				
				try {
					inter = Class.forName(classNames[i], true, classLoader);
					if (Serializable.class.isAssignableFrom(inter))
						serializable = true;
				}
				catch (ClassNotFoundException e) {
					inter = ClassNotFoundProxy.class;
					serializable = true;
				}

				// TODO: create a Javassist proxy if one the classse isn't an interface?
				if (!inter.isInterface()) {
					throw new UnsupportedOperationException(
						"Cannot create Proxy for: " + Arrays.toString(classNames) +
						" (" + inter + " isn't an interface)"
					);
				}
				
				interfaces.add(inter);
			}
			
			if (!serializable)
				interfaces.add(Serializable.class);
			
			cls = Proxy.getProxyClass(classLoader, interfaces.toArray(new Class<?>[0]));
			
			Class<?> previous = classesCache.putIfAbsent(key, cls);
			if (previous != null)
				cls = previous;
		}
		
		return cls;
	}
	
	private static boolean isDuplicate(String[] classNames, int index) {
		String className = classNames[index];
		for (int i = 0; i < index; i++) {
			if (className.equals(classNames[i]))
				return true;
		}
		return false;
	}
	
	private static String key(String[] classNames) {
		StringBuilder sb = new StringBuilder(classNames[0]);
		for (int i = 1; i < classNames.length; i++)
			sb.append(':').append(classNames[i]);
		return sb.toString();
	}
}
