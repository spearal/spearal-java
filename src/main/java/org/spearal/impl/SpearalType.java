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

/**
 * @author Franck WOLFF
 */
public enum SpearalType {

	// No parameters (0x00...0x0f).
	
	NULL(0x00),

	TRUE(0x01),
	FALSE(0x02),
	
	FLOATING(0x03),
	DATE(0x04),
	
	// 4 bits of parameters (0x10...0xf0).

	INTEGRAL(0x10),
	BIG_INTEGRAL(0x20),
	
	STRING(0x30),
	
	BYTES(0x40),
	
	COLLECTION(0x50),
	MAP(0x60),
	
	BEAN(0xf0);
	
	private static final SpearalType[] SIO_TYPES;
	static {
		SpearalType[] types = SpearalType.values();
		SIO_TYPES = new SpearalType[0xf0 + 1];
		
		for (SpearalType type : types) {
			int id = type.id;
			if (id < 0 || id > 0xf0 || (id != 0 && (id & 0xf0) != 0 && (id & 0x0f) != 0))
				throw new ExceptionInInitializerError("Illegal type id: " + id);
			if (SIO_TYPES[id] != null)
				throw new ExceptionInInitializerError("Duplicated ids: " + SIO_TYPES[id] + " / " + type);
			SIO_TYPES[id] = type;
		}
	}
	
	private final int id;
	
	SpearalType(int id) {
		this.id = id;
	}
	
	public int id() {
		return id;
	}
	
	public static int idOf(int parameterizedType) {
		if (parameterizedType < 0 || parameterizedType > 0xff)
			throw new IndexOutOfBoundsException("Illegal type: " + parameterizedType);
		return (parameterizedType > 0x0f ? (parameterizedType & 0xf0) : parameterizedType);
	}
	
	public static SpearalType valueOf(int parameterizedType) {
		return SIO_TYPES[idOf(parameterizedType)];
	}
}
