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
public interface SpearalIType {

	// No parameters (0x00...0x0f).
	
	static final int ITYPE_NULL = 0x00;

	static final int ITYPE_TRUE = 0x01;
	static final int ITYPE_FALSE = 0x02;
	
	// 4 bits of parameters (0x10...0xf0).

	static final int ITYPE_INTEGRAL = 0x10;
	static final int ITYPE_BIG_INTEGRAL = 0x20;
	
	static final int ITYPE_FLOATING = 0x30;
	static final int ITYPE_BIG_FLOATING = 0x40;

	static final int ITYPE_STRING = 0x50;
	
	static final int ITYPE_BYTE_ARRAY = 0x60;

	static final int ITYPE_DATE_TIME = 0x70;
	
	static final int ITYPE_COLLECTION = 0x80;
	static final int ITYPE_MAP = 0x90;
	
	static final int ITYPE_ENUM = 0xa0;
	static final int ITYPE_CLASS = 0xb0;
	static final int ITYPE_BEAN = 0xc0;
}
