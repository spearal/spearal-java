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

import org.spearal.impl.SpearalContextImpl;
import org.spearal.impl.SpearalInputImpl;
import org.spearal.impl.SpearalOutputImpl;
import org.spearal.impl.introspector.IntrospectorImpl;
import org.spearal.introspect.Introspector;
import org.spearal.partial.PartialObjectFactory;

/**
 * @author Franck WOLFF
 */
public class SpearalFactory {
	
	private final SpearalContextImpl context;
	
	public SpearalFactory() {
		this(null, null);
	}
	
	public SpearalFactory(Introspector introspector, PartialObjectFactory partialObjectFactory) {

		if (introspector == null)
			introspector = new IntrospectorImpl();
		
		if (partialObjectFactory == null)
			partialObjectFactory = newDefaultPartialObjectFactory();
		
		this.context = new SpearalContextImpl(introspector, partialObjectFactory);
		this.context.initStandardConfigurables();
	}
	
	protected static final PartialObjectFactory newDefaultPartialObjectFactory() {
		try {
			Class<?> cls = Class.forName("org.spearal.impl.partial.JavassistPartialObjectFactory");
			return (PartialObjectFactory)cls.newInstance();
		}
		catch (Exception e) {
			throw new RuntimeException("Could not create default PartialObjectFactory", e);
		}
	}
	
	public SpearalContext getContext() {
		return context;
	}
	
	public SpearalOutput newOutput(OutputStream out) {
		return new SpearalOutputImpl(context, out);
	}
	
	public SpearalOutput newOutput(OutputStream out, SpearalRequest request) {
		return new SpearalOutputImpl(context, request, out);
	}
	
	public SpearalInput newInput(InputStream in) {
		return new SpearalInputImpl(context, in);
	}
}
