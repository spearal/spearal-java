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

import static org.spearal.impl.SpearalType.BEAN;
import static org.spearal.impl.SpearalType.BIG_INTEGRAL;
import static org.spearal.impl.SpearalType.BYTE_ARRAY;
import static org.spearal.impl.SpearalType.CLASS;
import static org.spearal.impl.SpearalType.COLLECTION;
import static org.spearal.impl.SpearalType.DATE;
import static org.spearal.impl.SpearalType.ENUM;
import static org.spearal.impl.SpearalType.FALSE;
import static org.spearal.impl.SpearalType.FLOATING;
import static org.spearal.impl.SpearalType.INTEGRAL;
import static org.spearal.impl.SpearalType.MAP;
import static org.spearal.impl.SpearalType.STRING;
import static org.spearal.impl.SpearalType.TRUE;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalRequest;
import org.spearal.configurable.ObjectWriterProvider.ObjectWriter;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.util.ObjectIndexedCache;
import org.spearal.impl.util.StringIndexedCache;

/**
 * @author Franck WOLFF
 */
public class SpearalOutputImpl implements ExtendedSpearalOutput {

	private final SpearalContext context;
	private final SpearalRequest request;
	private final OutputStream out;
	
	private final StringIndexedCache storedStrings;
	private final ObjectIndexedCache storedObjects;
	private final Map<Class<?>, ClassDescriptor> descriptors;
	
	private final byte[] buffer;
	private int position;
	
	private int depth;
	
	public SpearalOutputImpl(SpearalContext context, OutputStream out) {
		this(context, null, out, 1024);
	}
	
	public SpearalOutputImpl(SpearalContext context, OutputStream out, int capacity) {
		this(context, null, out, capacity);
	}
	
	public SpearalOutputImpl(SpearalContext context, SpearalRequest request, OutputStream out) {
		this(context, request, out, 1024);
	}
	
	public SpearalOutputImpl(SpearalContext context, SpearalRequest request, OutputStream out, int capacity) {
		this.context = context;
		this.request = (request != null ? request : new SpearalRequestImpl(context));
		this.out = out;
		
		this.storedStrings = new StringIndexedCache();
		this.storedObjects = new ObjectIndexedCache();
		this.descriptors = new IdentityHashMap<Class<?>, ClassDescriptor>();

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
		depth++;
		
		if (o == null)
			writeNull();
		else {
			Class<?> cls = o.getClass();
			
			ObjectWriter writer = context.getWriter(cls);
			writer.write(this, o);
		}
		
		depth--;
		
		if (depth == 0)
			flushBuffer();
	}
	
	@Override
	public void writeClass(Class<?> value) throws IOException {
		ensureCapacity(1);
		buffer[position] = (byte)CLASS.id();
		writeStringData(value.getName());
	}

	@Override
	public void writeBean(Object value) throws IOException {
		if (value == null)
			throw new NullPointerException();
		
		int reference = storedObjects.putIfAbsent(value);
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(BEAN.id() | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			Class<?> cls = value.getClass();
			
			ClassDescriptor descriptor = descriptors.get(cls);
			if (descriptor == null) {
				String className = context.getClassNameAlias(cls.getName());
				StringBuilder sb = new StringBuilder(className).append(':');

				Collection<Property> selectedProperties = request.getFilteredProperties(cls);
				boolean first = true;
				for (Property property : selectedProperties) {
					if (first)
						first = false;
					else
						sb.append(',');
					sb.append(property.getName());
				}
				
				descriptor = new ClassDescriptor(sb.toString(), selectedProperties);
				descriptors.put(cls, descriptor);
			}
			
			ensureCapacity(1);
			buffer[position] = (byte)BEAN.id();
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

	@Override
	public void writeEnum(Enum<?> value) throws IOException {
		ensureCapacity(1);
		buffer[position] = (byte)ENUM.id();
		writeStringData(value.getClass().getName());
		
		writeString(value.name());
	}

	@Override
	public void writeNull() throws IOException {
		ensureCapacity(1);
		buffer[position++] = (byte)SpearalType.NULL.id();
	}
	
	@Override
	public void writeBoolean(boolean value) throws IOException {
		ensureCapacity(1);
		buffer[position++] = (byte)(value ? TRUE.id() : FALSE.id());
	}
	
	@Override
	public void writeChar(char value) throws IOException {
		writeString(String.valueOf(value));
	}
	
	@Override
	public void writeByte(byte value) throws IOException {
		int inverse = 0;
		if (value < 0 && value != Byte.MIN_VALUE) {
			inverse = 0x08;
			value = (byte)-value;
		}
		
		ensureCapacity(2);
		buffer[position++] = (byte)(INTEGRAL.id() | inverse);
		buffer[position++] = value;
	}
	
	@Override
	public void writeShort(short value) throws IOException {
		int inverse = 0;
		int length0;
		if (value < 0) {
			if (value == Short.MIN_VALUE)
				length0 = 1;
			else {
				inverse = 0x08;
				value = (short)-value;
				length0 = (value <= 0xff ? 0 : 1);
			}
		}
		else
			length0 = (value <= 0xff ? 0 : 1);
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(INTEGRAL.id() | inverse | length0);
		
		if (length0 == 1)
			buffer[position++] = (byte)(value >>> 8);
		buffer[position++] = (byte)value;
	}
	
	@Override
	public void writeInt(int value) throws IOException {
		int inverse = 0;
		int length0;
		if (value < 0) {
			if (value == Integer.MIN_VALUE)
				length0 = 3;
			else {
				inverse = 0x08;
				value = -value;
				length0 = unsignedIntLength0(value);
			}
		}
		else
			length0 = unsignedIntLength0(value);
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(INTEGRAL.id() | inverse | length0);
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
		
		buffer[position++] = (byte)(INTEGRAL.id() | inverse | length0);
		
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
	public void writeFloat(float value) throws IOException {
		writeDouble(value);
	}

	@Override
	public void writeDouble(double value) throws IOException {
		ensureCapacity(9);
		buffer[position++] = (byte)FLOATING.id();
		
		writeLongData(Double.doubleToLongBits(value));
	}
	
	private static final BigInteger LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
	private static final BigInteger LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
	
	@Override
	public void writeBigInteger(BigInteger value) throws IOException {
		if (value.compareTo(LONG_MIN_VALUE) >= 0 && value.compareTo(LONG_MAX_VALUE) <= 0) {
			writeLong(value.longValue());
			return;
		}
		
		String digits = value.toString();
		
		int negative = 0x00;
		int start = 0;
		int length = digits.length();
		if (digits.charAt(0) == '-') {
			negative = 0x80;
			start = 1;
			length--;
		}
		
		int length0 = unsignedIntLength0(length);
		
		ensureCapacity(length0 + 2);
		buffer[position++] = (byte)(BIG_INTEGRAL.id() | negative | length0);
		writeUnsignedIntValue(length, length0);
		
		do {
			ensureCapacity(1);
			int b = ((digits.charAt(start++) - '0') << 4);
			if (start < length)
				b |= (digits.charAt(start++) - '0');
			buffer[position++] = (byte)b;
		}
		while (start < length);
	}

	@Override
	public void writeBigDecimal(BigDecimal value) throws IOException {
	}

	@Override
	public void writeString(String value) throws IOException {
		ensureCapacity(1);
		buffer[position] = (byte)(STRING.id());
		
		writeStringData(value);
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
        	buffer[position++] |= (byte)(0x04 | length0);
        	ensureCapacity(length0 + 1);
        	writeUnsignedIntValue(index, length0);
        }
        else {
            final int count = utfByteCount(s);
            int length0 = unsignedIntLength0(count);
            buffer[position++] |= (byte)(0x04 | length0);
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
	
	@Override
	public void writeDate(Date value) throws IOException {
		ensureCapacity(9);

		buffer[position++] = (byte)DATE.id();
		writeLongData(value.getTime());
	}

	@Override
	public void writeByteArray(byte[] value) throws IOException {
		
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(BYTE_ARRAY.id() | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int length0 = unsignedIntLength0(value.length);
			
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(BYTE_ARRAY.id() | length0);
			writeUnsignedIntValue(value.length, length0);
	
			flushBuffer();
			out.write(value);
		}
	}

	@Override
	public void writeArray(Object value) throws IOException {
	}

	@Override
	public void writeCollection(Collection<?> value) throws IOException {
		int reference = storedObjects.putIfAbsent(value);
		
		if (reference != -1) {
			int length0 = unsignedIntLength0(reference);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(COLLECTION.id() | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int size = value.size();
			int length0 = unsignedIntLength0(size);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(COLLECTION.id() | length0);
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
			buffer[position++] = (byte)(MAP.id() | 0x08 | length0);
			writeUnsignedIntValue(reference, length0);
		}
		else {
			int size = value.size();
			int length0 = unsignedIntLength0(size);
			ensureCapacity(length0 + 2);
			buffer[position++] = (byte)(MAP.id() | length0);
			writeUnsignedIntValue(size, length0);
			for (Map.Entry<?, ?> entry : value.entrySet()) {
				writeAny(entry.getKey());
				writeAny(entry.getValue());
			}
		}
	}
	
	private static int unsignedLongLength0(long value) {
		if (value <= 0xffffL)
			return (value <= 0xffL ? 0 : 1);
		if (value <= 0xffffffffL)
			return (value <= 0xffffffL ? 2 : 3);
		if (value <= 0xffffffffffffL)
			return (value <= 0xffffffffL ? 4 : 5);
		return (value <= 0xffffffffffffL ? 6 : 7);
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
		if (value <= 0xffff)
			return (value <= 0xff ? 0 : 1);
		return (value <= 0xffffff ? 2 : 3);
	}
	
	private void writeUnsignedIntValue(int value, int length0) {
		final byte[] buffer = this.buffer;
		int position = this.position;
		
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
		
		this.position = position;
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
