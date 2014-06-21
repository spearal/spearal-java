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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
	private final ConcurrentMap<Type, TypeInstantiator> typeInstantiatorsCache;

	private final List<PropertyInstantiator> propertyInstantiators;
	private final ConcurrentMap<Property, PropertyInstantiator> propertyInstantiatorsCache;
	
	private final List<ConverterProvider> converterProviders;
	private final ConcurrentMap<ConverterKey, Converter<?>> convertersCache;
	
	private final List<CoderProvider> coderProviders;
	private final ConcurrentMap<Class<?>, Coder> codersCache;
	
	private final List<PropertyFactory> propertyFactories;
	
	public SpearalContextImpl() {
		this.classAliases = new HashMap<String, String>();

		this.typeInstantiators = new ArrayList<TypeInstantiator>();
		this.typeInstantiatorsCache = new ConcurrentHashMap<Type, TypeInstantiator>();

		this.propertyInstantiators = new ArrayList<PropertyInstantiator>();
		this.propertyInstantiatorsCache = new ConcurrentHashMap<Property, PropertyInstantiator>();
		
		this.converterProviders = new ArrayList<ConverterProvider>();
		this.convertersCache = new ConcurrentHashMap<ConverterKey, Converter<?>>();
		
		this.coderProviders = new ArrayList<CoderProvider>();
		this.codersCache = new ConcurrentHashMap<Class<?>, Coder>();
		
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
	public Collection<Property> getProperties(Class<?> cls) {
		return introspector.getProperties(this, cls);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Type type) throws InstantiationException {
		TypeInstantiator typeInstantiator = typeInstantiatorsCache.get(type);
		
		if (typeInstantiator == null) {
			securizer.checkDecodable(type);
			for (TypeInstantiator ti : typeInstantiators) {
				if (ti.canInstantiate(type)) {
					typeInstantiatorsCache.putIfAbsent(type, ti);
					typeInstantiator = ti;
					break;
				}
			}
		}
		
		if (typeInstantiator == null)
			throw new InstantiationException("Could not find any instantiator for: " + type);

		return typeInstantiator.instantiate((ExtendedSpearalDecoder)decoder, type);
	}

	@Override
	public Object instantiate(SpearalDecoder decoder, Property property) throws InstantiationException {
		PropertyInstantiator propertyInstantiator = propertyInstantiatorsCache.get(property);
		
		if (propertyInstantiator == null) {
			securizer.checkDecodable(property.getGenericType());
			for (PropertyInstantiator ti : propertyInstantiators) {
				if (ti.canInstantiate(property)) {
					propertyInstantiatorsCache.putIfAbsent(property, ti);
					propertyInstantiator = ti;
					break;
				}
			}
		}
		
		if (propertyInstantiator == null)
			throw new InstantiationException("Could not find any instantiator for: " + property);

		return propertyInstantiator.instantiate((ExtendedSpearalDecoder)decoder, property);
	}

	@Override
	public Object instantiatePartial(SpearalDecoder decoder, Class<?> cls, Collection<Property> partialProperties)
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
		if (converter == null) {
			for (ConverterProvider provider : converterProviders) {
				converter = provider.getConverter(valueClass, targetType);
				if (converter != null) {
					convertersCache.put(converterKey, converter);
					break;
				}
			}
			if (converter == null)
				throw new UnsupportedOperationException("No converter found from: " + valueClass + " to: " + targetType);
		}
		
		return converter.convert((ExtendedSpearalDecoder)decoder, value, targetType);
	}
	
	@Override
	public Coder getCoder(Class<?> valueClass) {
		Coder coder = codersCache.get(valueClass);
		if (coder == null) {
			for (CoderProvider provider : coderProviders) {
				coder = provider.getCoder(valueClass);
				if (coder != null) {
					codersCache.put(valueClass, coder);
					break;
				}
			}
			if (coder == null)
				throw new UnsupportedOperationException("Not coder found for type: " + valueClass);
		}
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
