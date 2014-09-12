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
package org.spearal.impl.introspector;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PROTECTED;
import static java.lang.reflect.Modifier.STATIC;
import static java.lang.reflect.Modifier.TRANSIENT;
import static org.spearal.configuration.PropertyFactory.ZERO_PROPERTIES;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.spearal.SpearalContext;
import org.spearal.annotation.Exclude;
import org.spearal.annotation.Include;
import org.spearal.configuration.Introspector;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.cache.CopyOnWriteMap;

/**
 * @author Franck WOLFF
 */
public class IntrospectorImpl implements Introspector {
	
	private static Logger logger = Logger.getLogger(IntrospectorImpl.class.getName());

	private final CopyOnWriteMap<Class<?>, Object, Property[]> cache;

	public IntrospectorImpl() {
		this.cache = new CopyOnWriteMap<Class<?>, Object, Property[]>(true,
			new ValueProvider<Class<?>, Object, Property[]>() {
				@Override
				public Property[] createValue(SpearalContext context, Class<?> key, Object unused) {
					return (
						Proxy.isProxyClass(key)
						? introspectProxyProperties(context, key)
						: introspectBeanProperties(context, key)
					);
				}
			}
		);
	}
	
	@Override
	public Property[] getProperties(SpearalContext context, Class<?> cls) {
		Property[] properties = cache.get(cls);
		if (properties == null)
			properties = cache.putIfAbsent(context, cls, null);
		return properties;
	}
	
	protected Property[] introspectBeanProperties(SpearalContext context, Class<?> cls) {
		if (cls == Object.class || cls == null)
			return ZERO_PROPERTIES;
		
		Field[] declaredFields = cls.getDeclaredFields();
		Method[] declaredMethods = cls.getDeclaredMethods();
		
		SortedMap<String, Property> propertiesMap = new TreeMap<String, Property>();
		for (Field field : declaredFields) {
			if ((field.getModifiers() & (STATIC | TRANSIENT)) == 0 &&
				!field.isAnnotationPresent(Exclude.class)) {
				
				field.setAccessible(true);
				
				String name = field.getName();
				Class<?> type = field.getType();
				
				Method getter = findGetter(declaredMethods, type, name);
				Method setter = findSetter(declaredMethods, type, name);

				Property property = context.createProperty(name, field, getter, setter);
				
				propertiesMap.put(name, property);
			}
		}
		
		for (Method getter : declaredMethods) {
			if ((getter.getModifiers() & (STATIC | PRIVATE | PROTECTED)) == 0 &&
				getter.getReturnType() != void.class &&
				getter.isAnnotationPresent(Include.class) &&
				getter.getParameterTypes().length == 0) {
				
				String name = null;

				String methodName = getter.getName();
				if (getter.getReturnType() == boolean.class && methodName.startsWith("is"))
					name = decapitalize(methodName.substring(2));
				else if (methodName.startsWith("get"))
					name = decapitalize(methodName.substring(3));
				
				if (name == null || name.length() == 0) {
					logger.warning("Ignoring method annotated with @Include (illegal property getter name): " + methodName);
					continue;
				}
				if (propertiesMap.containsKey(name))
					continue;
				
				Class<?> type = getter.getReturnType();
				Method setter = findSetter(declaredMethods, type, name);

				Property property = context.createProperty(name, null, getter, setter);
				
				propertiesMap.put(name, property);
			}
		}
		
		Property[] properties = propertiesMap.values().toArray(ZERO_PROPERTIES);
		
		Class<?> superCls = cls.getSuperclass();
		if (superCls == Object.class || superCls == null)
			return properties;
		
		Property[] superProperties = getProperties(context, superCls);
		return concat(superProperties, properties);
	}

	protected Method findGetter(Method[] methods, Class<?> type, String name) {
		final String isName = "is" + name;
		final String getName = "get" + name;

		Method looseMatch = null;
		
		for (Method method : methods) {
			if ((method.getModifiers() & (STATIC | PRIVATE | PROTECTED)) == 0 &&
				method.getReturnType() == type &&
				method.getParameterTypes().length == 0) {
				
				if (type == boolean.class && method.getName().equalsIgnoreCase(isName)) {
					if (type.equals(decapitalize(method.getName().substring(2))))
						return method;
					looseMatch = method;
				}
				else if (method.getName().equalsIgnoreCase(getName)) {
					if (name.equals(decapitalize(method.getName().substring(3))))
						return method;
					looseMatch = method;
				}
			}
		}
		
		return looseMatch;
	}
	
	protected Method findSetter(Method[] methods, Class<?> type, String name) {
		final String setName = "set" + name;
		
		Method looseMatch = null;
		
		for (Method method : methods) {
			if ((method.getModifiers() & (STATIC | PRIVATE | PROTECTED)) == 0 &&
				method.getReturnType() == void.class) {
				Class<?>[] parameterTypes = method.getParameterTypes();
				if (parameterTypes.length == 1 &&
					parameterTypes[0] == type &&
					method.getName().equalsIgnoreCase(setName)) {
					
					if (name.equals(decapitalize(method.getName().substring(3))))
						return method;
					looseMatch = method;
				}
			}
		}
		
		return looseMatch;
	}
	
	protected Property[] introspectProxyProperties(SpearalContext context, Class<?> cls) {
		if (cls == null)
			return ZERO_PROPERTIES;
		
		Map<String, Method> setters = new HashMap<String, Method>();
		Map<String, Method> getters = new HashMap<String, Method>();
		for (Class<?> inter : cls.getInterfaces()) {
			for (Method method : inter.getMethods()) {
				
				if (method.getReturnType() == void.class) {
					String name = method.getName();
					if (name.length() > 3 && name.startsWith("set")) {
						Class<?>[] parameterTypes = method.getParameterTypes();
						if (parameterTypes.length == 1)
							setters.put(name.substring(3).toLowerCase(), method);
					}
				}
				else if (method.getParameterTypes().length == 0) {
					String name = method.getName();
					if (name.length() > 3 && name.startsWith("get"))
						getters.put(name.substring(3).toLowerCase(), method);
					else if (method.getReturnType() == boolean.class &&
							 name.length() > 2 && name.startsWith("is"))
						getters.put(name.substring(2).toLowerCase(), method);
				}
			}
		}
		
		SortedMap<String, Property> propertiesMap = new TreeMap<String, Property>();
		for (Map.Entry<String, Method> nameGetter : getters.entrySet()) {
			Method getter = nameGetter.getValue();
			Method setter = setters.get(nameGetter.getKey());
			
			if ((setter != null && setter.getParameterTypes()[0] == getter.getReturnType()) ||
				getter.isAnnotationPresent(Include.class)) {

				String name = (
					getter.getName().startsWith("is")
					? decapitalize(getter.getName().substring(2))
					: decapitalize(getter.getName().substring(3))
				);
				
				Property property = context.createProperty(name, null, getter, setter);
				propertiesMap.put(name, property);
			}
			
		}

		return propertiesMap.values().toArray(ZERO_PROPERTIES);
	}
	
	protected static Property[] concat(Property[] properties1, Property[] properties2) {
		int length1 = properties1.length,
			length2 = properties2.length;
		
		if (length1 == 0)
			return properties2;
		if (length2 == 0)
			return properties1;
		
		Property[] properties = new Property[length1 + length2];
		System.arraycopy(properties1, 0, properties, 0, length1);
		System.arraycopy(properties2, 0, properties, length1, length2);
		return properties;
	}
	
    private static String decapitalize(String name) {
        if (name == null || name.length() == 0)
            return name;
        
        if (name.length() > 1 && Character.isUpperCase(name.charAt(1)) && Character.isUpperCase(name.charAt(0)))
            return name;
        
        char chars[] = name.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }
}
