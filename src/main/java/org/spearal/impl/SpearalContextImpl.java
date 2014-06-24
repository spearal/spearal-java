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
import org.spearal.configuration.ClassNameAlias;
import org.spearal.configuration.CoderProvider;
import org.spearal.configuration.CoderProvider.Coder;
import org.spearal.configuration.Configurable;
import org.spearal.configuration.ConverterProvider;
import org.spearal.configuration.ConverterProvider.Converter;
import org.spearal.configuration.Introspector;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PropertyFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.configuration.PropertyInstantiator;
import org.spearal.configuration.Repeatable;
import org.spearal.configuration.Securizer;
import org.spearal.configuration.TypeInstantiator;
import org.spearal.configuration.TypeLoader;
import org.spearal.impl.cache.CopyOnWriteKeyValueMap;
import org.spearal.impl.cache.KeyValueMap.ValueProvider;

/**
 * @author Franck WOLFF
 */
public class SpearalContextImpl implements SpearalContext {
	
	private Introspector introspector;
	private TypeLoader loader;
	private Securizer securizer;
	private PartialObjectFactory partialObjectFactory;
	
	private final Map<String, String> classAliases;
	
	private final List<TypeInstantiator> typeInstantiators;
	private final CopyOnWriteKeyValueMap<Type, TypeInstantiator> typeInstantiatorsCache;

	private final List<PropertyInstantiator> propertyInstantiators;
	private final CopyOnWriteKeyValueMap<Property, PropertyInstantiator> propertyInstantiatorsCache;
	
	private final List<ConverterProvider> converterProviders;
	private final CopyOnWriteKeyValueMap<ConverterKey, Converter<?>> convertersCache;
	
	private final List<CoderProvider> coderProviders;
	private final CopyOnWriteKeyValueMap<Class<?>, Coder> codersCache;
	
	private final List<PropertyFactory> propertyFactories;
	
	public SpearalContextImpl() {
		this.classAliases = new HashMap<String, String>();

		this.typeInstantiators = new ArrayList<TypeInstantiator>();
		this.typeInstantiatorsCache = new CopyOnWriteKeyValueMap<Type, TypeInstantiator>(true,
			new ValueProvider<Type, TypeInstantiator>() {
				@Override
				public TypeInstantiator createValue(SpearalContext context, Type key) {
					securizer.checkDecodable(key);
					for (TypeInstantiator instantiator : typeInstantiators) {
						if (instantiator.canInstantiate(key))
							return instantiator;
					}
					throw new RuntimeException("Could not find any instantiator for: " + key);
				}
			}
		);

		this.propertyInstantiators = new ArrayList<PropertyInstantiator>();
		this.propertyInstantiatorsCache = new CopyOnWriteKeyValueMap<Property, PropertyInstantiator>(false,
			new ValueProvider<Property, PropertyInstantiator>() {
				@Override
				public PropertyInstantiator createValue(SpearalContext context, Property key) {
					securizer.checkDecodable(key.getGenericType());
					for (PropertyInstantiator instantiator : propertyInstantiators) {
						if (instantiator.canInstantiate(key))
							return instantiator;
					}
					throw new RuntimeException("Could not find any instantiator for: " + key);
				}
			}
		);
		
		this.converterProviders = new ArrayList<ConverterProvider>();
		this.convertersCache = new CopyOnWriteKeyValueMap<ConverterKey, Converter<?>>(false,
			new ValueProvider<ConverterKey, Converter<?>>() {
				@Override
				public Converter<?> createValue(SpearalContext context, ConverterKey key) {
					for (ConverterProvider provider : converterProviders) {
						Converter<?> converter = provider.getConverter(key.valueClass, key.targetType);
						if (converter != null)
							return converter;
					}
					throw new RuntimeException("No converter found from: " + key.valueClass + " to: " + key.targetType);
				}
			}
		);
		
		this.coderProviders = new ArrayList<CoderProvider>();
		this.codersCache = new CopyOnWriteKeyValueMap<Class<?>, Coder>(true,
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
			
			if (configurable instanceof TypeInstantiator) {
				typeInstantiators.add((append ? typeInstantiators.size() : 0), (TypeInstantiator)configurable);
				added = true;
			}
			
			if (configurable instanceof PropertyInstantiator) {
				propertyInstantiators.add((append ? propertyInstantiators.size() : 0), (PropertyInstantiator)configurable);
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
	public Class<?> loadClass(String...classNames) throws SecurityException {
		return loader.loadClass(classNames);
	}

	@Override
	public Property[] getProperties(Class<?> cls) {
		return introspector.getProperties(this, cls);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Type type) throws InstantiationException {
		TypeInstantiator typeInstantiator = typeInstantiatorsCache.get(type);
		if (typeInstantiator == null)
			typeInstantiator = typeInstantiatorsCache.putIfAbsent(this, type);
		return typeInstantiator.instantiate((ExtendedSpearalDecoder)decoder, type);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Property property) throws InstantiationException {
		PropertyInstantiator propertyInstantiator = propertyInstantiatorsCache.get(property);
		if (propertyInstantiator == null)
			propertyInstantiator = propertyInstantiatorsCache.putIfAbsent(this, property);
		return propertyInstantiator.instantiate((ExtendedSpearalDecoder)decoder, property);
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
		
		ConverterKey converterKey = new ConverterKey(valueClass, targetType);
		Converter<?> converter = convertersCache.get(converterKey);
		if (converter == null)
			converter = convertersCache.putIfAbsent(this, converterKey);
		return converter.convert((ExtendedSpearalDecoder)decoder, value, targetType);
	}
	
	@Override
	public Coder getCoder(Class<?> valueClass) {
		Coder coder = codersCache.get(valueClass);
		if (coder == null)
			coder = codersCache.putIfAbsent(this, valueClass);
		return coder;
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

	private static final class ConverterKey {
		
		public final Class<?> valueClass;
		public final Type targetType;
		public final int hash;

		public ConverterKey(Class<?> valueClass, Type targetType) {
			this.valueClass = valueClass;
			this.targetType = targetType;
			this.hash = (valueClass == null ? 0 : valueClass.hashCode()) + targetType.hashCode();
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			ConverterKey key = (ConverterKey)obj;
			return key.valueClass == valueClass && key.targetType == targetType;
		}
	}
}
