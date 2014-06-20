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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;
import org.spearal.impl.SpearalDateTime;

/**
 * @author Franck WOLFF
 */
public class SimpleCodersProvider implements CoderProvider {

	private final Map<Class<?>, Coder> coders;
	
	public SimpleCodersProvider() {
		this.coders = new HashMap<Class<?>, Coder>();

		// Boolean
		
		coders.put(Boolean.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeBoolean(((Boolean)value).booleanValue());
			}
		});

		// Numbers
		
		coders.put(Byte.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeByte(((Byte)value).byteValue());
			}
		});
		
		coders.put(Short.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeShort(((Short)value).shortValue());
			}
		});
		
		coders.put(Integer.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeInt(((Integer)value).intValue());
			}
		});
		
		coders.put(Long.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeLong(((Long)value).longValue());
			}
		});
		
		coders.put(BigInteger.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeBigInteger(((BigInteger)value));
			}
		});
		
		coders.put(Float.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeFloat(((Float)value).floatValue());
			}
		});
		
		coders.put(Double.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDouble(((Double)value).doubleValue());
			}
		});
		
		coders.put(BigDecimal.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeBigDecimal(((BigDecimal)value));
			}
		});
		
		// String / Character
		
		coders.put(String.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeString((String)value);
			}
		});
		
		coders.put(char[].class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeString(String.valueOf((char[])value));
			}
		});
		
		coders.put(Character.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeString(String.valueOf(((Character)value).charValue()));
			}
		});
		
		// Byte array
		
		coders.put(byte[].class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeByteArray(((byte[])value));
			}
		});
		
		// Temporal types.
		
		coders.put(GregorianCalendar.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDateTime(SpearalDateTime.forGregorianCalendar((GregorianCalendar)value));
			}
		});
		
		coders.put(Date.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDateTime(SpearalDateTime.forDate((Date)value));
			}
		});
		
		coders.put(java.sql.Date.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDateTime(SpearalDateTime.forSQLDate((java.sql.Date)value));
			}
		});
		
		coders.put(Time.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDateTime(SpearalDateTime.forSQLTime((Time)value));
			}
		});
		
		coders.put(Timestamp.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeDateTime(SpearalDateTime.forSQLTimestamp((Timestamp)value));
			}
		});
		
		// Class.
		
		coders.put(Class.class, new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeClass((Class<?>)value);
			}
		});
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return coders.get(valueClass);
	}
}
