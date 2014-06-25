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

import org.spearal.SpearalContext;
import org.spearal.configuration.TypeLoader;
import org.spearal.impl.cache.CopyOnWriteValueMap;
import org.spearal.impl.cache.ValueMap.ValueProvider;
import org.spearal.impl.util.ClassDescriptionUtil;

/**
 * @author Franck WOLFF
 */
public class TypeLoaderImpl implements TypeLoader, ValueProvider<String, Class<?>> {

	private final ClassLoader classLoader;
	private final CopyOnWriteValueMap<String, Class<?>> classesCache;
	
	public TypeLoaderImpl() {
		this(TypeLoaderImpl.class.getClassLoader());
	}
	
	public TypeLoaderImpl(final ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.classesCache = new CopyOnWriteValueMap<String, Class<?>>(true, this);
	}

	@Override
	public Class<?> loadClass(SpearalContext context, String classNames) throws SecurityException {
		if (classNames == null || classNames.length() == 0)
			return null;
		
		Class<?> cls = classesCache.getOrPutIfAbsent(context, classNames);
		return (cls != ClassNotFound.class ? cls : null);
	}
	
	@Override
	public Class<?> createValue(SpearalContext context, String key) {
		Class<?> cls;
		
		String[] classNames = ClassDescriptionUtil.splitClassNames(key);
		if (classNames.length <= 1) {
			try {
				cls = Class.forName(context.getClassNameAlias(key), true, classLoader);
				if (cls.isInterface()) {
					if (Serializable.class.isAssignableFrom(cls))
						cls = Proxy.getProxyClass(classLoader, new Class<?>[]{ cls });
					else
						cls = Proxy.getProxyClass(classLoader, new Class<?>[]{ cls, Serializable.class });
				}
			}
			catch (ClassNotFoundException e) {
				cls = ClassNotFound.class;
			}
		}
		else {
			List<Class<?>> interfaces = new ArrayList<Class<?>>(classNames.length);
			
			for (int i = 0; i < classNames.length; i++) {
				if (isDuplicate(classNames, i))
					continue;
				
				Class<?> inter;
				try {
					inter = Class.forName(context.getClassNameAlias(classNames[i]), true, classLoader);
				}
				catch (ClassNotFoundException e) {
					continue;
				}
				if (!inter.isInterface()) {
					throw new UnsupportedOperationException(
						"Cannot create Proxy for: " + Arrays.toString(classNames) +
						" (" + inter + " isn't an interface)"
					);
				}
				interfaces.add(inter);
			}
			
			if (interfaces.size() == 0)
				cls = ClassNotFound.class;
			else
				cls = Proxy.getProxyClass(classLoader, interfaces.toArray(new Class<?>[0]));
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
	
	private interface ClassNotFound {
	}
}
