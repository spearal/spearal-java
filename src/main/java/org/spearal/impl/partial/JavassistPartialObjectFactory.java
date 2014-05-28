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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.partial.PartialObjectFactory;
import org.spearal.partial.PartialObjectProxy;
import org.spearal.partial.UndefinedPropertyException;

/**
 * @author Franck WOLFF
 */
public class JavassistPartialObjectFactory implements PartialObjectFactory {
	
	private final ConcurrentMap<Class<?>, Class<?>> proxyClasses = new ConcurrentHashMap<Class<?>, Class<?>>();
	
	public JavassistPartialObjectFactory() {
	}

	@Override
	public Object instantiatePartial(SpearalContext ctx, Class<?> cls, Collection<Property> partialProperties)
		throws InstantiationException, IllegalAccessException {
		
		Class<?> proxyClass = proxyClasses.get(cls);
		if (proxyClass == null) {
			ProxyFactory proxyFactory = new ProxyFactory();
			proxyFactory.setFilter(new PartialObjectFilter(ctx, cls));
			proxyFactory.setSuperclass(cls);
			proxyFactory.setInterfaces(new Class<?>[] { PartialObjectProxy.class });
			proxyClass = proxyFactory.createClass();
			Class<?> previousProxyClass = proxyClasses.putIfAbsent(cls, proxyClass);
			if (previousProxyClass != null)
				proxyClass = previousProxyClass;
		}

		ProxyObject proxyObject = (ProxyObject)proxyClass.newInstance();
		proxyObject.setHandler(new PartialObjectProxyHandler(partialProperties));
		return proxyObject;
	}
	
	private static class PartialObjectFilter implements MethodFilter {

		private final Set<Method> getters;

		public PartialObjectFilter(SpearalContext ctx, Class<?> cls) {
			this.getters = new HashSet<Method>();
			
			for (Property property : ctx.getProperties(cls)) {
				if (property.getGetter() != null)
					getters.add(property.getGetter());
			}
		}

		@Override
		public boolean isHandled(Method method) {
			return method.getName().startsWith("spearal") || getters.contains(method);
		}
	}
	
	private static class PartialObjectProxyHandler implements MethodHandler {

		private final Collection<Property> partialProperties;
		private final Set<Method> partialGetters;
		private final Set<String> partialPropertiesNames;

		public PartialObjectProxyHandler(Collection<Property> partialProperties) {
			this.partialProperties = partialProperties;
			
			this.partialGetters = new HashSet<Method>(partialProperties.size());
			for (Property property : partialProperties) {
				if (property.getGetter() != null)
					partialGetters.add(property.getGetter());
			}
			
			this.partialPropertiesNames = new HashSet<String>(partialProperties.size());
			for (Property property : partialProperties)
				partialPropertiesNames.add(property.getName());
		}

		public Object invoke(Object obj, Method method, Method proceed, Object[] args) throws Exception {
			if (partialGetters.contains(method))
				return proceed.invoke(obj, args);

			String name = method.getName();
			if ("spearalIsDefined".equals(name))
				return Boolean.valueOf(partialPropertiesNames.contains(args[0]));
			if ("spearalGetDefinedProperties".equals(name))
				return partialProperties;
			
			throw new UndefinedPropertyException(method.toString());
		}
	}
}
