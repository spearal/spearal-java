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
import java.util.HashSet;
import java.util.Set;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

import org.spearal.SpearalContext;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.cache.CopyOnWriteKeyValueMap;
import org.spearal.impl.cache.KeyValueMap.ValueProvider;

/**
 * @author Franck WOLFF
 */
public class JavassistPartialObjectFactory implements PartialObjectFactory, ValueProvider<Class<?>, Class<?>> {
	
	private final CopyOnWriteKeyValueMap<Class<?>, Class<?>> proxyClassesCache;
	
	public JavassistPartialObjectFactory() {
		this.proxyClassesCache = new CopyOnWriteKeyValueMap<Class<?>, Class<?>>(true, this);
	}

	@Override
	public Class<?> createValue(SpearalContext context, Class<?> key) {
		context.getSecurizer().checkDecodable(key);
		
		ProxyFactory proxyFactory = new ProxyFactory();
		proxyFactory.setFilter(new PartialObjectFilter(context, key));
		proxyFactory.setSuperclass(key);
		proxyFactory.setInterfaces(new Class<?>[] { PartialObjectProxy.class });
		return proxyFactory.createClass();
	}

	@Override
	public Object instantiatePartial(ExtendedSpearalDecoder decoder, Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException {
		
		Class<?> proxyClass = proxyClassesCache.getOrPutIfAbsent(decoder.getContext(), cls);
		ProxyObject proxyObject = (ProxyObject)proxyClass.newInstance();
		proxyObject.setHandler(new PartialObjectProxyHandler(partialProperties));
		return proxyObject;
	}
	
	private static boolean isPartialObjectProxyMethod(Method method) {
		for (Method m : PartialObjectProxy.class.getMethods()) {
			if (m.equals(method))
				return true;
		}
		
		return false;
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
			return getters.contains(method) || isPartialObjectProxyMethod(method);
		}
	}
	
	private static class PartialObjectProxyHandler implements MethodHandler {

		private final Property[] partialProperties;
		private final Set<Method> partialGetters;
		private final Set<String> partialPropertiesNames;

		public PartialObjectProxyHandler(Property[] partialProperties) {
			this.partialProperties = partialProperties;
			
			this.partialGetters = new HashSet<Method>(partialProperties.length);
			for (Property property : partialProperties) {
				if (property.getGetter() != null)
					partialGetters.add(property.getGetter());
			}
			
			this.partialPropertiesNames = new HashSet<String>(partialProperties.length);
			for (Property property : partialProperties)
				partialPropertiesNames.add(property.getName());
		}

		public Object invoke(Object obj, Method method, Method proceed, Object[] args) throws Exception {
			if (partialGetters.contains(method))
				return proceed.invoke(obj, args);

			String name = method.getName();
			if ("$isDefined".equals(name))
				return Boolean.valueOf(partialPropertiesNames.contains(args[0]));
			if ("$getDefinedProperties".equals(name))
				return partialProperties;
			
			throw new UndefinedPropertyException(method.toString());
		}
	}
}
