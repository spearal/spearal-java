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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalDecoder;
import org.spearal.SpearalEncoder;
import org.spearal.configuration.ClassNameAlias;
import org.spearal.configuration.CoderProvider;
import org.spearal.configuration.CoderProvider.Coder;
import org.spearal.configuration.Configurable;
import org.spearal.configuration.ConverterProvider;
import org.spearal.configuration.ConverterProvider.Converter;
import org.spearal.configuration.EncoderBeanDescriptorFactory;
import org.spearal.configuration.EncoderBeanDescriptorFactory.EncoderBeanDescriptor;
import org.spearal.configuration.Introspector;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PropertyFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiatorProvider;
import org.spearal.configuration.PropertyInstantiatorProvider.PropertyInstantiator;
import org.spearal.configuration.Repeatable;
import org.spearal.configuration.Securizer;
import org.spearal.configuration.TypeInstantiatorProvider;
import org.spearal.configuration.TypeInstantiatorProvider.TypeInstantiator;
import org.spearal.configuration.TypeLoader;
import org.spearal.impl.cache.CopyOnWriteDualIdentityMap;
import org.spearal.impl.cache.CopyOnWriteValueMap;
import org.spearal.impl.cache.DualIdentityMap;
import org.spearal.impl.cache.ValueMap.ValueProvider;

/**
 * @author Franck WOLFF
 */
public class SpearalContextImpl implements SpearalContext {
	
	private Introspector introspector;
	private TypeLoader loader;
	private Securizer securizer;
	private PartialObjectFactory partialObjectFactory;
	
	private final Map<String, String> classAliases;
	
	private final List<TypeInstantiatorProvider> typeInstantiatorProviders;
	private final CopyOnWriteValueMap<Type, TypeInstantiator> typeInstantiatorsCache;

	private final List<PropertyInstantiatorProvider> propertyInstantiatorProviders;
	private final CopyOnWriteValueMap<Property, PropertyInstantiator> propertyInstantiatorsCache;
	
	private final List<ConverterProvider> converterProviders;
	private final CopyOnWriteDualIdentityMap<Class<?>, Type, Converter<?>> convertersCache;
	
	private final List<CoderProvider> coderProviders;
	private final CopyOnWriteValueMap<Class<?>, Coder> codersCache;
	
	private final List<PropertyFactory> propertyFactories;
	
	private final List<EncoderBeanDescriptorFactory> descriptorFactories;
	
	public SpearalContextImpl() {
		this.classAliases = new HashMap<String, String>();

		this.typeInstantiatorProviders = new ArrayList<TypeInstantiatorProvider>();
		this.typeInstantiatorsCache = new CopyOnWriteValueMap<Type, TypeInstantiator>(true,
			new ValueProvider<Type, TypeInstantiator>() {
				@Override
				public TypeInstantiator createValue(SpearalContext context, Type key) {
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
		this.propertyInstantiatorsCache = new CopyOnWriteValueMap<Property, PropertyInstantiator>(false,
			new ValueProvider<Property, PropertyInstantiator>() {
				@Override
				public PropertyInstantiator createValue(SpearalContext context, Property key) {
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
		this.codersCache = new CopyOnWriteValueMap<Class<?>, Coder>(true,
			new ValueProvider<Class<?>, Coder>() {
				@Override
				public Coder createValue(SpearalContext context, Class<?> key) {
					for (CoderProvider provider : coderProviders) {
						Coder coder = provider.getCoder(key);
						if (coder != null)
							return coder;
					}
					throw new RuntimeException("Not coder found for type: " + key);
				}
			}
		);
		
		this.propertyFactories = new ArrayList<PropertyFactory>();
		
		this.descriptorFactories = new ArrayList<EncoderBeanDescriptorFactory>();
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
			if (configurable instanceof ClassNameAlias) {
				ClassNameAlias classAlias = (ClassNameAlias)configurable;
				classAliases.put(classAlias.getClassName(), classAlias.getAlias());
				classAliases.put(classAlias.getAlias(), classAlias.getClassName());
				added = true;
			}
			
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
			
			if (configurable instanceof PropertyFactory) {
				propertyFactories.add((append ? propertyFactories.size() : 0), (PropertyFactory)configurable);
				added = true;
			}
			
			if (configurable instanceof EncoderBeanDescriptorFactory) {
				descriptorFactories.add((append ? descriptorFactories.size() : 0), (EncoderBeanDescriptorFactory)configurable);
				added = true;
			}
		}
		else {
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
	public String getClassNameAlias(String className) {
		String alias = classAliases.get(className);
		return (alias != null ? alias : className);
	}

	@Override
	public Class<?> loadClass(String classNames) throws SecurityException {
		return loader.loadClass(this, classNames);
	}

	@Override
	public Property[] getProperties(Class<?> cls) {
		return introspector.getProperties(this, cls);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Type type) throws InstantiationException {
		return typeInstantiatorsCache.getOrPutIfAbsent(this, type)
			.instantiate((ExtendedSpearalDecoder)decoder, type);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Property property) throws InstantiationException {
		return propertyInstantiatorsCache.getOrPutIfAbsent(this, property)
			.instantiate((ExtendedSpearalDecoder)decoder, property);
	}

	@Override
	public Object instantiatePartial(SpearalDecoder decoder, Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException {

		return partialObjectFactory.instantiatePartial((ExtendedSpearalDecoder)decoder, cls, partialProperties);
	}

	@Override
	public Object convert(SpearalDecoder decoder, Object value, Type targetType) {
		Class<?> valueClass = (value != null ? value.getClass() : null);
		if (valueClass == targetType)
			return value;
		return convertersCache.getOrPutIfAbsent(this, valueClass, targetType)
			.convert((ExtendedSpearalDecoder)decoder, value, targetType);
	}
	
	@Override
	public Coder getCoder(Class<?> valueClass) {
		return codersCache.getOrPutIfAbsent(this, valueClass);
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
	public EncoderBeanDescriptor createDescriptor(SpearalEncoder encoder, Object value) {
		for (EncoderBeanDescriptorFactory factory : descriptorFactories) {
			EncoderBeanDescriptor descriptor = factory.createDescription(encoder, value);
			if (descriptor != null)
				return descriptor;
		}
		throw new UnsupportedOperationException("Could not create bean descriptor for: " + value);
	}
}
