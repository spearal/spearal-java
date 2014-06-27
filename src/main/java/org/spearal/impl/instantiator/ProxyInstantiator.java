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
import java.util.HashMap;
import java.util.Map;

import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiatorProvider;
import org.spearal.configuration.PropertyInstantiatorProvider.PropertyInstantiator;
import org.spearal.configuration.TypeInstantiatorProvider;
import org.spearal.configuration.TypeInstantiatorProvider.TypeInstantiator;
import org.spearal.impl.ExtendedSpearalDecoder;

/**
 * @author Franck WOLFF
 */
public class ProxyInstantiator implements
	TypeInstantiatorProvider, TypeInstantiator,
	PropertyInstantiatorProvider, PropertyInstantiator {

	@Override
	public TypeInstantiator getInstantiator(Type type) {
		return (canInstantiate(type) ? this : null);
	}

	@Override
	public Object instantiate(ExtendedSpearalDecoder decoder, Type type) {
		decoder.getContext().getSecurizer().checkDecodable(type);
		
        try {
    		Class<?> cls = (Class<?>)type;
    		Constructor<?> constructor = cls.getConstructor(new Class<?>[]{ InvocationHandler.class });
    		Property[] properties = decoder.getContext().getProperties(cls);
    		return constructor.newInstance(new PropertiesInvocationHandler(decoder, properties));
		}
        catch (Exception e) {
			throw new RuntimeException("Could not create instance of: " + type, e);
		}
	}

	@Override
	public PropertyInstantiator getInstantiator(Property property) {
		return (canInstantiate(property.getGenericType()) ? this : null);
	}

	@Override
	public Object instantiate(ExtendedSpearalDecoder decoder, Property property) {
		return instantiate(decoder, property.getGenericType());
	}

	private static boolean canInstantiate(Type type) {
		return (type instanceof Class<?> && Proxy.isProxyClass((Class<?>)type));
	}

	private static class PropertiesInvocationHandler implements InvocationHandler {
		
		private final ExtendedSpearalDecoder decoder;
		private final Map<Method, String> methods;
		private final Map<String, Object> values;
		
		public PropertiesInvocationHandler(ExtendedSpearalDecoder decoder, Property[] properties) {
			this.decoder = decoder;
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
					return decoder.getContext().convert(decoder, values.get(propertyName), method.getReturnType());
			}
			
			return null;
		}
	}
}
