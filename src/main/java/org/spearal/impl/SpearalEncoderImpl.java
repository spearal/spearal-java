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
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalRequest;
import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.util.ClassCache;
import org.spearal.impl.util.ClassCache.ValueProvider;
import org.spearal.impl.util.ObjectIndexedCache;
import org.spearal.impl.util.StringIndexedCache;

/**
 * @author Franck WOLFF
 */
public class SpearalEncoderImpl implements ExtendedSpearalEncoder, SpearalIType {

	private final SpearalContext context;
	private final SpearalRequest request;
	private final OutputStream out;
	
	private final StringIndexedCache storedStrings;
	private final ObjectIndexedCache storedObjects;
	private final ClassCache<ClassDescriptor> descriptors;
	
	private final ClassCache<ObjectWriter> writers;
	
	private final byte[] buffer;
	private int position;
	
	private int depth;
	
	public SpearalEncoderImpl(SpearalContext context, OutputStream out) {
		this(context, null, out, 1024);
	}
	
	public SpearalEncoderImpl(SpearalContext context, OutputStream out, int capacity) {
		this(context, null, out, capacity);
	}
	
	public SpearalEncoderImpl(SpearalContext context, SpearalRequest request, OutputStream out) {
		this(context, request, out, 1024);
	}
	
	public SpearalEncoderImpl(final SpearalContext context, SpearalRequest request, OutputStream out, int capacity) {
		this.context = context;
		this.request = (request != null ? request : new SpearalRequestImpl(context));
		this.out = out;
		
		this.storedStrings = new StringIndexedCache();
		this.storedObjects = new ObjectIndexedCache();
		this.descriptors = new ClassCache<ClassDescriptor>(new ValueProvider<ClassDescriptor>() {
			@Override
			public ClassDescriptor createValue(Class<?> key) {
				context.getSecurizer().checkEncodable(key);
				return createDescriptor(key);
			}
		});
		
		this.writers = new ClassCache<ObjectWriter>(new ValueProvider<ObjectWriter>() {
			@Override
			public ObjectWriter createValue(Class<?> key) {
				return context.getWriter(key);
			}
		});

		this.buffer = new byte[capacity];
        this.position = 0;
        
        this.depth = 0;
	}
	
	@Override
	public SpearalContext getContext() {
		return context;
	}

	@Override
	public SpearalRequest getRequest() {
		return request;
	}

	@Override
	public void writeAny(Object o) throws IOException {
		++depth;
		
		if (o == null)
			writeNull();
		else
			writers.putIfAbsent(o.getClass()).write(this, o);
		
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
			buffer[position++] = (byte)date.day;
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
	
	private static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
	
	@Override
	public void writeBigInteger(BigInteger value) throws IOException {
		if (value.compareTo(LONG_MIN_VALUE) >= 0 && value.compareTo(LONG_MAX_VALUE) <= 0) {
			writeLong(value.longValue());
			return;
		}
		
		byte[] bytes = value.toByteArray();
		int length0 = unsignedIntLength0(bytes.length);
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(ITYPE_BIG_INTEGRAL | length0);
		writeUnsignedIntValue(bytes.length, length0);
		
		writeBytes(bytes);
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
		byte[] bytes = value.unscaledValue().toByteArray();
		int length0 = unsignedIntLength0(bytes.length);

		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(ITYPE_BIG_FLOATING | length0);
		writeUnsignedIntValue(bytes.length, length0);
		
		writeBytes(bytes);

		writeInt(value.scale());
	}
	
	@Override
	public void writeChar(char value) throws IOException {
		writeString(String.valueOf(value));
	}

	@Override
	public void writeString(String value) throws IOException {
		ensureCapacity(1);
		buffer[position] = ITYPE_STRING;
		
		writeStringData(value);
	}

	@Override
	public void writeByteArray(byte[] value) throws IOException {
		
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_BYTE_ARRAY | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int length0 = unsignedIntLength0(value.length);
			
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_BYTE_ARRAY | length0);
			writeUnsignedIntValue(value.length, length0);
	
			writeBytes(value);
		}
	}

	@Override
	public void writeArray(Object value) throws IOException {
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_COLLECTION | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int size = Array.getLength(value);
			int length0 = unsignedIntLength0(size);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_COLLECTION | length0);
			writeUnsignedIntValue(size, length0);
			for (int i = 0; i < size; i++)
				writeAny(Array.get(value, i));
		}
	}

	@Override
	public void writeCollection(Collection<?> value) throws IOException {
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_COLLECTION | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int size = value.size();
			int length0 = unsignedIntLength0(size);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_COLLECTION | length0);
			writeUnsignedIntValue(size, length0);
			
			for (Object item : value)
				writeAny(item);
		}
	}

	@Override
	public void writeMap(Map<?, ?> value) throws IOException {
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_MAP | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int size = value.size();
			int length0 = unsignedIntLength0(size);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_MAP | length0);
			writeUnsignedIntValue(size, length0);
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				writeAny(entry.getKey());
				writeAny(entry.getValue());
			}
		}
	}

	@Override
	public void writeEnum(Enum<?> value) throws IOException {
		ensureCapacity(1);
		buffer[position] = (byte)ITYPE_ENUM;
		writeStringData(value.getClass().getName());
		
		writeString(value.name());
	}
	
	@Override
	public void writeClass(Class<?> value) throws IOException {
		ensureCapacity(1);
		buffer[position] = (byte)ITYPE_CLASS;
		writeStringData(value.getName());
	}

	@Override
	public void writeBean(Object value) throws IOException {

		int reference = storedObjects.putIfAbsent(value);
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(ITYPE_BEAN | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			ClassDescriptor descriptor = descriptors.putIfAbsent(value.getClass());
			
			ensureCapacity(1);
			buffer[position] = (byte)ITYPE_BEAN;
			writeStringData(descriptor.description);
			
			for (Property property : descriptor.properties) {
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
	
	private ClassDescriptor createDescriptor(Class<?> cls) {
		StringBuilder sb = new StringBuilder();

		if (!Proxy.isProxyClass(cls))
			sb.append(context.getClassNameAlias(cls.getName())).append(':');
		else {
			for (Class<?> inter : cls.getInterfaces())
				sb.append(context.getClassNameAlias(inter.getName())).append(':');
		}

		Collection<Property> selectedProperties = request.getFilteredProperties(cls);
		boolean first = true;
		for (Property property : selectedProperties) {
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(property.getName());
		}
		
		return new ClassDescriptor(sb.toString(), selectedProperties);
	}
    
    private void writeStringData(String s) throws IOException {
    	final int length = s.length();

    	if (length == 0) {
    		position++;
        	ensureCapacity(1);
        	buffer[position++] = 0;
            return;
        }

        int index = storedStrings.putIfAbsent(s);

        if (index >= 0) {
        	int length0 = unsignedIntLength0(index);
        	buffer[position++] |= (0x04 | length0);
        	ensureCapacity(length0 + 1);
        	writeUnsignedIntValue(index, length0);
        }
        else {
            final int count = utfByteCount(s);
            int length0 = unsignedIntLength0(count);
            if (length0 > 0)
            	buffer[position++] |= length0;
            else
            	position++;
            ensureCapacity(length0 + 1);
        	writeUnsignedIntValue(count, length0);
            
        	final byte[] buffer = this.buffer;
        	final int bufferLength = buffer.length;
            
        	int position = this.position;
            
            // String chars are in [0x0000, 0x007F]: write them directly as bytes.
            if (count == length) {
                if (length <= bufferLength - position) {
                	for (int i = 0; i < length; i++)
                		buffer[position++] = (byte)s.charAt(i);
                	this.position = position;
                }
                else {
    	        	
    	        	int i = 0;
    	        	while (position < bufferLength)
    	        		buffer[position++] = (byte)s.charAt(i++);
    	        	this.position = position;
    	        	
    	        	do {
    		        	flushBuffer();
    		        	position = 0;

    		        	int max = Math.min(bufferLength, length - i);
    		        	while (position < max)
    		        		buffer[position++] = (byte)s.charAt(i++);
    		        	this.position = position;
    	        	}
    	        	while (i < length);
                }
            }
            // We have at least one char > 0x007F but enough buffer to write them all.
            else if (count <= bufferLength - position) {
            	
            	for (int i = 0; i < length; i++) {
                	char c = s.charAt(i);
                	if (c <= 0x007F)
                    	buffer[position++] = (byte)c;
                    else if (c > 0x07FF) {
                    	buffer[position++] = (byte)(0xE0 | (c >>> 12));
                    	buffer[position++] = (byte)(0x80 | ((c >>> 6) & 0x3F));
                    	buffer[position++] = (byte)(0x80 | (c & 0x3F));
                    }
                    else {
                    	buffer[position++] = (byte)(0xC0 | ((c >>> 6) & 0x1F));
                    	buffer[position++] = (byte)(0x80 | (c & 0x3F));
                    }
                }
                
                this.position = position;
            }
            // We have at least one char > 0x007F and not enough buffer to write them all.
            else {
	        	final int bufferLengthMinus3 = buffer.length - 3;
            	
            	int i = 0, total = 0;
	        	do {
	            	flushBuffer();

	            	position = 0;
	            	final int max = Math.min(count - total, bufferLengthMinus3);
	            	
	            	while (position < max) {
	            		char c = s.charAt(i++);
		            	if (c <= 0x007F)
		                	buffer[position++] = (byte)c;
		                else if (c > 0x07FF) {
		                	buffer[position++] = (byte)(0xE0 | (c >>> 12));
		                	buffer[position++] = (byte)(0x80 | ((c >>> 6) & 0x3F));
		                	buffer[position++] = (byte)(0x80 | (c & 0x3F));
		                }
		                else {
		                	buffer[position++] = (byte)(0xC0 | ((c >>> 6) & 0x1F));
		                	buffer[position++] = (byte)(0x80 | (c & 0x3F));
		                }
	            	}
	            	
	            	total += position;
	            	this.position = position;
	        	}
	        	while (total < count);
            }
        }
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
/*	
	private void writeIntData(int value) {
		final byte[] buffer = this.buffer;
		int position = this.position;
		
		buffer[position++] = (byte)(value >>> 24);
		buffer[position++] = (byte)(value >>> 16);
		buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
		
		this.position = position;
	}
*/	
	private static int unsignedIntLength0(int value) {
		if (value <= 0xffff)
			return (value <= 0xff ? 0 : 1);
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
/*	
	private void writeVariableUnsignedIntValue(int value) throws IOException {
		int length;
		if (value <= 0x3fff)
			length = (value <= 0x7f ? 1 : 2);
		else if (value <= 0xfffffff)
			length = (value <= 0x1fffff ? 3 : 4);
		else
			length = 5;
		
		ensureCapacity(length);
		
		switch (length) {
		case 5:
			buffer[position++] = (byte)0xf0;
			break;
		case 4:
			buffer[position] = (byte)0xe0;
			break;
		case 3:
			buffer[position] = (byte)0xc0;
			break;
		case 2:
			buffer[position] = (byte)0x80;
			break;
		}
		
		switch (length) {
		case 5:
		case 4:
			buffer[position++] = (byte)(value >>> 24);
		case 3:
			buffer[position++] = (byte)(value >>> 16);
		case 2:
			buffer[position++] = (byte)(value >>> 8);
		case 1:
			buffer[position++] = (byte)value;
		}
	}
	
	private void writeVariableIntValue(int value) throws IOException {
		int inverse = 0x00;
		if (value < 0 && value != Integer.MIN_VALUE) {
			inverse = 0x80;
			value = -value;
		}
		
		int length;
		if (value <= 0x1fff)
			length = (value <= 0x3f ? 1 : 2);
		else if (value <= 0x7ffffff)
			length = (value <= 0xfffff ? 3 : 4);
		else
			length = 5;
		
		ensureCapacity(length);
		
		switch (length) {
		case 5:
			buffer[position++] = (byte)(inverse | 0x78);
			break;
		case 4:
			buffer[position] = (byte)(inverse | 0x70);
			break;
		case 3:
			buffer[position] = (byte)(inverse | 0x60);
			break;
		case 2:
			buffer[position] = (byte)(inverse | 0x40);
			break;
		}
		
		switch (length) {
		case 5:
		case 4:
			buffer[position++] = (byte)(value >>> 24);
		case 3:
			buffer[position++] = (byte)(value >>> 16);
		case 2:
			buffer[position++] = (byte)(value >>> 8);
		case 1:
			buffer[position++] = (byte)value;
		}
	}
*/
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
    
    private static int utfByteCount(String s) {
    	final int length = s.length();
    	
    	int count = length;
        for (int i = 0; i < length; i++) {
        	char c = s.charAt(i);
        	if (c > 0x007F) {
        		if (c > 0x07FF)
        			count += 2;
        		else
        			count++;
        	}
        }
        return count;
    }
	
	private static class ClassDescriptor {
		
		public final String description;
		public final Collection<Property> properties;
		
		public ClassDescriptor(String description, Collection<Property> properties) {
			this.description = description;
			this.properties = properties;
		}
	}
}
