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

import org.spearal.SpearalFactory;
import org.spearal.SpearalDecoder;
import org.spearal.SpearalEncoder;
import org.spearal.SpearalRequest;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractSpearalTestUnit {

	public AbstractSpearalTestUnit() {
	}

	protected byte[] encode(Object o) throws IOException {
		return encode(new SpearalFactory(), null, o);
	}

	protected byte[] encode(SpearalRequest request, Object o) throws IOException {
		return encode(new SpearalFactory(), request, o);
	}

	protected byte[] encode(SpearalFactory factory, Object o) throws IOException {
		return encode(factory, null, o);
	}

	protected byte[] encode(SpearalFactory factory, SpearalRequest request, Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalEncoder out = factory.newEncoder(baos, request);
		out.writeAny(o);
		return baos.toByteArray();
	}
	
	protected Object decode(byte[] bytes) throws IOException {
		return decode(new SpearalFactory(), bytes);
	}
	
	protected Object decode(SpearalFactory factory, byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalDecoder in = factory.newDecoder(bais);
		return in.readAny();
	}
}
