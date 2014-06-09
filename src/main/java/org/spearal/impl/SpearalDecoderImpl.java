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
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalPrinter;
import org.spearal.SpearalPrinter.StringData;
import org.spearal.configurable.PropertyFactory.Property;
import org.spearal.impl.util.StringCache;
import org.spearal.impl.util.StringCache.ValueProvider;
import org.spearal.impl.util.TypeUtil;
import org.spearal.impl.util.UnmodifiableArray;

/**
 * @author Franck WOLFF
 */
public class SpearalDecoderImpl implements ExtendedSpearalDecoder {

	private static final Charset UTF8 = Charset.forName("UTF-8");
	
	private final List<String> storedStrings;
	private final List<Object> storedObjects;
	private final StringCache<ClassDescriptor> descriptors;

    private final SpearalContext context;
	
    private final InputStream in;
	private final byte[] buffer;
	private int position;
	private int size;
	private boolean eof;

	public SpearalDecoderImpl(SpearalContext context, InputStream in) {
		this(context, in, 1024);
	}

	public SpearalDecoderImpl(final SpearalContext context, InputStream in, int capacity) {
        this.storedStrings = new ArrayList<String>(64);
        this.storedObjects = new ArrayList<Object>(64);
        this.descriptors = new StringCache<ClassDescriptor>(new ValueProvider<ClassDescriptor>() {
			@Override
			public ClassDescriptor createValue(String key) {
				return ClassDescriptor.forDescription(context, key);
			}
		});

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

	@Override
	public void skipAny()  throws IOException {
    	ensureAvailable(1);
        skipAny(buffer[position++] & 0xff);
	}
	
	@Override
	public void printAny(SpearalPrinter printer) throws IOException {
    	ensureAvailable(1);
        printAny(printer, buffer[position++] & 0xff);
	}

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
        switch (SpearalType.valueOf(parameterizedType)) {
        
        case NULL:
        case TRUE:
        case FALSE:
        	return;

        case DATE:
        	skipDate(parameterizedType);
        	return;
        case TIMESTAMP:
        	skipTimestamp(parameterizedType);
        	return;

        case INTEGRAL:
        	skipIntegral(parameterizedType);
        	return;
        case BIG_INTEGRAL:
        	skipBigIntegral(parameterizedType);
        	return;
        	
        case FLOATING:
        	skipFloating(parameterizedType);
        	return;
        case BIG_FLOATING:
        	skipBigFloating(parameterizedType);
        	return;
        	
        case STRING:
        	skipString(parameterizedType);
        	return;
        
        case BYTE_ARRAY:
        	skipByteArray(parameterizedType);
        	return;
            
        case COLLECTION:
        	skipCollection(parameterizedType);
        	return;
        case MAP:
        	skipMap(parameterizedType);
        	return;
            
        case ENUM:
        	skipEnum(parameterizedType);
        	return;
        case CLASS:
        	skipClass(parameterizedType);
        	return;
        case BEAN:
        	skipBean(parameterizedType);
        	return;
        }
        
        throw new RuntimeException("Unexpected type: " + parameterizedType);
	}

	public void printAny(SpearalPrinter printer, int parameterizedType) throws IOException {
        switch (SpearalType.valueOf(parameterizedType)) {
        
        case NULL:
        	printer.printNull();
        	return;

        case TRUE:
        	printer.printBoolean(true);
        	return;
        case FALSE:
        	printer.printBoolean(false);
        	return;

        case DATE:
        	printer.printDate(readDate(parameterizedType));
        	return;
        case TIMESTAMP:
        	printer.printTimestamp(readTimestamp(parameterizedType));
        	return;

        case INTEGRAL:
        	printer.printIntegral(readIntegral(parameterizedType));
        	return;
        case BIG_INTEGRAL:
        	printer.printBigIntegral(readBigIntegral(parameterizedType));
        	return;
        	
        case FLOATING:
        	printer.printFloating(readFloating(parameterizedType));
        	return;
        case BIG_FLOATING:
        	printer.printBigFloating(readBigFloating(parameterizedType));
        	return;
        	
        case STRING:
        	printString(printer, parameterizedType);
        	return;
        
        case BYTE_ARRAY:
        	printByteArray(printer, parameterizedType);
        	return;
            
        case COLLECTION:
        	printCollection(printer, parameterizedType);
        	return;
        case MAP:
        	printMap(printer, parameterizedType);
        	return;
            
        case ENUM:
        	printEnum(printer, parameterizedType);
        	return;
        case CLASS:
        	printClass(printer, parameterizedType);
        	return;
        case BEAN:
        	printBean(printer, parameterizedType);
        	return;
        }
        
        throw new RuntimeException("Unexpected type: " + parameterizedType);
	}
    
    @Override
    public Date readDate(int parameterizedType) throws IOException {
    	ensureAvailable(8);
    	return new Date(readLongData());
    }
    
    @Override
    public void skipDate(int parameterizedType) throws IOException {
    	ensureAvailable(8);
    	position += 8;
    }
    
    public void printDate(PrintStream out, int parameterizedType) throws IOException {
    	out.print(readDate(parameterizedType));
    }

    public Timestamp readTimestamp(int parameterizedType) throws IOException {
    	ensureAvailable(12);
    	Timestamp timestamp = new Timestamp(readLongData());
    	timestamp.setNanos(readIntData());
    	return timestamp;
    }
    
    @Override
    public void skipTimestamp(int parameterizedType) throws IOException {
    	ensureAvailable(12);
    	position += 12;
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
    public void skipIntegral(int parameterizedType) throws IOException {
    	final int length = (parameterizedType & 0x07) + 1;
    	ensureAvailable(length);
    	position += length;
    }
    
    @Override
	public BigInteger readBigIntegral(int parameterizedType) throws IOException {
    	final int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int length = readUnsignedIntegerValue(length0);
    	
    	byte[] bytes = new byte[length];
    	readFully(bytes, 0, length);
    	return new BigInteger(bytes);
	}

    @Override
    public void skipBigIntegral(int parameterizedType) throws IOException {
    	int length0 = (parameterizedType & 0x03);
    	ensureAvailable(length0 + 1);
    	skipFully(readUnsignedIntegerValue(length0));
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
    public void skipFloating(int parameterizedType) throws IOException {
		if ((parameterizedType & 0x08) != 0) {
			int length = (parameterizedType & 0x03) + 1;
			ensureAvailable(length);
			position += length;
		}
		else {
			ensureAvailable(8);
			position += 8;
		}
	}

	@Override
	public BigDecimal readBigFloating(int parameterizedType) throws IOException {
		int bytesLength0 = (parameterizedType & 0x03);
		
		ensureAvailable(bytesLength0 + 2);
		int length = readUnsignedIntegerValue(bytesLength0);
		
		byte[] bytes = new byte[length];
    	readFully(bytes, 0, length);
    	
    	ensureAvailable(1);
    	parameterizedType = (buffer[position++] & 0xff);
    	// assert (SpearalType.valueOf(parameterizedType) == SpearalType.INTEGRAL);
    	int scale = (int)readIntegral(parameterizedType);
    	
    	return new BigDecimal(new BigInteger(bytes), scale);
	}
	
	@Override
    public void skipBigFloating(int parameterizedType) throws IOException {
		int bytesLength0 = (parameterizedType & 0x03);
		
		ensureAvailable(bytesLength0 + 2);
		int length = readUnsignedIntegerValue(bytesLength0);
		
		skipFully(length);

		ensureAvailable(1);
    	parameterizedType = (buffer[position++] & 0xff);
    	readIntegral(parameterizedType);
	}

	@Override
    public String readString(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	return readStringData(indexOrLength, reference);
    }
	
	@Override
    public void skipString(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	skipStringData(indexOrLength, reference);
	}
	
	private void printString(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
		
    	printer.printString(getStringData(indexOrLength, reference));
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
    	storedObjects.add(bytes);
    	readFully(bytes, 0, indexOrLength);
		return bytes;
	}
    
    @Override
    public void skipByteArray(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (!reference) {
    		storedObjects.add(null);
    		skipFully(indexOrLength);
    	}
    }
    
    private void printByteArray(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);

    	if (reference)
    		printer.printByteArray((byte[])storedObjects.get(indexOrLength), indexOrLength, true);
    	else {
        	byte[] bytes = new byte[indexOrLength];
        	storedObjects.add(bytes);
        	readFully(bytes, 0, indexOrLength);
        	printer.printByteArray(bytes, storedObjects.size() - 1, false);
    	}
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
    public void skipCollection(int parameterizedType) throws IOException {
		boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (!reference) {
			storedObjects.add(null);
    		for (int i = 0; i < indexOrLength; i++)
    			skipAny();
    	}
	}
	
	private void printCollection(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		printer.printCollectionReference(indexOrLength);
    	else {
    		printer.printCollectionStart(storedObjects.size(), indexOrLength);
    		storedObjects.add(null);
    		
    		for (int i = 0; i < indexOrLength; i++)
    			printAny(printer);
    		
    		printer.printCollectionEnd();
    	}
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
	
	private void printMap(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		printer.printMapReference(indexOrLength);
    	else {
    		printer.printMapStart(storedObjects.size(), indexOrLength);
    		storedObjects.add(null);
    		
    		for (int i = 0; i < indexOrLength; i++) {
    			printAny(printer);
    			printAny(printer);
    		}
    		
    		printer.printMapEnd();
    	}
	}
	
	@Override
    public void skipMap(int parameterizedType) throws IOException {
		boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (!reference) {
			storedObjects.add(null);
    		for (int i = 0; i < indexOrLength; i++) {
    			skipAny();
    			skipAny();
    		}
    	}
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Enum<?> readEnum(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	String className = context.getClassNameAlias(readStringData(indexOrLength, reference));
		Class<? extends Enum> cls = (Class<? extends Enum>)context.loadClass(className);
    	
    	ensureAvailable(1);
    	String value = readString(buffer[position++] & 0xff);
    	
    	return Enum.valueOf(cls, value);
	}
	
    private void printEnum(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	StringData className = getStringData(indexOrLength, reference);
    	
    	ensureAvailable(1);
    	parameterizedType = (buffer[position++] & 0xff);
    	
    	reference = (parameterizedType & 0x04) != 0;
    	length0 = (parameterizedType & 0x03);

    	ensureAvailable(length0 + 1);
    	indexOrLength = readUnsignedIntegerValue(length0);
    	
    	StringData value = getStringData(indexOrLength, reference);

    	printer.printEnum(className, value);
	}
    
    @Override
    public void skipEnum(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x04) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	skipStringData(indexOrLength, reference);
    	
    	ensureAvailable(1);
    	skipString(buffer[position++] & 0xff);
    }

	@Override
	public Class<?> readClass(int parameterizedType) throws IOException {
		boolean reference = (parameterizedType & 0x04) != 0;
		int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	String className = readStringData(indexOrLength, reference);
		return context.loadClass(context.getClassNameAlias(className));
	}
	
	@Override
    public void skipClass(int parameterizedType) throws IOException {
		boolean reference = (parameterizedType & 0x04) != 0;
		int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	skipStringData(indexOrLength, reference);
	}
	
	private void printClass(SpearalPrinter printer, int parameterizedType) throws IOException {
		boolean reference = (parameterizedType & 0x04) != 0;
		int length0 = (parameterizedType & 0x03);
    	
		ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
		
		printer.printClass(getStringData(indexOrLength, reference));
	}

	@Override
    public Object readBean(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		return storedObjects.get(indexOrLength);
    	
    	boolean classDescReference = (parameterizedType & 0x04) != 0;
    	String classDescription = readStringData(indexOrLength, classDescReference);
    	ClassDescriptor descriptor = descriptors.putIfAbsent(classDescription);

    	try {
	    	Class<?> cls = descriptor.cls;

	    	Object value = (
	    		descriptor.partial
	    		? context.instantiatePartial(cls, descriptor.properties)
	    		: context.instantiate(cls)
	    	);
	    	storedObjects.add(value);
	    	
	    	for (Property property : descriptor.properties) {
	    		ensureAvailable(1);
	    		int propertyType = (buffer[position++] & 0xff);
	    		
	    		if (property != null)
	    			property.read(this, value, propertyType);
	    		else
	    			skipAny(propertyType);
	    	}
	    	
	    	return value;
    	}
    	catch (Exception e) {
    		throw new IOException(e);
    	}
    }
	
	@Override
    public void skipBean(int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (!reference) {
    		boolean classDescReference = (parameterizedType & 0x04) != 0;
    		String classDescription = readStringData(indexOrLength, classDescReference);
    		storedObjects.add(null);
    		
    		int propertiesCount = ClassDescriptor.propertiesCount(classDescription);
    		for (int i = 0; i < propertiesCount; i++)
    			skipAny();
    	}
	}
	
	private void printBean(SpearalPrinter printer, int parameterizedType) throws IOException {
    	boolean reference = (parameterizedType & 0x08) != 0;
    	int length0 = (parameterizedType & 0x03);
    	
    	ensureAvailable(length0 + 1);
    	int indexOrLength = readUnsignedIntegerValue(length0);
    	
    	if (reference)
    		printer.printBeanReference(indexOrLength);
    	else {
        	boolean classDescReference = (parameterizedType & 0x04) != 0;
        	StringData classDescription = getStringData(indexOrLength, classDescReference);
    		String[] classNames = ClassDescriptor.classNames(classDescription.value);
    		
    		storedObjects.add(null);
    		printer.printBeanStart(storedObjects.size() - 1, classDescription, classNames);
    		
    		String[] propertyNames = ClassDescriptor.propertyNames(classDescription.value);
    		for (String propertyName : propertyNames) {
    			printer.printBeanPropertyStart(propertyName);
    			printAny(printer);
    			printer.printBeanPropertyEnd();
    		}
    		
    		printer.printBeanEnd();
    	}
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
    
    private String readStringData(int indexOrLength, boolean reference) throws IOException {
    	if (reference)
    		return storedStrings.get(indexOrLength);
    	
    	if (indexOrLength == 0)
    		return "";
        
    	String value;
        if (indexOrLength <= buffer.length) {
        	ensureAvailable(indexOrLength);
        	value = new String(buffer, position, indexOrLength, UTF8);
        	position += indexOrLength;
        }
        else {
	        byte[] bytes = new byte[indexOrLength];
	        readFully(bytes, 0, indexOrLength);
	        value = new String(bytes, UTF8);
        }
        
    	storedStrings.add(value);
        
        return value;
    }
    
    private void skipStringData(int indexOrLength, boolean reference) throws IOException {
    	if (!reference) {
    		storedStrings.add(null);
    		skipFully(indexOrLength);
    	}
    }
    
    private StringData getStringData(int indexOrLength, boolean reference) throws IOException {
    	if (reference)
    		return new StringData(storedStrings.get(indexOrLength), indexOrLength, true);
    	if (indexOrLength == 0)
    		return new StringData("", -1, false);
    	String value = readStringData(indexOrLength, reference);
    	return new StringData(value, storedStrings.size() - 1, false);
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

	private void skipFully(int len) throws IOException {
		if (len < 0)
			throw new IndexOutOfBoundsException();
        if (len == 0)
        	return;

        final int left = size - position;
		if (len <= left)
			position += len;
		else {
			if (left > 0) {
				len -= left;
				position = size;
			}
			
			while (len > 0) {
				int count = (int)in.skip(len);
				if (count == len)
					return;
	            if (count <= 0)
	                throw new EOFException();
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
		
		public static String[] classNames(String description) {
			int iLastColon = description.lastIndexOf(':');
			if (iLastColon == -1)
				return new String[0];
			return description.substring(0, iLastColon).split(":");
		}
		
		public static String[] propertyNames(String description) {
			int iLastColon = description.lastIndexOf(':');
			if (iLastColon == -1)
				return new String[]{ description };
			return description.substring(iLastColon + 1).split(",");
		}
		
		public static int propertiesCount(String description) {
			int iLastColon = description.lastIndexOf(':');
			if (iLastColon == -1)
				return 0;
			
			int count = 1;
			for (int i = iLastColon + 1; i < description.length(); i++) {
				if (description.charAt(i) == ',')
					count++;
			}
			return count;
		}
		
		public ClassDescriptor(Class<?> cls, Collection<Property> properties, boolean partial) {
			this.cls = cls;
			this.properties = properties;
			this.partial = partial;
		}
	}
}
