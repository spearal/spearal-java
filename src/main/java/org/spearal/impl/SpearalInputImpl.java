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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.util.TypeUtil;
import org.spearal.impl.util.UnmodifiableArray;

/**
 * @author Franck WOLFF
 */
public class SpearalInputImpl implements ExtendedSpearalInput {

	private final List<String> storedStrings;
	private final List<Object> storedObjects;
	private final Map<String, ClassDescriptor> descriptors;

    private final SpearalContext context;
	
    private final InputStream in;
	private final byte[] buffer;
	private int position;
	private int size;
	private boolean eof;

	public SpearalInputImpl(SpearalContext context, InputStream in) {
		this(context, in, 1024);
	}

	public SpearalInputImpl(SpearalContext context, InputStream in, int capacity) {
        this.storedStrings = new ArrayList<String>(64);
        this.storedObjects = new ArrayList<Object>(64);
        this.descriptors = new HashMap<String, ClassDescriptor>();

        this.context = context;
        this.in = in;
        this.buffer = new byte[capacity];
        this.position = 0;
        this.size = 0;
        this.eof = false;
	}
	
    @Override
	public SpearalContext getContext() {
		return context;
	}

	@Override
    public Object readAny() throws IOException {
    	ensureAvailable(1);
        return readAny(buffer[position++] & 0xff);
    }

//	@Override
//	public Object readAny(Type type) throws IOException {
//    	ensureAvailable(1);
//    	int t = buffer[position++] & 0xff;
//    	return null; // readCollection(t, null, type);
//	}

	@Override
    public Object readAny(int parameterizedType) throws IOException {
        switch (SpearalType.valueOf(parameterizedType)) {
        
        case NULL:
        	return null;
        
        case TRUE:
        	return Boolean.TRUE;
        case FALSE:
        	return Boolean.FALSE;

        case DATE:
        	return readDate(parameterizedType);
        case TIMESTAMP:
        	return readTimestamp(parameterizedType);

        case INTEGRAL:
        	return Long.valueOf(readIntegral(parameterizedType));
        case BIG_INTEGRAL:
        	return readBigIntegral(parameterizedType);
        	
        case FLOATING:
        	return Double.valueOf(readFloating(parameterizedType));
        case BIG_FLOATING:
        	return readBigFloating(parameterizedType);
        	
        case STRING:
        	return readString(parameterizedType);
        
        case BYTE_ARRAY:
        	return readByteArray(parameterizedType);
        case ARRAY:
        	return readArray(parameterizedType);
            
        case COLLECTION:
        	return readCollection(parameterizedType);
        case MAP:
        	return readMap(parameterizedType);
            
        case ENUM:
        	return readEnum(parameterizedType);
        case CLASS:
        	return readClass(parameterizedType);
        case BEAN:
        	return readBean(parameterizedType);
        }
        
        throw new RuntimeException("Unexpected type: " + parameterizedType);
    }

    @Override
	public void skipAny(int parameterizedType) throws IOException {
    	// TODO
		readAny(parameterizedType);
	}

	@Override
	public Class<?> readClass(int parameterizedType) throws IOException {
		int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	String className = readStringData(indexOrLength);
		return context.loadClass(context.getClassNameAlias(className));
	}

	@Override
    public Object readBean(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return storedObjects.get(indexOrLength);
    	
    	String classDescription = readStringData(indexOrLength);
    	
    	ClassDescriptor descriptor = descriptors.get(classDescription);
    	if (descriptor == null) {
    		descriptor = ClassDescriptor.forDescription(context, classDescription);
    		descriptors.put(classDescription, descriptor);
    	}

    	try {
	    	Class<?> cls = descriptor.cls;

	    	Object value = (
	    		descriptor.partial
	    		? context.instantiatePartial(cls, descriptor.properties)
	    		: context.instantiate(cls)
	    	);
	    	storedObjects.add(value);
	    	
	    	for (Property property : descriptor.properties) {
	    		if (property != null) {
	    	    	ensureAvailable(1);
	    	    	int propertyType = (buffer[position++] & 0xff);
	    			property.read(this, value, propertyType);
	    		}
	    		else
	    			skipObject();
	    	}
	    	
	    	return value;
    	}
    	catch (Exception e) {
    		throw new IOException(e);
    	}
    }
    
    private void skipObject() throws IOException {
    	// TODO
    	readAny();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Enum<?> readEnum(int parameterizedType) throws IOException {
		int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	String className = context.getClassNameAlias(readStringData(indexOrLength));
		Class<? extends Enum> cls = (Class<? extends Enum>)context.loadClass(className);
    	
    	ensureAvailable(1);
    	String value = readString(buffer[position++] & 0xff);
    	
    	return Enum.valueOf(cls, value);
	}

	@Override
    public String readString(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return storedStrings.get(indexOrLength);
    	
    	String value = readStringData(indexOrLength);
    	if (value.length() > 0)
    		storedStrings.add(value);
    	return value;
    }
    
    private String readStringData(int length) throws IOException {
    	if (length == 0)
    		return "";
        
    	String result;
        if (length <= size - position) {
        	result = new String(buffer, position, length, "UTF-8");
        	position += length;
        }
        else if (length <= buffer.length) {
        	ensureAvailable(length);
        	result = new String(buffer, position, length, "UTF-8");
        	position += length;
        }
        else {
	        byte[] bytes = new byte[length];
	        readFully(bytes, 0, length);
	        result = new String(bytes, "UTF-8");
        }
        //storedStrings.add(result);
        return result;
    }
    
    private int readUnsignedIntegerValue(int length0) {
		int v = 0;
		
    	final byte[] buffer = this.buffer;
    	int position = this.position;

    	switch (length0) {
		case 3:
			v |= (buffer[position++] & 0xff) << 24;
		case 2:
			v |= (buffer[position++] & 0xff) << 16;
		case 1:
			v |= (buffer[position++] & 0xff) << 8;
		case 0:
			v |= (buffer[position++] & 0xff);
		}
		
    	this.position = position;
		
		return v;
    }
    
    @Override
    public long readIntegral(int parameterizedType) throws IOException {
    	final int length0 = (parameterizedType & 0x07);

    	ensureAvailable(length0 + 1);

    	final byte[] buffer = this.buffer;
    	int position = this.position;
    	
		long v = 0L;
		
		switch (length0) {
		case 7:
			v |= (buffer[position++] & 0xffL) << 56;
		case 6:
			v |= (buffer[position++] & 0xffL) << 48;
		case 5:
			v |= (buffer[position++] & 0xffL) << 40;
		case 4:
			v |= (buffer[position++] & 0xffL) << 32;
		case 3:
			v |= (buffer[position++] & 0xffL) << 24;
		case 2:
			v |= (buffer[position++] & 0xffL) << 16;
		case 1:
			v |= (buffer[position++] & 0xffL) << 8;
		case 0:
			v |= (buffer[position++] & 0xffL);
		}
		
		this.position = position;
		
		if ((parameterizedType & 0x08) != 0)
			v = -v;
		
		return v;
    }
    
    @Override
	public BigInteger readBigIntegral(int parameterizedType) throws IOException {
    	throw new UnsupportedOperationException("Not implemented");
	}

	@Override
    public double readFloating(int parameterizedType) throws IOException {
		if ((parameterizedType & 0x08) != 0) {
			int length0 = (parameterizedType & 0x03);
			
			ensureAvailable(length0 + 1);
	    	
			final byte[] buffer = this.buffer;
	    	int position = this.position;
			
			long doubleAsLong = 0L;
			
			switch (length0) {
			case 3:
				doubleAsLong |= (buffer[position++] & 0xffL) << 24;
			case 2:
				doubleAsLong |= (buffer[position++] & 0xffL) << 16;
			case 1:
				doubleAsLong |= (buffer[position++] & 0xffL) << 8;
			case 0:
				doubleAsLong |= (buffer[position++] & 0xffL);
			}
			
			this.position = position;
			
			if ((parameterizedType & 0x04) != 0)
				doubleAsLong = -doubleAsLong;
			
			return (doubleAsLong / 1000.0);
		}
		
    	ensureAvailable(8);
    	return Double.longBitsToDouble(readLongData());
    }

	@Override
	public BigDecimal readBigFloating(int parameterizedType) throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}
    
    @Override
    public Date readDate(int parameterizedType) throws IOException {
    	ensureAvailable(8);
    	return new Date(readLongData());
    }

    public Timestamp readTimestamp(int parameterizedType) throws IOException {
    	ensureAvailable(12);
    	Timestamp timestamp = new Timestamp(readLongData());
    	timestamp.setNanos(readIntData());
    	return timestamp;
    }
    
    @Override
	public byte[] readByteArray(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return (byte[])storedObjects.get(indexOrLength);
    	
    	byte[] bytes = new byte[indexOrLength];
    	readFully(bytes, 0, indexOrLength);
		return bytes;
	}

	@Override
	public Object readArray(int parameterizedType) throws IOException {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Collection<?> readCollection(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return (List<?>)storedObjects.get(indexOrLength);
    	
    	List<Object> value = new ArrayList<Object>(indexOrLength);
    	storedObjects.add(value);
    	
    	for (int i = 0; i < indexOrLength; i++)
    		value.add(readAny());
    	
    	return value;
	}
    
    @Override
	public void readCollection(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
    	
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference) {
    		property.setValue(holder, storedObjects.get(indexOrLength));
    		return;
    	}
    	
    	Collection<Object> value = property.getValue(holder);
    	if (value != null)
    		value.clear();
    	else
    		value = property.initValue(holder, context);
    	storedObjects.add(value);
    	
    	Type elementType = TypeUtil.getElementType(property.getGenericType());
    	if (elementType == Object.class) {
        	for (int i = 0; i < indexOrLength; i++)
        		value.add(readAny());
    	}
    	else if (elementType instanceof Class) {
    		Class<?> elementClass = (Class<?>)elementType;
	    	for (int i = 0; i < indexOrLength; i++) {
	    		Object element = readAny();
	    		if (element == null || elementClass.isAssignableFrom(element.getClass()))
	    			value.add(element);
	    		else
	    			value.add(context.convert(element, elementType));
	    	}
    	}
    	else {
	    	for (int i = 0; i < indexOrLength; i++)
	    		value.add(context.convert(readAny(), elementType));
    	}
	}

	@Override
	public Map<?, ?> readMap(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return (Map<?, ?>)storedObjects.get(indexOrLength);
    	
    	Map<Object, Object> value = new LinkedHashMap<Object, Object>(indexOrLength);
    	storedObjects.add(value);
    	
    	for (int i = 0; i < indexOrLength; i++) {
    		Object key = readAny();
    		Object val = readAny();
    		value.put(key, val);
    	}
    	
    	return value;
	}

	@Override
	public void readMap(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
    	
		boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference) {
    		property.setValue(holder, storedObjects.get(indexOrLength));
    		return;
    	}
    	
    	Map<Object, Object> value = property.getValue(holder);
    	if (value != null)
    		value.clear();
    	else
    		value = property.initValue(holder, context);
    	storedObjects.add(value);

    	Type[] keyValueTypes = TypeUtil.getKeyValueType(property.getGenericType());
    	Type keyType = keyValueTypes[0];
    	Type valType = keyValueTypes[1];
    	
    	if (keyType == Object.class && valType == Object.class) {
        	for (int i = 0; i < indexOrLength; i++) {
        		Object key = readAny();
        		Object val = readAny();
        		value.put(key, val);
        	}
    	}
    	else if (keyType instanceof Class && valType instanceof Class) {
    		Class<?> keyClass = (Class<?>)keyType;
    		Class<?> valClass = (Class<?>)valType;
        	
    		for (int i = 0; i < indexOrLength; i++) {
        		Object key = readAny();
        		if (key != null && !keyClass.isAssignableFrom(key.getClass()))
        			key = context.convert(key, keyType);
        		Object val = readAny();
        		if (val != null && !valClass.isAssignableFrom(val.getClass()))
        			val = context.convert(val, valType);
        		value.put(key, val);
        	}
    	}
    	else {
	    	for (int i = 0; i < indexOrLength; i++) {
	    		Object key = context.convert(readAny(), keyType);
	    		Object val = context.convert(readAny(), valType);
	    		value.put(key, val);
	    	}
    	}
	}

	private long readLongData() {
    	final byte[] buffer = this.buffer;
    	int position = this.position;
    	
		long v =
			(buffer[position++] & 0xffL) << 56 |
			(buffer[position++] & 0xffL) << 48 |
			(buffer[position++] & 0xffL) << 40 |
			(buffer[position++] & 0xffL) << 32 |
			(buffer[position++] & 0xffL) << 24 |
			(buffer[position++] & 0xffL) << 16 |
			(buffer[position++] & 0xffL) <<  8 |
			(buffer[position++] & 0xffL);

		this.position = position;
		
		return v;
    }

	private int readIntData() {
    	final byte[] buffer = this.buffer;
    	int position = this.position;
    	
		int v =
			(buffer[position++] & 0xff) << 24 |
			(buffer[position++] & 0xff) << 16 |
			(buffer[position++] & 0xff) <<  8 |
			(buffer[position++] & 0xff);

		this.position = position;
		
		return v;
    }

	private void readFully(byte[] b, int off, int len) throws IOException {
		if (b == null)
			throw new NullPointerException();
		if (off < 0 || len < 0 || len > b.length - off)
			throw new IndexOutOfBoundsException();
        if (len == 0)
        	return;

        final int left = size - position;
		if (len <= left) {
			System.arraycopy(buffer, position, b, off, len);
			position += len;
		}
		else {
			if (left > 0) {
				System.arraycopy(buffer, position, b, off, left);
				off += left;
				len -= left;
				position = size;
			}
			
			while (len > 0) {
				int count = in.read(b, off, len);
				if (count == len)
					return;
	            if (count <= 0)
	                throw new EOFException();
	            off += count;
	            len -= count;
			}
		}
	}
	
	private void ensureAvailable(int count) throws IOException {
		
		// assert(count > 0);

		if (size - position < count)
			fillBuffer(count);
	}

	private void fillBuffer(int count) throws IOException {
		final int left = size - position;
		
		// assert(left >= 0);
		// assert(count > 0);
		// assert(left < count);
		
		if (left > 0) {
			if (left + count > buffer.length)
				throw new IllegalArgumentException(Integer.toString(count));
			System.arraycopy(buffer, position, buffer, 0, left);
		}
		else if (eof) {
			position = size = 0;
			throw new EOFException();
		}
		
		position = 0;
		size = left;
		
		do {
			int read = in.read(buffer, size, buffer.length - size);
			if (read == -1) {
				eof = true;
				throw new EOFException();
			}
			size += read;
		}
		while (size < count);
	}
	
	private static class ClassDescriptor {
		
		public final Class<?> cls;
		public final Collection<Property> properties;
		public final boolean partial;

		public static ClassDescriptor forDescription(SpearalContext context, String description) {
			
			String[] classNames;
			String[] propertyNames;

			int iLastColon = description.lastIndexOf(':');
			if (iLastColon == -1) {
				classNames = new String[]{ context.getClassNameAlias(description) };
				propertyNames = new String[0];
			}
			else {
				classNames = description.substring(0, iLastColon).split(":");
				for (int i = 0; i < classNames.length; i++)
					classNames[i] = context.getClassNameAlias(classNames[i]);
				propertyNames = description.substring(iLastColon + 1).split(",");
			}
			
			Class<?> cls = context.loadClass(classNames);
			
			Collection<Property> properties = context.getProperties(cls);
			Property[] serializedProperties = new Property[propertyNames.length];
			for (int i = 0; i < propertyNames.length; i++) {
				String propertyName = propertyNames[i];
				for (Property property : properties) {
					if (propertyName.equals(property.getName())) {
						serializedProperties[i] = property;
						break;
					}
				}
			}
			
			boolean partial = serializedProperties.length < properties.size();
			
			return new ClassDescriptor(cls, new UnmodifiableArray<Property>(serializedProperties), partial);
		}
		
		public ClassDescriptor(Class<?> cls, Collection<Property> properties, boolean partial) {
			this.cls = cls;
			this.properties = properties;
			this.partial = partial;
		}
	}
}
