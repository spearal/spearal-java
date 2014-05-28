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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.spearal.SpearalContext;
import org.spearal.configurable.ClassNameAlias;
import org.spearal.configurable.ConfigurableItem;
import org.spearal.configurable.Converter;
import org.spearal.configurable.ObjectWriterProvider;
import org.spearal.configurable.PropertyFactory;
import org.spearal.configurable.PropertyInstantiator;
import org.spearal.configurable.TypeInstantiator;
import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.converter.BooleanConverter;
import org.spearal.impl.converter.ByteConverter;
import org.spearal.impl.converter.CharConverter;
import org.spearal.impl.converter.IntConverter;
import org.spearal.impl.converter.LongConverter;
import org.spearal.impl.converter.ShortConverter;
import org.spearal.impl.instantiator.CollectionInstantiator;
import org.spearal.impl.instantiator.MapInstantiator;
import org.spearal.impl.properties.BeanPropertyFactory;
import org.spearal.impl.properties.BooleanPropertyFactory;
import org.spearal.impl.properties.CollectionPropertyFactory;
import org.spearal.impl.properties.DatePropertyFactory;
import org.spearal.impl.properties.FloatingPropertyFactory;
import org.spearal.impl.properties.IntegralPropertyFactory;
import org.spearal.impl.properties.MapPropertyFactory;
import org.spearal.impl.properties.StringPropertyFactory;
import org.spearal.introspect.Introspector;
import org.spearal.partial.PartialObjectFactory;

/**
 * @author Franck WOLFF
 */
public class SpearalContextImpl implements SpearalContext {
	
	private final Introspector introspector;
	
	private final PartialObjectFactory partialObjectFactory;
	
	private final Map<String, String> classAliases;
	
	private final List<TypeInstantiator> typeInstantiators;
	private final ConcurrentMap<Type, TypeInstantiator> typeInstantiatorsCache;

	private final List<PropertyInstantiator> propertyInstantiators;
	private final ConcurrentMap<Property, PropertyInstantiator> propertyInstantiatorsCache;
	
	private final List<Converter> converters;
	private final ConcurrentMap<InputConverterKey, Converter> convertersCache;
	
	private final List<ObjectWriterProvider> staticObjectWriterProviders;
	private final ConcurrentMap<Class<?>, ObjectWriter> staticObjectWriterCache;
	
	private final List<PropertyFactory> propertyFactories;
	private final ConcurrentMap<Class<?>, PropertyFactory> propertyFactoriesCache;
	
	public SpearalContextImpl(Introspector introspector, PartialObjectFactory partialObjectFactory) {
		this.introspector = introspector;
		
		this.partialObjectFactory = partialObjectFactory;
		
		this.classAliases = new HashMap<String, String>();

		this.typeInstantiators = new ArrayList<TypeInstantiator>();
		this.typeInstantiatorsCache = new ConcurrentHashMap<Type, TypeInstantiator>();

		this.propertyInstantiators = new ArrayList<PropertyInstantiator>();
		this.propertyInstantiatorsCache = new ConcurrentHashMap<Property, PropertyInstantiator>();
		
		this.converters = new ArrayList<Converter>();
		this.convertersCache = new ConcurrentHashMap<InputConverterKey, Converter>();
		
		this.staticObjectWriterProviders = new ArrayList<ObjectWriterProvider>();
		this.staticObjectWriterCache = new ConcurrentHashMap<Class<?>, ObjectWriter>();
		
		this.propertyFactories = new ArrayList<PropertyFactory>();
		this.propertyFactoriesCache = new ConcurrentHashMap<Class<?>, PropertyFactory>();
	}
	
	@Override
	public void initStandardConfigurables() {
		
		// Converters.
		
		prependConfiguraableItem(new ShortConverter());
		prependConfiguraableItem(new ByteConverter());
		prependConfiguraableItem(new CharConverter());
		prependConfiguraableItem(new BooleanConverter());
		prependConfiguraableItem(new LongConverter());
		prependConfiguraableItem(new IntConverter());

		// Instantiators.
		
		prependConfiguraableItem(new CollectionInstantiator());
		prependConfiguraableItem(new MapInstantiator());
		
		// StaticObjectWriterProviders & PropertyFactories

		prependConfiguraableItem(new BeanPropertyFactory());
		
		prependConfiguraableItem(new DatePropertyFactory());
		prependConfiguraableItem(new MapPropertyFactory());
		prependConfiguraableItem(new CollectionPropertyFactory());
		
		prependConfiguraableItem(new FloatingPropertyFactory());
		prependConfiguraableItem(new BooleanPropertyFactory());
		prependConfiguraableItem(new IntegralPropertyFactory());
		prependConfiguraableItem(new StringPropertyFactory());
	}
	
	@Override
	public void prependConfiguraableItem(ConfigurableItem item) {
		boolean added = false;
		
		if (item instanceof ClassNameAlias) {
			ClassNameAlias classAlias = (ClassNameAlias)item;
			classAliases.put(classAlias.getClassName(), classAlias.getAlias());
			classAliases.put(classAlias.getAlias(), classAlias.getClassName());
			added = true;
		}
		
		if (item instanceof TypeInstantiator) {
			typeInstantiators.add(0, (TypeInstantiator)item);
			added = true;
		}
		
		if (item instanceof PropertyInstantiator) {
			propertyInstantiators.add(0, (PropertyInstantiator)item);
			added = true;
		}
		
		if (item instanceof Converter) {
			converters.add(0, (Converter)item);
			added = true;
		}
		
		if (item instanceof ObjectWriterProvider) {
			staticObjectWriterProviders.add(0, (ObjectWriterProvider)item);
			added = true;
		}
		
		if (item instanceof PropertyFactory) {
			propertyFactories.add(0, (PropertyFactory)item);
			added = true;
		}
		
		if (!added)
			throw new RuntimeException("Unsupported configuration item: " + item);
	}

	@Override
	public String getClassNameAlias(String className) {
		String alias = classAliases.get(className);
		return (alias != null ? alias : className);
	}

	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return Class.forName(className);
	}

	@Override
	public Collection<Property> getProperties(Class<?> cls) {
		return introspector.getProperties(this, cls);
	}

	@Override
	public Object instantiate(Type type) throws InstantiationException {
		TypeInstantiator typeInstantiator = typeInstantiatorsCache.get(type);
		
		if (typeInstantiator == null) {
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

		return typeInstantiator.instantiate(type);
	}

	@Override
	public Object instantiate(Property property) throws InstantiationException {
		PropertyInstantiator propertyInstantiator = propertyInstantiatorsCache.get(property);
		
		if (propertyInstantiator == null) {
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

		return propertyInstantiator.instantiate(property);
	}

	@Override
	public Object instantiatePartial(Class<?> cls, Collection<Property> partialProperties)
		throws InstantiationException, IllegalAccessException {

		return partialObjectFactory.instantiatePartial(this, cls, partialProperties);
	}

	@Override
	public Object convert(Object o, Type target) {
		Class<?> cls = (o != null ? o.getClass() : null);
		InputConverterKey key = new InputConverterKey(cls, target);
		
		Converter inputConverter = convertersCache.get(key);
		if (inputConverter == null) {
			for (Converter ic : converters) {
				if (ic.canConvert(cls, target)) {
					convertersCache.put(key, ic);
					inputConverter = ic;
					break;
				}
			}
			if (inputConverter == null)
				throw new UnsupportedOperationException("No converter from: " + cls + " to: " + target);
		}
		return inputConverter.convert(o, target);
	}
	
	@Override
	public ObjectWriter getWriter(Class<?> type) {
		ObjectWriter writer = staticObjectWriterCache.get(type);
		if (writer == null) {
			for (ObjectWriterProvider provider : staticObjectWriterProviders) {
				writer = provider.getWriter(type);
				if (writer != null) {
					staticObjectWriterCache.putIfAbsent(type, writer);
					break;
				}
			}
			if (writer == null)
				throw new UnsupportedOperationException("Not static writer for type: " + type);
		}
		return writer;
	}

	@Override
	public PropertyFactory getPropertyFactory(Class<?> type) {
		PropertyFactory factory = propertyFactoriesCache.get(type);
		if (factory == null) {
			for (PropertyFactory f : propertyFactories) {
				if (f.canCreateProperty(type)) {
					factory = f;
					propertyFactoriesCache.putIfAbsent(type, factory);
					break;
				}
			}
			if (factory == null)
				throw new UnsupportedOperationException("Not property factory for type: " + type);
		}
		return factory;
	}

	private static class InputConverterKey {
		
		public final Class<?> cls;
		public final Type target;
		public final int hash;

		public InputConverterKey(Class<?> cls, Type target) {
			this.cls = cls;
			this.target = target;
			this.hash = (31 * target.hashCode()) + (cls == null ? 0 : cls.hashCode());
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			InputConverterKey key = (InputConverterKey)obj;
			return key.cls == cls && key.target == target;
		}
	}
}
