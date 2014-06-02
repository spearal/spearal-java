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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.spearal.SpearalEncoder;

/**
 * @author Franck WOLFF
 */
public interface ExtendedSpearalEncoder extends SpearalEncoder {
	
	void writeNull() throws IOException;
	
	void writeBoolean(boolean value) throws IOException;

	void writeDate(Date value) throws IOException;
	void writeTimestamp(Timestamp value) throws IOException;

	void writeByte(byte value) throws IOException;
	void writeShort(short value) throws IOException;
	void writeInt(int value) throws IOException;
	void writeLong(long value) throws IOException;
	void writeBigInteger(BigInteger value) throws IOException;

	void writeFloat(float value) throws IOException;
	void writeDouble(double value) throws IOException;
	void writeBigDecimal(BigDecimal value) throws IOException;

	void writeChar(char value) throws IOException;
	void writeString(String value) throws IOException;
	
	void writeByteArray(byte[] value) throws IOException;
	void writeArray(Object value) throws IOException;
	
	void writeCollection(Collection<?> value) throws IOException;
	void writeMap(Map<?, ?> value) throws IOException;
	
	void writeEnum(Enum<?> value) throws IOException;
	void writeClass(Class<?> value) throws IOException;
	void writeBean(Object value) throws IOException;
}
