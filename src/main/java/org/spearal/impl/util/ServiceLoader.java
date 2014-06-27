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
package org.spearal.impl.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Franck WOLFF
 */
public class ServiceLoader<S> implements Iterable<S> {
	
	private static final Logger log = Logger.getLogger(ServiceLoader.class.getName());
	
	private final Class<S> service;
	private final ClassLoader loader;
	
	private List<String> serviceClassNames;
	
	private Class<?>[] constructorParameterTypes = new Class<?>[0];
	private Object[] constructorParameters = new Object[0];
	
	private ServiceLoader(Class<S> service, ClassLoader loader, List<String> servicesNames) {
		this.service = service;
		this.loader = loader;
		this.serviceClassNames = servicesNames;
	}
	
	public void setConstructorParameters(Class<?>[] constructorParameterTypes, Object[] constructorParameters) {		
		
		if (constructorParameterTypes == null)
			constructorParameterTypes = new Class<?>[0];
		if (constructorParameters == null)
			constructorParameters = new Object[0];
		
		if (constructorParameterTypes.length != constructorParameters.length)
			throw new IllegalArgumentException("constructor types and argurments must have the same size");

		this.constructorParameterTypes = constructorParameterTypes;
		this.constructorParameters = constructorParameters;
	}

	public ServicesIterator<S> iterator() {
		return new ServicesIterator<S>(loader, serviceClassNames.iterator(), constructorParameterTypes, constructorParameters);
	}
	
	public void reload() {
		ServiceLoader<S> serviceLoaderTmp = load(service, loader);
		this.serviceClassNames = serviceLoaderTmp.serviceClassNames;
	}
	
	public static <S> ServiceLoader<S> load(Class<S> service) {
		return load(service, Thread.currentThread().getContextClassLoader());
	}

	public static <S> ServiceLoader<S> load(final Class<S> service, final ClassLoader loader) {
		List<String> serviceClassNames = new ArrayList<String>();
		
		try {
			// Standard Java platforms.
			Enumeration<URL> en = loader.getResources("META-INF/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			// Android support (META-INF/** files are not included in APK files).
			en = loader.getResources("meta_inf/services/" + service.getName());
			while (en.hasMoreElements())
				parse(en.nextElement(), serviceClassNames);
			
			return new ServiceLoader<S>(service, loader, serviceClassNames);
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Could not load services of type " + service, e);
			throw new RuntimeException(e);
		}
	}
	
	private static void parse(URL serviceFile, List<String> serviceClassNames) {
		try {
		    InputStream is = serviceFile.openStream();
		    try {
		    	BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
	
		    	String serviceClassName = null;
		    	while ((serviceClassName = reader.readLine()) != null) {
		    		int comment = serviceClassName.indexOf('#');
		    		if (comment >= 0)
		    			serviceClassName = serviceClassName.substring(0, comment);
			        serviceClassName = serviceClassName.trim();
			        if (serviceClassName.length() > 0) {
			        	log.log(Level.FINE, "Adding service " + serviceClassName + " from " + serviceFile);
			        	serviceClassNames.add(serviceClassName);
			        }
		    	}
		    }
		    finally {
		    	is.close();
		    }
		}
		catch (Exception e) {
			log.log(Level.SEVERE, "Could not parse service file " + serviceFile, e);
		}
	}
	
	public static class ServicesIterator<S> implements Iterator<S> {
		
		private final ClassLoader loader;
		private final Iterator<String> serviceClassNames;
		private final Class<?>[] constructorParameterTypes;
		private final Object[] constructorParameters;

		private ServicesIterator(ClassLoader loader, Iterator<String> servicesNames, Class<?>[] constructorParameterTypes, Object[] constructorParameters) {
			this.loader = loader;
			this.serviceClassNames = servicesNames;
			this.constructorParameterTypes = constructorParameterTypes;
			this.constructorParameters = constructorParameters;
		}

		public boolean hasNext() {
			return serviceClassNames.hasNext();
		}

		public S next() {
			String serviceClassName = serviceClassNames.next();
			log.log(Level.FINE, "Loading service " + serviceClassName);
			try {
				@SuppressWarnings("unchecked")
				Class<? extends S> serviceClass = (Class<? extends S>)loader.loadClass(serviceClassName);
				return serviceClass.getConstructor(constructorParameterTypes).newInstance(constructorParameters);
			}
			catch (Throwable t) {
				log.log(Level.SEVERE, "Could not load service " + serviceClassName, t);
				throw new RuntimeException(t);
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}	
}
