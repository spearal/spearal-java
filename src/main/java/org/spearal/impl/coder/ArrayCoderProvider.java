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
package org.spearal.impl.coder;

import java.io.IOException;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

/**
 * @author Franck WOLFF
 */
public class ArrayCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public ArrayCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeArray(value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return (valueClass.isArray() ? coder : null);
	}
}
