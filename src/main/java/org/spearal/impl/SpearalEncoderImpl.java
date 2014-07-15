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

import static org.spearal.impl.SharedConstants.BIG_NUMBER_ALPHA_MIRROR;
import static org.spearal.impl.SharedConstants.UTF8;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalPropertyFilter;
import org.spearal.configuration.EncoderBeanDescriptorFactory.EncoderBeanDescriptor;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.cache.IdentityIndexMap;
import org.spearal.impl.cache.StringIndexMap;

/**
 * @author Franck WOLFF
 */
public class SpearalEncoderImpl implements ExtendedSpearalEncoder, SpearalIType {

	private final SpearalContext context;
	private final SpearalPropertyFilter propertyFilter;
	private final OutputStream out;
	
	private final StringIndexMap sharedStrings;
	private final IdentityIndexMap sharedObjects;
	private final Map<Class<?>, EncoderBeanDescriptor> descriptors;
	
	private final byte[] buffer;
	private int position;
	
	private int depth;
	
	public SpearalEncoderImpl(SpearalContext context, OutputStream out) {
		this(context, null, out, 1024);
	}
	
	public SpearalEncoderImpl(SpearalContext context, OutputStream out, int capacity) {
		this(context, null, out, capacity);
	}
	
	public SpearalEncoderImpl(SpearalContext context, SpearalPropertyFilter request, OutputStream out) {
		this(context, request, out, 1024);
	}
	
	public SpearalEncoderImpl(final SpearalContext context, SpearalPropertyFilter propertyFilter, OutputStream out, int capacity) {
		this.context = context;
		this.propertyFilter = (propertyFilter != null ? propertyFilter : new SpearalPropertyFilterImpl(context));
		this.out = out;
		
		this.sharedStrings = new StringIndexMap();
		this.sharedObjects = new IdentityIndexMap();
		this.descriptors = new IdentityHashMap<Class<?>, EncoderBeanDescriptor>(32);

		this.buffer = new byte[capacity];
        this.position = 0;
        
        this.depth = 0;
	}
	
	@Override
	public SpearalContext getContext() {
		return context;
	}

	@Override
	public SpearalPropertyFilter getPropertyFilter() {
		return propertyFilter;
	}

	@Override
	public void writeAny(Object o) throws IOException {
		++depth;
		
		if (o == null)
			writeNull();
		else
			context.getCoder(o.getClass()).encode(this, o);
		
		if ((--depth) == 0)
			flushBuffer();
	}

	@Override
	public void writeNull() throws IOException {
		ensureCapacity(1);
		buffer[position++] = ITYPE_NULL;
	}
	
	@Override
	public void writeBoolean(boolean value) throws IOException {
		ensureCapacity(1);
		buffer[position++] = (value ? (byte)ITYPE_TRUE : (byte)ITYPE_FALSE);
	}
	
	@Override
	public void writeDateTime(SpearalDateTime date) throws IOException {
		
		int parameters = 0x00;
		int subsecs = date.nanoseconds;
		
		if (date.hasDate)
			parameters |= 0x08;
		
		if (date.hasTime) {
			parameters |= 0x04;
			if (subsecs != 0) {
				if (subsecs % 1000 == 0) {
					if (subsecs % 1000000 == 0) {
						subsecs /= 1000000;
						parameters |= 0x03;
					}
					else {
						subsecs /= 1000;
						parameters |= 0x02;
					}
				}
				else
					parameters |= 0x01;
			}
		}
		
		ensureCapacity(1);
		buffer[position++] = (byte)(ITYPE_DATE_TIME | parameters);
		
		if (date.hasDate) {
			int year = date.year - 2000;
			
			int inverse;
			if (year < 0) {
				inverse = 0x80;
				year = -year;
			}
			else
				inverse = 0x00;
			
			int length0 = unsignedIntLength0(year);
			
			ensureCapacity(length0 + 3);
			buffer[position++] = (byte)(inverse | (length0 << 4) | date.month);
			buffer[position++] = (byte)date.date;
			writeUnsignedIntValue(year, length0);
		}
		
		if (date.hasTime) {
			if (subsecs == 0) {
				ensureCapacity(3);
				buffer[position++] = (byte)date.hours;
				buffer[position++] = (byte)date.minutes;
				buffer[position++] = (byte)date.seconds;
			}
			else {
				int length0 = unsignedIntLength0(subsecs);
				ensureCapacity(length0 + 4);
				buffer[position++] = (byte)((length0 << 5) | date.hours);
				buffer[position++] = (byte)date.minutes;
				buffer[position++] = (byte)date.seconds;
				writeUnsignedIntValue(subsecs, length0);				
			}
		}
	}
	
	@Override
	public void writeByte(byte value) throws IOException {
		int inverse = 0;
		if (value < 0) {
			inverse = 0x08;
			if (value != Byte.MIN_VALUE)
				value = (byte)-value;
		}
		
		ensureCapacity(2);
		buffer[position++] = (byte)(ITYPE_INTEGRAL | inverse);
		buffer[position++] = value;
	}
	
	@Override
	public void writeShort(short value) throws IOException {
		int inverse;
		int length0;
		if (value < 0) {
			inverse = 0x08;
			if (value == Short.MIN_VALUE)
				length0 = 1;
			else {
				value = (short)-value;
				length0 = (value <= 0xff ? 0 : 1);
			}
		}
		else {
			inverse = 0;
			length0 = (value <= 0xff ? 0 : 1);
		}
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(ITYPE_INTEGRAL | inverse | length0);
		
		if (length0 == 1)
			buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
	}
	
	@Override
	public void writeInt(int value) throws IOException {
		int inverse;
		int length0;
		if (value < 0) {
			inverse = 0x08;
			if (value == Integer.MIN_VALUE)
				length0 = 3;
			else {
				value = -value;
				length0 = unsignedIntLength0(value);
			}
		}
		else {
			inverse = 0;
			length0 = unsignedIntLength0(value);
		}
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(ITYPE_INTEGRAL | inverse | length0);
		writeUnsignedIntValue(value, length0);
	}
	
	@Override
	public void writeLong(long value) throws IOException {
		int inverse = 0;
		int length0;
		if (value < 0) {
			if (value == Long.MIN_VALUE)
				length0 = 7;
			else {
				inverse = 0x08;
				value = -value;
				length0 = unsignedLongLength0(value);
			}
		}
		else
			length0 = unsignedLongLength0(value);
		
		ensureCapacity(length0 + 2);
		
		final byte[] buffer = this.buffer;
		int position = this.position;
		
		buffer[position++] = (byte)(ITYPE_INTEGRAL | inverse | length0);
		
		switch (length0) {
		case 7:
			buffer[position++] = (byte)(value >>> 56);
		case 6:
			buffer[position++] = (byte)(value >>> 48);
		case 5:
			buffer[position++] = (byte)(value >>> 40);
		case 4:
			buffer[position++] = (byte)(value >>> 32);
		case 3:
			buffer[position++] = (byte)(value >>> 24);
		case 2:
			buffer[position++] = (byte)(value >>> 16);
		case 1:
			buffer[position++] = (byte)(value >>> 8);
		case 0:
			buffer[position++] = (byte)value;
			break;
		default:
			throw new RuntimeException("Internal error: length0=" + length0);
		}
		
		this.position = position;
	}
	
	@Override
	public void writeBigInteger(BigInteger value) throws IOException {
		writeBigNumberData(ITYPE_BIG_INTEGRAL, exponentize(value));
	}

	@Override
	public void writeFloat(float value) throws IOException {
		writeDouble(value);
	}

	@Override
	public void writeDouble(double value) throws IOException {

		long bits = Double.doubleToLongBits(value);
		
		// Not NaN, +/-Infinity or -0.0
		if ((bits & 0x7ff0000000000000L) != 0x7ff0000000000000L && bits != 0x8000000000000000L) {
			
			long doubleAsLong = (long)value;
			
			if (value == doubleAsLong) {
				// 6.5 bytes max (absolute value), i.e. max length of a IEEE 754 fraction part.
				if (doubleAsLong >= -0x000fffffffffffffL && doubleAsLong <= 0x000fffffffffffffL) {
					writeLong(doubleAsLong);
					return;
				}
			}
			else {
				doubleAsLong = (long)(value * 1000.0);

				if (value == (doubleAsLong / 1000.0) ||
					value == ((doubleAsLong += (doubleAsLong < 0 ? -1 : 1)) / 1000.0)) {
					
					// 4 bytes max (absolute value)
					if (doubleAsLong >= -0xffffffffL && doubleAsLong <= 0xffffffffL) {
						int inverse;
						
						if (doubleAsLong < 0) {
							doubleAsLong = -doubleAsLong;
							inverse = 0x04;
						}
						else
							inverse = 0x00;
						
						int length0 = unsignedLongLength0(doubleAsLong);
						
						ensureCapacity(length0 + 2);
	
						final byte[] buffer = this.buffer;
						int position = this.position;
						
						buffer[position++] = (byte)(ITYPE_FLOATING | 0x08 | inverse | length0);
						
						switch (length0) {
						case 3:
							buffer[position++] = (byte)(doubleAsLong >>> 24);
						case 2:
							buffer[position++] = (byte)(doubleAsLong >>> 16);
						case 1:
							buffer[position++] = (byte)(doubleAsLong >>> 8);
						case 0:
							buffer[position++] = (byte)doubleAsLong;
							break;
						default:
							throw new RuntimeException("Internal error: length0=" + length0);
						}
						
						this.position = position;
						
						return;
					}
				}
			}
		}
		
		ensureCapacity(9);
		buffer[position++] = ITYPE_FLOATING;
		writeLongData(bits);
	}

	@Override
	public void writeBigDecimal(BigDecimal value) throws IOException {
		writeBigNumberData(ITYPE_BIG_FLOATING, value.toString());
	}
	
	@Override
	public void writeChar(char value) throws IOException {
		writeStringData(ITYPE_STRING, String.valueOf(value));
	}

	@Override
	public final void writeString(String value) throws IOException {
		writeStringData(ITYPE_STRING, value);
	}

	@Override
	public void writeByteArray(byte[] value) throws IOException {
		if (!putAndWriteObjectReference(ITYPE_BYTE_ARRAY, value)) {
			writeTypeUint(ITYPE_BYTE_ARRAY, value.length);
			writeBytes(value);
		}
	}

	@Override
	public void writeArray(Object value) throws IOException {
		if (!putAndWriteObjectReference(ITYPE_COLLECTION, value)) {
			final int size = Array.getLength(value);
			writeTypeUint(ITYPE_COLLECTION, size);
			for (int i = 0; i < size; i++)
				writeAny(Array.get(value, i));
		}
	}

	@Override
	public void writeCollection(Collection<?> value) throws IOException {
		if (!putAndWriteObjectReference(ITYPE_COLLECTION, value)) {
			final int size = value.size();
			writeTypeUint(ITYPE_COLLECTION, size);
			for (Object item : value)
				writeAny(item);
		}
	}

	@Override
	public void writeMap(Map<?, ?> value) throws IOException {
		if (!putAndWriteObjectReference(ITYPE_MAP, value)) {
			final int size = value.size();
			writeTypeUint(ITYPE_MAP, size);
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				writeAny(entry.getKey());
				writeAny(entry.getValue());
			}
		}
	}

	@Override
	public void writeEnum(Enum<?> value) throws IOException {
		writeStringData(ITYPE_ENUM, value.getClass().getName());
		writeStringData(ITYPE_STRING, value.name());
	}
	
	@Override
	public void writeClass(Class<?> value) throws IOException {
		writeStringData(ITYPE_CLASS, value.getName());
	}

	@Override
	public void writeBean(Object value) throws IOException {
		if (!putAndWriteObjectReference(ITYPE_BEAN, value)) {
			Class<?> cls = value.getClass();
			
			EncoderBeanDescriptor descriptor = descriptors.get(cls);
			if (descriptor == null) {
				descriptor = context.createDescriptor(this, value);
				if (descriptor.isCacheable())
					descriptors.put(cls, descriptor);
			}
			
			writeStringData(ITYPE_BEAN, descriptor.getDescription());
			
			for (Property property : descriptor.getProperties()) {
				if (property == null)
					continue;
				try {
					property.write(this, value);
				}
				catch (IOException e) {
					throw e;
				}
				catch (Exception e) {
					throw new IOException(e);
				}
			}
		}
	}
	
    private void writeStringData(int type, String s) throws IOException {
    	final int length = s.length();

    	if (length == 0) {
        	ensureCapacity(2);
    		buffer[position++] = (byte)type;
        	buffer[position++] = 0;
            return;
        }
    	
    	if (!putAndWriteStringReference(type, s)) {
        	byte[] bytes = s.getBytes(UTF8);
        	writeTypeUint(type, bytes.length);
        	writeBytes(bytes);
        }
    }
	
	private static String exponentize(BigInteger value) {
		String representation = value.toString(10);
		final int length = representation.length();
		if (length > 3) {
			int trailingZeros = 0;
			for (int i = length - 1; i > 0 && representation.charAt(i) == '0'; i--)
				trailingZeros++;
			if (trailingZeros > 2)
				representation = representation.substring(0, length - trailingZeros) + "E" + trailingZeros;
		}
		return representation;
	}
	
	private void writeBigNumberData(int type, String representation) throws IOException {
        if (!putAndWriteStringReference(type, representation)) {
        	final int length = representation.length();
        	writeTypeUint(type, length);
        	
	    	final byte[] buffer = this.buffer;
	    	final int bufferLength = buffer.length;
	    	int position = this.position;
	
	    	for (int i = 0; i < length; ) {
	    		if (position >= bufferLength) {
	    			this.position = bufferLength;
	    			flushBuffer();
	    			position = 0;
	    		}
	    		int b = (BIG_NUMBER_ALPHA_MIRROR[representation.charAt(i++)] << 4);
	    		if (i == length) {
	    			buffer[position++] = (byte)b;
	    			break;
	    		}
	    		b |= BIG_NUMBER_ALPHA_MIRROR[representation.charAt(i++)];
	    		buffer[position++] = (byte)b;
	    	}
	    	this.position = position;
        }
	}
	
	private boolean putAndWriteObjectReference(int type, Object o) throws IOException {
		int index = sharedObjects.putIfAbsent(o);
		
		if (index == -1)
			return false;
        
		int length0 = unsignedIntLength0(index);
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(type | 0x08 | length0);
		writeUnsignedIntValue(index, length0);
		return true;
	}
	
	private boolean putAndWriteStringReference(int type, String s) throws IOException {
		int index = sharedStrings.putIfAbsent(s);
		
		if (index == -1)
			return false;
        
		int length0 = unsignedIntLength0(index);
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(type | 0x04 | length0);
		writeUnsignedIntValue(index, length0);
		return true;
	}
	
	private void writeTypeUint(int type, int uint) throws IOException {
		final int length0 = unsignedIntLength0(uint);
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(type | length0);
		writeUnsignedIntValue(uint, length0);
	}

	private static int unsignedLongLength0(long value) {
		if (value <= 0xffffffffL) {
			if (value <= 0xffffL)
				return (value <= 0xffL ? 0 : 1);
			return (value <= 0xffffffL ? 2 : 3);
		}
		if (value <= 0xffffffffffffL)
			return (value <= 0xffffffffffL ? 4 : 5);
		return (value <= 0xffffffffffffffL ? 6 : 7);
	}
	
	private void writeLongData(long value) {
		final byte[] buffer = this.buffer;
		int position = this.position;
		
		buffer[position++] = (byte)(value >>> 56);
		buffer[position++] = (byte)(value >>> 48);
		buffer[position++] = (byte)(value >>> 40);
		buffer[position++] = (byte)(value >>> 32);
		buffer[position++] = (byte)(value >>> 24);
		buffer[position++] = (byte)(value >>> 16);
		buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
		
		this.position = position;
	}

	private static int unsignedIntLength0(int value) {
		if (value <= 0xff)
			return 0;
		if (value <= 0xffff)
			return 1;
		return (value <= 0xffffff ? 2 : 3);
	}
	
	private void writeUnsignedIntValue(int value, int length0) {
		switch (length0) {
		case 3:
			buffer[position++] = (byte)(value >>> 24);
		case 2:
			buffer[position++] = (byte)(value >>> 16);
		case 1:
			buffer[position++] = (byte)(value >>> 8);
		case 0:
			buffer[position++] = (byte)value;
			break;
		default:
			throw new RuntimeException("Internal error: length0=" + length0);
		}
	}

    private void ensureCapacity(int capacity) throws IOException {
		if (buffer.length - position < capacity)
			flushBuffer();
	}
	
    private void flushBuffer() throws IOException {
		if (position > 0) {
			out.write(buffer, 0, position);
			position = 0;
		}
	}
    
    private void writeBytes(byte[] bytes) throws IOException {
    	if (bytes.length > 0) {
	    	if (buffer.length - position >= bytes.length) {
	    		System.arraycopy(bytes, 0, buffer, position, bytes.length);
	    		position += bytes.length;
	    	}
	    	else {
	    		flushBuffer();
	    		out.write(bytes, 0, bytes.length);
	    	}
    	}
    }
}
