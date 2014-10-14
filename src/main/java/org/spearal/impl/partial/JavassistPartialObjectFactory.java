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
package org.spearal.impl.partial;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.spearal.SpearalContext;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.cache.CopyOnWriteMap;
import org.spearal.impl.instantiator.ProxyInstantiator;

/**
 * @author Franck WOLFF
 */
public class JavassistPartialObjectFactory implements PartialObjectFactory, ValueProvider<Class<?>, Object, Class<?>> {

	private final CopyOnWriteMap<Class<?>, Object, Class<?>> proxyClassesCache;
	
	public JavassistPartialObjectFactory() {
		this.proxyClassesCache = new CopyOnWriteMap<Class<?>, Object, Class<?>>(true, this);
	}

	@Override
	public Class<?> createValue(SpearalContext context, Class<?> key, Object unused) {
		context.getSecurizer().checkDecodable(key);
		
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setFilter(new PartialObjectFilter(context, key));
		proxyFactory.setSuperclass(key);
		proxyFactory.setInterfaces(new Class<?>[] { ExtendedPartialObjectProxy.class });
		return proxyFactory.createClass();
	}

	@Override
	public Object instantiatePartial(SpearalContext context, Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException {
		
		if (Proxy.isProxyClass(cls))
			return ProxyInstantiator.instantiatePartial(context, cls, partialProperties);
		
		Class<?> proxyClass = proxyClassesCache.getOrPutIfAbsent(context, cls);
		ProxyObject proxyObject = (ProxyObject)proxyClass.newInstance();
		proxyObject.setHandler(new PartialObjectProxyHandler(context, cls, partialProperties));
		return proxyObject;
	}
	
	private static class PartialObjectFilter implements MethodFilter {
		
		private static final Method[] partialObjectProxyMethods = PartialObjectProxy.class.getMethods();

		private final Set<Method> accessors;

		public PartialObjectFilter(SpearalContext ctx, Class<?> cls) {
			this.accessors = new HashSet<Method>();
			
			for (Property property : ctx.getProperties(cls)) {
				if (property.hasGetter())
					accessors.add(property.getGetter());
				if (property.hasSetter())
					accessors.add(property.getSetter());
			}
		}

		@Override
		public boolean isHandled(Method method) {
			return accessors.contains(method) || isPartialObjectProxyMethod(method);
		}
		
		private static boolean isPartialObjectProxyMethod(Method method) {
			for (Method partialObjectProxyMethod : partialObjectProxyMethods) {
				if (partialObjectProxyMethod.equals(method))
					return true;
			}
			return false;
		}
	}
	
	private static class PartialObjectProxyHandler implements MethodHandler {

		private final Property[] allProperties;
		private final Map<String, Property> definedProperties;

		public PartialObjectProxyHandler(SpearalContext context, Class<?> cls, Property[] partialProperties) {
			this.allProperties = context.getProperties(cls);
			
			this.definedProperties = new HashMap<String, Property>(partialProperties.length);
			for (Property property : partialProperties) {
				if (property != null)
					this.definedProperties.put(property.getName(), property);
			}
		}

		public Object invoke(Object obj, Method method, Method proceed, Object[] args) throws Exception {

			// Proxy methods.
			if (method.getDeclaringClass() == PartialObjectProxy.class) {
				String name = method.getName();
				if ("$hasUndefinedProperties".equals(name))
					return Boolean.valueOf(definedProperties.size() < allProperties.length);
				if ("$isDefined".equals(name))
					return Boolean.valueOf(definedProperties.containsKey(args[0]));
				if ("$getDefinedProperties".equals(name))
					return definedProperties.values().toArray(new Property[definedProperties.size()]);
				if ("$getActualClass".equals(name))
					return obj.getClass().getSuperclass();
				throw new UnsupportedOperationException("Internal error: " + method.toString());
			}
			
			// Setters.
			if (method.getReturnType() == void.class) {
				for (Property property : allProperties) {
					if (method.equals(property.getSetter())) {
						proceed.invoke(obj, args);
						definedProperties.put(property.getName(), property);
						return null;
					}
				}
				throw new UnsupportedOperationException("Internal error: " + method.toString());
			}
			
			// Getters.
			for (Property property : definedProperties.values()) {
				if (method.equals(property.getGetter()))
					return proceed.invoke(obj, args);
			}
			
			throw new UndefinedPropertyException(method.toString());
		}
	}
}
