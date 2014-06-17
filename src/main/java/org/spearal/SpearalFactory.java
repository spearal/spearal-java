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

import org.spearal.impl.SpearalContextImpl;
import org.spearal.impl.SpearalDecoderImpl;
import org.spearal.impl.SpearalEncoderImpl;
import org.spearal.impl.SpearalPrinterImpl;
import org.spearal.impl.introspector.IntrospectorImpl;
import org.spearal.impl.loader.TypeLoaderImpl;
import org.spearal.impl.security.SecurizerImpl;
import org.spearal.introspect.Introspector;
import org.spearal.loader.TypeLoader;
import org.spearal.partial.PartialObjectFactory;
import org.spearal.security.Securizer;

/**
 * @author Franck WOLFF
 */
public class SpearalFactory {
	
	private final SpearalContextImpl context;
	
	public SpearalFactory() {
		this(null, null, null, null);
	}
	
	public SpearalFactory(
		Introspector introspector,
		TypeLoader loader,
		Securizer securizer,
		PartialObjectFactory partialObjectFactory) {

		if (introspector == null)
			introspector = new IntrospectorImpl();

		if (loader == null)
			loader = new TypeLoaderImpl();
		
		if (securizer == null)
			securizer = new SecurizerImpl();
		
		if (partialObjectFactory == null)
			partialObjectFactory = newDefaultPartialObjectFactory();
		
		this.context = new SpearalContextImpl(introspector, loader, securizer, partialObjectFactory);
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
	
	public SpearalEncoder newEncoder(OutputStream out) {
		return new SpearalEncoderImpl(context, out);
	}
	
	public SpearalEncoder newEncoder(OutputStream out, SpearalRequest request) {
		return new SpearalEncoderImpl(context, request, out);
	}
	
	public SpearalDecoder newDecoder(InputStream in) {
		return new SpearalDecoderImpl(context, in);
	}
	
	public SpearalPrinter newPrinter(PrintStream out) {
		return new SpearalPrinterImpl(out);
	}
}
