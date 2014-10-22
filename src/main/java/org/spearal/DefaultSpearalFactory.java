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
package org.spearal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.spearal.configuration.Configurable;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.impl.SpearalContextImpl;
import org.spearal.impl.SpearalDecoderImpl;
import org.spearal.impl.SpearalEncoderImpl;
import org.spearal.impl.SpearalPrinterImpl;
import org.spearal.impl.alias.AliasStrategyImpl;
import org.spearal.impl.coder.ArrayCoder;
import org.spearal.impl.coder.BeanCoder;
import org.spearal.impl.coder.CollectionCoder;
import org.spearal.impl.coder.EnumCoder;
import org.spearal.impl.coder.MapCoder;
import org.spearal.impl.coder.SimpleCodersProvider;
import org.spearal.impl.converter.EnumConverter;
import org.spearal.impl.converter.SimpleConvertersProvider;
import org.spearal.impl.descriptor.FilteredBeanDescriptorFactoryImpl;
import org.spearal.impl.instantiator.ArrayInstantiator;
import org.spearal.impl.instantiator.ClassInstantiator;
import org.spearal.impl.instantiator.CollectionInstantiator;
import org.spearal.impl.instantiator.MapInstantiator;
import org.spearal.impl.instantiator.ProxyInstantiator;
import org.spearal.impl.introspector.IntrospectorImpl;
import org.spearal.impl.loader.TypeLoaderImpl;
import org.spearal.impl.partial.NoProxyPartialObjectFactory;
import org.spearal.impl.property.SimplePropertiesFactory;
import org.spearal.impl.security.SecurizerImpl;
import org.spearal.impl.util.ServiceLoader;

/**
 * @author Franck WOLFF
 * @author William DRAI
 */
public class DefaultSpearalFactory implements SpearalFactory {
	
	private final SpearalContextImpl context;
	
	public DefaultSpearalFactory() {
		this(true);
	}
	
	public DefaultSpearalFactory(boolean loadServices) {
		
		context = new SpearalContextImpl();
		
		// Reflection / Security.
		
		context.configure(new IntrospectorImpl());
		context.configure(new TypeLoaderImpl());
		context.configure(new SecurizerImpl());
		context.configure(newDefaultPartialObjectFactory());
		
		// Converters.
		
		context.configure(new SimpleConvertersProvider(), true);
		context.configure(new EnumConverter(), true);

		// Instantiators.
		
		context.configure(new CollectionInstantiator(), true);
		context.configure(new ArrayInstantiator(), true);
		context.configure(new MapInstantiator(), true);
		context.configure(new ProxyInstantiator(), true);
		context.configure(new ClassInstantiator(), true);
		
		// StaticObjectWriterProviders & PropertyFactories.

		context.configure(new SimplePropertiesFactory(), true);
		
		// CoderProviders.
		
		context.configure(new SimpleCodersProvider(), true);
		context.configure(new CollectionCoder(), true);
		context.configure(new MapCoder(), true);
		context.configure(new ArrayCoder(), true);
		context.configure(new EnumCoder(), true);
		context.configure(new BeanCoder(), true);
		
		// Bean descriptors.
		
		context.configure(new FilteredBeanDescriptorFactoryImpl(), true);
		
		// Alias strategy.
		
		context.configure(new AliasStrategyImpl(), true);
		
		// Load plugins.
		
		if (loadServices) {
			for (Configurable configurable : ServiceLoader.load(Configurable.class))
				context.configure(configurable);
		}
	}
	
	protected static final PartialObjectFactory newDefaultPartialObjectFactory() {
		try {
			// Check if javassist is available
			Class.forName("javassist.util.proxy.ProxyObject");
			// Instantiate factory
			Class<?> cls = Class.forName("org.spearal.impl.partial.JavassistPartialObjectFactory");
			return (PartialObjectFactory)cls.newInstance();
		}
		catch (Throwable e) {
			// javassist not present, fallback to incomplete classes
			return new NoProxyPartialObjectFactory();
		}
	}
	
	@Override
	public SpearalContext getContext() {
		return context;
	}
	
	@Override
	public SpearalEncoder newEncoder(OutputStream out) {
		return new SpearalEncoderImpl(context, out);
	}
	
	@Override
	public SpearalEncoder newEncoder(OutputStream out, SpearalPropertyFilter request) {
		return new SpearalEncoderImpl(context, request, out);
	}
	
	@Override
	public SpearalDecoder newDecoder(InputStream in) {
		return new SpearalDecoderImpl(context, in);
	}
	
	@Override
	public SpearalPrinter newPrinter(PrintStream out) {
		return new SpearalPrinterImpl(out);
	}
}
