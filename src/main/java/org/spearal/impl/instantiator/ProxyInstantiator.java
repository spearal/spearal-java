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

import org.spearal.SpearalContext;
import org.spearal.configuration.PartialObjectFactory.UndefinedPropertyException;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiatorProvider;
import org.spearal.configuration.PropertyInstantiatorProvider.PropertyInstantiator;
import org.spearal.configuration.TypeInstantiatorProvider;
import org.spearal.configuration.TypeInstantiatorProvider.TypeInstantiator;

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
	public Object instantiate(SpearalContext context, Type type) {
		context.getSecurizer().checkDecodable(type);
		
        try {
    		Class<?> cls = (Class<?>)type;
    		Constructor<?> constructor = cls.getConstructor(new Class<?>[]{ InvocationHandler.class });
    		Property[] properties = context.getProperties(cls);
    		return constructor.newInstance(new PropertiesInvocationHandler(context, properties));
		}
        catch (Exception e) {
			throw new RuntimeException("Could not create instance of: " + type, e);
		}
	}
	
	public static Object instantiatePartial(SpearalContext context, Type type, Property[] serializedProperties) {
		context.getSecurizer().checkDecodable(type);
		
        try {
    		Class<?> cls = (Class<?>)type;
    		Constructor<?> constructor = cls.getConstructor(new Class<?>[]{ InvocationHandler.class });
    		Property[] properties = context.getProperties(cls);
    		return constructor.newInstance(new PropertiesInvocationHandler(context, properties, serializedProperties));
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
	public Object instantiate(SpearalContext context, Property property) {
		return instantiate(context, property.getGenericType());
	}

	private static boolean canInstantiate(Type type) {
		return (type instanceof Class<?> && Proxy.isProxyClass((Class<?>)type));
	}

	private static class PropertiesInvocationHandler implements InvocationHandler {
		
		private final SpearalContext context;
		private final Map<Method, String> methods;
		private final Map<String, Object> values;
		private final Property[] properties;
		private Map<String, Property> definedProperties = new HashMap<String, Property>();
		
		public PropertiesInvocationHandler(SpearalContext context, Property[] properties) {
			this(context, properties, null);
		}
		
		public PropertiesInvocationHandler(SpearalContext context, Property[] properties, Property[] definedProperties) {
			this.context = context;
			this.methods = new HashMap<Method, String>();
			this.values = new HashMap<String, Object>();
			this.properties = properties;
			
			for (Property property : properties) {
				methods.put(property.getGetter(), property.getName());
				if (property.getSetter() != null)
					methods.put(property.getSetter(), property.getName());
			}
			if (definedProperties != null) {
				for (Property property : definedProperties)
					this.definedProperties.put(property.getName(), property);
			}
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
			
			if ("$hasUndefinedProperties".equals(method.getName()) && method.getParameterTypes().length == 0) {
				return properties.length > definedProperties.size();
			}
			else if ("$isDefined".equals(method.getName()) && method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == String.class) {
				if (values.containsKey(args[0]))
					return true;
				if (definedProperties.containsKey(args[0]))
					return true;						
				return false;
			}
			else if ("$getDefinedProperties".equals(method.getName()) && method.getParameterTypes().length == 0)
				return definedProperties.values().toArray(new Property[definedProperties.size()]);
			
			String propertyName = methods.get(method);
			if (propertyName != null) {
				if (method.getName().startsWith("set")) {
					values.put(propertyName, args[0]);
					
					Property property = null;
					for (Property p : properties) {
						if (p.getName().equals(propertyName)) {
							property = p;
							break;
						}
					}
					definedProperties.put(propertyName, property);
				}
				else {
					if (!values.containsKey(propertyName))
						throw new UndefinedPropertyException(method.toString());
					
					return context.convert(values.get(propertyName), method.getReturnType());
				}
			}
			
			return null;
		}
	}
}
