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

import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.spearal.SpearalContext;
import org.spearal.configuration.TypeLoader;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.cache.CopyOnWriteMap;
import org.spearal.impl.util.ClassDescriptionUtil;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class TypeLoaderImpl implements TypeLoader, ValueProvider<String, Type, Class<?>> {

	private final ClassLoader classLoader;
	private final CopyOnWriteMap<String, Type, Class<?>> classesCache;
	
	public TypeLoaderImpl() {
		this(TypeLoaderImpl.class.getClassLoader());
	}
	
	public TypeLoaderImpl(final ClassLoader classLoader) {
		this.classLoader = classLoader;
		this.classesCache = new CopyOnWriteMap<String, Type, Class<?>>(true, this);
	}

	@Override
	public Class<?> loadClass(SpearalContext context, String classNames, Type targetType) throws SecurityException {
		if (classNames == null || classNames.length() == 0)
			return null;
		
		Class<?> cls = classesCache.getOrPutIfAbsent(context, classNames, targetType);
		return (cls != ClassNotFound.class ? cls : null);
	}
	
	@Override
	public Class<?> createValue(SpearalContext context, String key, Type targetType) {
		Class<?> cls;
		
		String[] classNames = ClassDescriptionUtil.splitClassNames(key);
		if (classNames.length <= 1) {
			try {
				cls = Class.forName(context.unalias(key), true, classLoader);
				if (cls.isInterface())
					cls = Proxy.getProxyClass(classLoader, new Class<?>[]{ cls });
			}
			catch (ClassNotFoundException e) {
				cls = null;
			}
		}
		else {
			List<Class<?>> interfaces = new ArrayList<Class<?>>(classNames.length);
			
			for (int i = 0; i < classNames.length; i++) {
				if (isDuplicate(classNames, i))
					continue;
				
				Class<?> inter;
				try {
					inter = Class.forName(context.unalias(classNames[i]), true, classLoader);
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
				cls = null;
			else
				cls = Proxy.getProxyClass(classLoader, interfaces.toArray(new Class<?>[0]));
		}
		
		if (cls == null) {
			if (targetType == null)
				cls = ClassNotFound.class;
			else {
				cls = TypeUtil.classOfType(targetType);
				if (cls.isInterface())
					cls = Proxy.getProxyClass(cls.getClassLoader(), new Class<?>[]{ cls });
			}
			
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
