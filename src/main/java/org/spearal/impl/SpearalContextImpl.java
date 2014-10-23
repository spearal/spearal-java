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
package org.spearal.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.configuration.AliasStrategy;
import org.spearal.configuration.CoderProvider;
import org.spearal.configuration.CoderProvider.Coder;
import org.spearal.configuration.Configurable;
import org.spearal.configuration.ConverterProvider;
import org.spearal.configuration.ConverterProvider.Converter;
import org.spearal.configuration.FilteredBeanDescriptorFactory;
import org.spearal.configuration.FilteredBeanDescriptorFactory.FilteredBeanDescriptor;
import org.spearal.configuration.Introspector;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PartialObjectFactory.PartialObjectProxy;
import org.spearal.configuration.PropertyFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiatorProvider;
import org.spearal.configuration.PropertyInstantiatorProvider.PropertyInstantiator;
import org.spearal.configuration.Repeatable;
import org.spearal.configuration.Securizer;
import org.spearal.configuration.TypeInstantiatorProvider;
import org.spearal.configuration.TypeInstantiatorProvider.TypeInstantiator;
import org.spearal.configuration.TypeLoader;
import org.spearal.configuration.UnfilterablePropertiesProvider;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.cache.CopyOnWriteDualIdentityMap;
import org.spearal.impl.cache.CopyOnWriteMap;
import org.spearal.impl.cache.DualIdentityMap;

/**
 * @author Franck WOLFF
 * @author William DRAI
 */
public class SpearalContextImpl implements SpearalContext {
	
	private Introspector introspector;
	private TypeLoader loader;
	private Securizer securizer;
	private PartialObjectFactory partialObjectFactory;
	private AliasStrategy aliasStrategy;
	
	private final List<TypeInstantiatorProvider> typeInstantiatorProviders;
	private final CopyOnWriteMap<Type, Object, TypeInstantiator> typeInstantiatorsCache;

	private final List<PropertyInstantiatorProvider> propertyInstantiatorProviders;
	private final CopyOnWriteMap<Property, Object, PropertyInstantiator> propertyInstantiatorsCache;
	
	private final List<ConverterProvider> converterProviders;
	private final CopyOnWriteDualIdentityMap<Class<?>, Type, Converter<?>> convertersCache;
	
	private final List<CoderProvider> coderProviders;
	private final CopyOnWriteMap<Class<?>, Object, Coder> codersCache;
	
	private final List<UnfilterablePropertiesProvider> unfilterablePropertiesProviders;
	private final CopyOnWriteMap<Class<?>, Object, String[]> unfilterablePropertiesCache;
	
	private final List<PropertyFactory> propertyFactories;
	
	private final List<FilteredBeanDescriptorFactory> descriptorFactories;
	
	private static final String[] EMPTY_STRING_ARRAY = new String[] {};
	
	
	public SpearalContextImpl() {
		this.typeInstantiatorProviders = new ArrayList<TypeInstantiatorProvider>();
		this.typeInstantiatorsCache = new CopyOnWriteMap<Type, Object, TypeInstantiator>(true,
			new ValueProvider<Type, Object, TypeInstantiator>() {
				@Override
				public TypeInstantiator createValue(SpearalContext context, Type key, Object unused) {
					for (TypeInstantiatorProvider provider : typeInstantiatorProviders) {
						TypeInstantiator instantiator = provider.getInstantiator(key);
						if (instantiator != null)
							return instantiator;
					}
					throw new RuntimeException("Could not find any instantiator for: " + key);
				}
			}
		);

		this.propertyInstantiatorProviders = new ArrayList<PropertyInstantiatorProvider>();
		this.propertyInstantiatorsCache = new CopyOnWriteMap<Property, Object, PropertyInstantiator>(false,
			new ValueProvider<Property, Object, PropertyInstantiator>() {
				@Override
				public PropertyInstantiator createValue(SpearalContext context, Property key, Object unused) {
					for (PropertyInstantiatorProvider provider : propertyInstantiatorProviders) {
						PropertyInstantiator instantiator = provider.getInstantiator(key);
						if (instantiator != null)
							return instantiator;
					}
					throw new RuntimeException("Could not find any instantiator for: " + key);
				}
			}
		);
		
		this.converterProviders = new ArrayList<ConverterProvider>();
		this.convertersCache = new CopyOnWriteDualIdentityMap<Class<?>, Type, Converter<?>>(
			new DualIdentityMap.ValueProvider<Class<?>, Type, Converter<?>>() {
				@Override
				public Converter<?> createValue(SpearalContext context, Class<?> valueClass, Type targetType) {
					for (ConverterProvider provider : converterProviders) {
						Converter<?> converter = provider.getConverter(valueClass, targetType);
						if (converter != null)
							return converter;
					}
					throw new RuntimeException("No converter found from: " + valueClass + " to: " + targetType);
				}
			}
		);
		
		this.coderProviders = new ArrayList<CoderProvider>();
		this.codersCache = new CopyOnWriteMap<Class<?>, Object, Coder>(true,
			new ValueProvider<Class<?>, Object, Coder>() {
				@Override
				public Coder createValue(SpearalContext context, Class<?> key, Object param) {
					for (CoderProvider provider : coderProviders) {
						Coder coder = provider.getCoder(key);
						if (coder != null)
							return coder;
					}
					throw new RuntimeException("Not coder found for type: " + key);
				}
			}
		);
		
		this.unfilterablePropertiesProviders = new ArrayList<UnfilterablePropertiesProvider>();
		this.unfilterablePropertiesCache = new CopyOnWriteMap<Class<?>, Object, String[]>(true,
			new ValueProvider<Class<?>, Object, String[]>() {
				
				@Override
				public String[] createValue(SpearalContext context, Class<?> key, Object unused) {
					for (UnfilterablePropertiesProvider unfilterablePropertiesProvider : unfilterablePropertiesProviders) {
						String[] unfilterableProperties = unfilterablePropertiesProvider.getUnfilterableProperties(key);
						if (unfilterableProperties != null)
							return unfilterableProperties;
					}
					return EMPTY_STRING_ARRAY;
				}
			}
		);
		
		this.propertyFactories = new ArrayList<PropertyFactory>();
		
		this.descriptorFactories = new ArrayList<FilteredBeanDescriptorFactory>();
	}

	@Override
	public Securizer getSecurizer() {
		return securizer;
	}
	
	@Override
	public void configure(Configurable configurable) {
		configure(configurable, false);
	}
	
	@Override
	public void configure(Configurable configurable, boolean append) {
		boolean added = false;
		
		if (configurable instanceof Repeatable) {
			if (configurable instanceof TypeInstantiatorProvider) {
				typeInstantiatorProviders.add((append ? typeInstantiatorProviders.size() : 0), (TypeInstantiatorProvider)configurable);
				added = true;
			}
			
			if (configurable instanceof PropertyInstantiatorProvider) {
				propertyInstantiatorProviders.add((append ? propertyInstantiatorProviders.size() : 0), (PropertyInstantiatorProvider)configurable);
				added = true;
			}
			
			if (configurable instanceof ConverterProvider) {
				converterProviders.add((append ? converterProviders.size() : 0), (ConverterProvider)configurable);
				added = true;
			}
			
			if (configurable instanceof CoderProvider) {
				coderProviders.add((append ? coderProviders.size() : 0), (CoderProvider)configurable);
				added = true;
			}
			
			if (configurable instanceof UnfilterablePropertiesProvider) {
				unfilterablePropertiesProviders.add((append ? unfilterablePropertiesProviders.size() : 0), (UnfilterablePropertiesProvider)configurable);
				added = true;
			}
			
			if (configurable instanceof PropertyFactory) {
				propertyFactories.add((append ? propertyFactories.size() : 0), (PropertyFactory)configurable);
				added = true;
			}
			
			if (configurable instanceof FilteredBeanDescriptorFactory) {
				descriptorFactories.add((append ? descriptorFactories.size() : 0), (FilteredBeanDescriptorFactory)configurable);
				added = true;
			}
		}
		else {
			if (configurable instanceof AliasStrategy) {
				aliasStrategy = (AliasStrategy)configurable;
				added = true;
			}
			
			if (configurable instanceof Introspector) {
				introspector = (Introspector)configurable;
				added = true;
			}
			
			if (configurable instanceof TypeLoader) {
				loader = (TypeLoader)configurable;
				added = true;
			}
			
			if (configurable instanceof Securizer) {
				securizer = (Securizer)configurable;
				added = true;
			}
			
			if (configurable instanceof PartialObjectFactory) {
				partialObjectFactory = (PartialObjectFactory)configurable;
				added = true;
			}
		}
		
		if (!added)
			throw new RuntimeException("Unsupported configurable: " + configurable);
	}

	@Override
	public String alias(Class<?> cls) {
		return aliasStrategy.alias(cls);
	}

	@Override
	public String unalias(String aliasedClassName) {
		return aliasStrategy.unalias(aliasedClassName);
	}

	@Override
	public Class<?> loadClass(String classNames, Type target) throws SecurityException {
		return loader.loadClass(this, classNames, target);
	}

	@Override
	public Property[] getProperties(Class<?> cls) {
		return introspector.getProperties(this, cls);
	}
	
	@Override
	public Object newInstance(Class<?> cls) {
		if (cls.isInterface())
			cls = Proxy.getProxyClass(cls.getClassLoader(), cls, PartialObjectProxy.class);
        try {
			Property[] properties = getProperties(cls);
			return instantiatePartial(cls, properties);
        }
        catch (InstantiationException e) {
            throw new RuntimeException("Could not create instance of " + cls, e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Could not create instance of " + cls, e);
        }
	}

	@Override
	public Object instantiate(Type type, Object param) throws InstantiationException {
		return typeInstantiatorsCache.getOrPutIfAbsent(this, type).instantiate(this, type, param);
	}

	@Override
	public Object instantiate(Property property, Object param) throws InstantiationException {
		return propertyInstantiatorsCache.getOrPutIfAbsent(this, property).instantiate(this, property, param);
	}

	@Override
	public Object instantiatePartial(Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException {

		return partialObjectFactory.instantiatePartial(this, cls, partialProperties);
	}

	@Override
	public Object convert(Object value, Type targetType) {
		Class<?> valueClass = (value != null ? value.getClass() : null);
		if (valueClass == targetType)
			return value;
		return convertersCache.getOrPutIfAbsent(this, valueClass, targetType)
			.convert(this, value, targetType);
	}
	
	@Override
	public Coder getCoder(Class<?> valueClass) {
		return codersCache.getOrPutIfAbsent(this, valueClass);
	}
	
	@Override
	public String[] getUnfilterableProperties(Class<?> valueClass) {
		return unfilterablePropertiesCache.getOrPutIfAbsent(this, valueClass);
	}
	
	@Override
	public Property createProperty(String name, Field field, Method getter, Method setter) {
		for (PropertyFactory factory : propertyFactories) {
			Property property = factory.createProperty(name, field, getter, setter);
			if (property != null)
				return property;
		}
		throw new UnsupportedOperationException(
			"Could not create property for: " + name + " {" +
				"field=" + field + ", " +
				"getter=" + getter + ", " +
				"setter=" + setter +
			"}"
		);
	}
	
	@Override
	public FilteredBeanDescriptor createDescriptor(SpearalPropertyFilter filter, Object value) {
		for (FilteredBeanDescriptorFactory factory : descriptorFactories) {
			FilteredBeanDescriptor descriptor = factory.createDescription(this, filter, value);
			if (descriptor != null)
				return descriptor;
		}
		throw new UnsupportedOperationException("Could not create bean descriptor for: " + value);
	}
}
