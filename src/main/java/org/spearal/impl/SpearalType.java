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
public enum SpearalType implements SpearalIType {

	// No parameters (0x00...0x0f).
	
	NULL(ITYPE_NULL),

	TRUE(ITYPE_TRUE),
	FALSE(ITYPE_FALSE),
	
	// 4 bits of parameters (0x10...0xf0).

	INTEGRAL(ITYPE_INTEGRAL),
	BIG_INTEGRAL(ITYPE_BIG_INTEGRAL),
	
	FLOATING(ITYPE_FLOATING),
	BIG_FLOATING(ITYPE_BIG_FLOATING),

	STRING(ITYPE_STRING),
	
	BYTE_ARRAY(ITYPE_BYTE_ARRAY),
	
	DATE_TIME(ITYPE_DATE_TIME),
	
	COLLECTION(ITYPE_COLLECTION),
	MAP(ITYPE_MAP),
	
	ENUM(ITYPE_ENUM),
	CLASS(ITYPE_CLASS),
	BEAN(ITYPE_BEAN);
	
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
	
	public static SpearalType valueOf(int parameterizedType) {
		
		if (parameterizedType >= 0x10) {
			if (parameterizedType <= 0xff) {
				SpearalType type = SIO_TYPES[parameterizedType & 0xf0];
				if (type != null)
					return type;
			}
		}
		else if (parameterizedType >= 0) {
			SpearalType type = SIO_TYPES[parameterizedType];
			if (type != null)
				return type;
		}
		
		throw new IllegalArgumentException("Illegal parameterized type: " + parameterizedType);
	}
}
