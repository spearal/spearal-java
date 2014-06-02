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
package org.spearal.impl.instantiator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.configurable.PropertyInstantiator;
import org.spearal.configurable.TypeInstantiator;

/**
 * @author Franck WOLFF
 */
public class ProxyInstantiator implements TypeInstantiator, PropertyInstantiator {

	@Override
	public boolean canInstantiate(Type type) {
		return (type instanceof Class<?> && Proxy.isProxyClass((Class<?>)type));
	}

	@Override
	public Object instantiate(SpearalContext context, Type type) {
		Class<?> cls = (Class<?>)type;
        try {
    		Constructor<?> constructor = cls.getConstructor(new Class<?>[]{ InvocationHandler.class });
    		Collection<Property> properties = context.getProperties(cls);
    		return constructor.newInstance(new PropertiesInvocationHandler(context, properties));
		}
        catch (Exception e) {
			throw new RuntimeException("Could not create instance of: " + cls, e);
		}
	}

	@Override
	public boolean canInstantiate(Property property) {
		return canInstantiate(property.getGenericType());
	}

	@Override
	public Object instantiate(SpearalContext context, Property property) {
		return instantiate(context, property.getGenericType());
	}

	private static class PropertiesInvocationHandler implements InvocationHandler {
		
		private final SpearalContext context;
		private final Map<Method, String> methods;
		private final Map<String, Object> values;
		
		public PropertiesInvocationHandler(SpearalContext context, Collection<Property> properties) {
			this.context = context;
			this.methods = new HashMap<Method, String>();
			this.values = new HashMap<String, Object>();
			
			for (Property property : properties) {
				methods.put(property.getGetter(), property.getName());
				if (property.getSetter() != null)
					methods.put(property.getSetter(), property.getName());
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

			String propertyName = methods.get(method);
			if (propertyName != null) {
				if (method.getName().startsWith("set"))
					values.put(propertyName, args[0]);
				else
					return context.convert(values.get(propertyName), method.getReturnType());
			}
			
			return null;
		}
	}
}
