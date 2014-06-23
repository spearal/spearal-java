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
package org.spearal.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Type;

import org.spearal.SpearalFactory;
import org.spearal.SpearalDecoder;
import org.spearal.SpearalEncoder;
import org.spearal.SpearalPropertyFilter;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractSpearalTestUnit {

	protected static final PrintStream NULL_PRINT_STREAM = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
		}
	});
	
	protected PrintStream printStream;
	
	public AbstractSpearalTestUnit() {
		printStream = NULL_PRINT_STREAM;
	}

	protected byte[] encode(Object o) throws IOException {
		return encode(new SpearalFactory(), null, o);
	}

	protected byte[] encode(SpearalPropertyFilter request, Object o) throws IOException {
		return encode(new SpearalFactory(), request, o);
	}

	protected byte[] encode(SpearalFactory factory, Object o) throws IOException {
		return encode(factory, null, o);
	}

	protected byte[] encode(SpearalFactory factory, SpearalPropertyFilter request, Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalEncoder out = factory.newEncoder(baos, request);
		out.writeAny(o);
		return baos.toByteArray();
	}
	
	protected String toString(byte[] bytes) {
		final String hex = "0123456789abcdef";
		StringBuilder sb = new StringBuilder(bytes.length * 6);
		sb.append('[');
		boolean first = true;
		for (byte b : bytes) {
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append("0x").append(hex.charAt((b >>> 4) & 0x0f)).append(hex.charAt(b & 0x0f));
		}
		sb.append(']');
		return sb.toString();
	}
	
	protected Object decode(byte[] bytes) throws IOException {
		return decode(new SpearalFactory(), bytes, null);
	}
	
	@SuppressWarnings("unchecked")
	protected <T> T decode(byte[] bytes, Type targetType) throws IOException {
		return (T)decode(new SpearalFactory(), bytes, targetType);
	}
	
	protected Object decode(SpearalFactory factory, byte[] bytes, Type targetType) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalDecoder in = factory.newDecoder(bais);
		in.skipAny();
		
		bais.reset();
		in = factory.newDecoder(bais);
		in.printAny(factory.newPrinter(printStream));
		
		bais.reset();
		in = factory.newDecoder(bais);
		return in.readAny(targetType);
	}
}
