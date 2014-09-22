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

import static org.spearal.impl.SharedConstants.BIG_NUMBER_ALPHA;
import static org.spearal.impl.SharedConstants.UTF8;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spearal.SpearalContext;
import org.spearal.SpearalPrinter;
import org.spearal.SpearalPrinter.StringData;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.cache.EqualityMap;
import org.spearal.impl.util.ClassDescriptionUtil;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class SpearalDecoderImpl implements ExtendedSpearalDecoder {

	private final List<String> sharedStrings;
	private final List<Object> sharedObjects;
	
	private final Path path;
	private final PartialObjectMap partialObjectsMap;
	
	private final EqualityMap<String, Type, ClassDescriptor> descriptors;
	private final EqualityMap<String, Object, BigInteger> bigIntegers;
	private final EqualityMap<String, Object, BigDecimal> bigDecimals;

	private final SpearalContext context;
	
	private final InputStream in;
	private final byte[] buffer;
	private int position;
	private int size;

	public SpearalDecoderImpl(SpearalContext context, InputStream in) {
		this(context, in, 1024);
	}

	public SpearalDecoderImpl(final SpearalContext context, InputStream in, int capacity) {
		this.sharedStrings = new ArrayList<String>(64);
		this.sharedObjects = new ArrayList<Object>(64);
		
		this.path = new Path();
		this.partialObjectsMap = new PartialObjectMap();
		
		this.descriptors = new EqualityMap<String, Type, ClassDescriptor>(new ValueProvider<String, Type, ClassDescriptor>() {
			@Override
			public ClassDescriptor createValue(SpearalContext context, String key, Type targetType) {
				return ClassDescriptor.forDescription(context, key, targetType);
			}
		});
		this.bigIntegers = new EqualityMap<String, Object, BigInteger>(new ValueProvider<String, Object, BigInteger>() {
			@Override
			public BigInteger createValue(SpearalContext context, String key, Object unsused) {
				final int exponentIndex = key.indexOf('E');

				BigInteger bigInteger;
				if (exponentIndex == -1)
					bigInteger = new BigInteger(key);
				else {
					bigInteger = new BigInteger(key.substring(0, exponentIndex));
					int exponent = Integer.parseInt(key.substring(exponentIndex + 1));
					bigInteger = bigInteger.multiply(BigInteger.TEN.pow(exponent));
				}
				return bigInteger;
			}
		});
		this.bigDecimals = new EqualityMap<String, Object, BigDecimal>(new ValueProvider<String, Object, BigDecimal>() {
			@Override
			public BigDecimal createValue(SpearalContext context, String key, Object unsused) {
				return new BigDecimal(key);
			}
		});

		this.context = context;
		this.in = in;
		this.buffer = new byte[capacity];
		this.position = 0;
		this.size = 0;
	}
	
	@Override
	public SpearalContext getContext() {
		return context;
	}

	@Override
	public Object readAny() throws IOException {
		return readAny(readNextByte(), null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T readAny(Type targetType) throws IOException {
		return (T)readAny(readNextByte(), targetType);
	}

	@Override
	public boolean containsPartialObjects() {
		return !partialObjectsMap.isEmpty();
	}

	@Override
	public Map<Object, List<PathSegment>> getPartialObjectsMap() {
		return partialObjectsMap;
	}

	@Override
	public void skipAny()  throws IOException {
		readAny();
	}
	
	@Override
	public void printAny(SpearalPrinter printer) throws IOException {
		printAny(printer, readNextByte());
	}

	@Override
	public Object readAny(int parameterizedType) throws IOException {
		return readAny(parameterizedType, null);
	}

	@Override
	public Object readAny(int parameterizedType, Type targetType) throws IOException {

		targetType = TypeUtil.unwrapTypeVariable(targetType);
		
		Object value;
		boolean convert;
		
		switch (SpearalType.valueOf(parameterizedType)) {
		
		case NULL:
			value = null;
			convert = ((targetType instanceof Class<?>) && ((Class<?>)targetType).isPrimitive());
			break;
		
		case TRUE:
			value = Boolean.TRUE;
			convert = (targetType != null && targetType != boolean.class && targetType != Boolean.class);
			break;

		case FALSE:
			value = Boolean.FALSE;
			convert = (targetType != null && targetType != boolean.class && targetType != Boolean.class);
			break;

		case INTEGRAL:
			value = Long.valueOf(readIntegral(parameterizedType));
			convert = (targetType != null && targetType != long.class && targetType != Long.class);
			break;

		case BIG_INTEGRAL:
			value = readBigIntegral(parameterizedType);
			convert = (targetType != null && targetType != BigInteger.class);
			break;
			
		case FLOATING:
			value = Double.valueOf(readFloating(parameterizedType));
			convert = (targetType != null && targetType != double.class && targetType != Double.class);
			break;
		
		case BIG_FLOATING:
			value = readBigFloating(parameterizedType);
			convert = (targetType != null && targetType != BigDecimal.class);
			break;
			
		case STRING:
			value = readString(parameterizedType);
			convert = (targetType != null && targetType != String.class);
			break;
		
		case BYTE_ARRAY:
			value = readByteArray(parameterizedType);
			convert = (targetType != null && targetType != byte[].class);
			break;

		case DATE_TIME:
			value = readDateTime(parameterizedType);
			if (targetType == null) {
				targetType = Object.class;
				convert = true;
			}
			else
				convert = (targetType != SpearalDateTime.class);
			break;
			
		case COLLECTION:
			value = readCollection(parameterizedType, targetType);
			convert = (targetType != null && !Collection.class.isAssignableFrom(TypeUtil.classOfType(targetType)));
			break;
			
		case MAP:
			value = readMap(parameterizedType, targetType);
			convert = (targetType != null && !Map.class.isAssignableFrom(TypeUtil.classOfType(targetType)));
			break;
			
		case ENUM:
			value = readEnum(parameterizedType, targetType);
			convert = (targetType != null && targetType != value.getClass());
			break;
			
		case CLASS:
			value = readClass(parameterizedType, targetType);
			convert = (targetType != null && targetType != Class.class);
			break;
			
		case BEAN:
			value = readBean(parameterizedType, targetType);
			convert = (
				targetType != null &&
				targetType != value.getClass() &&
				!((targetType instanceof Class) && ((Class<?>)targetType).isAssignableFrom(value.getClass()))
			);
			break;
		
		default:
			throw new RuntimeException("Unexpected parameterized type: " + parameterizedType);
		}
		
		return (convert ? context.convert(value, targetType) : value);
	}

	@Override
	public void skipAny(int parameterizedType) throws IOException {
		readAny(parameterizedType);
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

		case INTEGRAL:
			printer.printIntegral(readIntegral(parameterizedType));
			return;
		case BIG_INTEGRAL:
			printBigIntegral(printer, parameterizedType);
			return;
			
		case FLOATING:
			printer.printFloating(readFloating(parameterizedType));
			return;
		case BIG_FLOATING:
			printBigFloating(printer, parameterizedType);
			return;
			
		case STRING:
			printString(printer, parameterizedType);
			return;
		
		case BYTE_ARRAY:
			printByteArray(printer, parameterizedType);
			return;

		case DATE_TIME:
			printDateTime(printer, parameterizedType);
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
		final int indexOrLength = readIndexOrLength(parameterizedType);
		String representation;
		if (isStringReference(parameterizedType))
			representation = sharedStrings.get(indexOrLength);
		else
			representation = readBigNumberData(indexOrLength);
		return bigIntegers.putIfAbsent(context, representation);
	}
	
	void printBigIntegral(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		if (isStringReference(parameterizedType))
			printer.printBigIntegral(new StringData(sharedStrings.get(indexOrLength), indexOrLength, true));
		else
			printer.printBigIntegral(new StringData(readBigNumberData(indexOrLength), sharedStrings.size() - 1, false));
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
		final int indexOrLength = readIndexOrLength(parameterizedType);
		String representation;
		if (isStringReference(parameterizedType))
			representation = sharedStrings.get(indexOrLength);
		else
			representation = readBigNumberData(indexOrLength);
		return bigDecimals.putIfAbsent(context, representation);
	}
	
	void printBigFloating(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		if (isStringReference(parameterizedType))
			printer.printBigFloating(new StringData(sharedStrings.get(indexOrLength), indexOrLength, true));
		else
			printer.printBigFloating(new StringData(readBigNumberData(indexOrLength), sharedStrings.size() - 1, false));
	}

	@Override
	public String readString(int parameterizedType) throws IOException {
		return readStringData(parameterizedType);
	}
	
	private void printString(SpearalPrinter printer, int parameterizedType) throws IOException {
		printer.printString(getStringData(parameterizedType));
	}
	
	@Override
	public byte[] readByteArray(int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);

		if (isObjectReference(parameterizedType))
			return (byte[])sharedObjects.get(indexOrLength);
		
		byte[] bytes = new byte[indexOrLength];
		sharedObjects.add(bytes);
		readFully(bytes, 0, indexOrLength);
		return bytes;
	}
	
	private void printByteArray(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);

		if (isObjectReference(parameterizedType))
			printer.printByteArray((byte[])sharedObjects.get(indexOrLength), indexOrLength, true);
		else {
			byte[] bytes = new byte[indexOrLength];
			sharedObjects.add(bytes);
			readFully(bytes, 0, indexOrLength);
			printer.printByteArray(bytes, sharedObjects.size() - 1, false);
		}
	}
	
	@Override
	public SpearalDateTime readDateTime(int parameterizedType) throws IOException {

		boolean hasDate = ((parameterizedType & 0x08) != 0);
		boolean hasTime = ((parameterizedType & 0x04) != 0);
		
		int year = 0;
		int month = 0;
		int date = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		int nanoseconds = 0;
		
		if (hasDate) {
			ensureAvailable(2);
			month = (buffer[position++] & 0xff);
			date = (buffer[position++] & 0xff);
			
			int length0 = ((month >>> 4) & 0x03);
			boolean inverse = ((month & 0x80) != 0);
			
			month &= 0x0f;
			
			ensureAvailable(length0 + 1);
			year = readUnsignedIntegerValue(length0);
			if (inverse)
				year = -year;
			year += 2000;
		}
		
		if (hasTime) {
			ensureAvailable(3);
			hours = (buffer[position++] & 0xff);
			minutes = (buffer[position++] & 0xff);
			seconds = (buffer[position++] & 0xff);
			
			int subsecondsType = (parameterizedType & 0x03);
			if (subsecondsType != 0) {
				int length0 = (hours >>> 5);
				ensureAvailable(length0 + 1);
				nanoseconds = readUnsignedIntegerValue(length0);
				
				if (subsecondsType == 2)
					nanoseconds *= 1000;
				else if (subsecondsType == 3)
					nanoseconds *= 1000000;
			}
			
			hours &= 0x1f;
		}
		
		return new SpearalDateTime(year, month, date, hours, minutes, seconds, nanoseconds, hasDate, hasTime);
	}
	
	private void printDateTime(SpearalPrinter printer, int parameterizedType) throws IOException {
		printer.printDateTime(readDateTime(parameterizedType));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Collection<?> readCollection(int parameterizedType, Type targetType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);

		if (isObjectReference(parameterizedType))
			return (List<?>)sharedObjects.get(indexOrLength);
		
		Collection<Object> value = null;
		Type elementType = null;	
		if (targetType != null && Collection.class.isAssignableFrom(TypeUtil.classOfType(targetType))) {		
			try {
				value = (Collection<Object>)context.instantiate(targetType);
			} 
			catch (Exception e) {
				throw new RuntimeException("Couldn't instantiate type: " + targetType, e);
			}
			elementType = TypeUtil.getElementType(targetType);
		}
		else
			value = new ArrayList<Object>(indexOrLength);
		
		sharedObjects.add(value);

		CollectionPathSegmentImpl segment = new CollectionPathSegmentImpl(value);
		path.push(segment);
		for (segment.index = 0; segment.index < indexOrLength; segment.index++)
			value.add(readAny(elementType));
		path.pop();

		return value;
	}
	
	private void printCollection(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType))
			printer.printCollectionReference(indexOrLength);
		else {
			int index = sharedObjects.size();
			printer.printCollectionStart(index, indexOrLength);
			sharedObjects.add(null);
			
			for (int i = 0; i < indexOrLength; i++) {
				printer.printCollectionItemStart(index, i);
				printAny(printer);
				printer.printCollectionItemEnd(index, i);
			}
			
			printer.printCollectionEnd(index);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readCollection(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType)) {
			property.set(holder, sharedObjects.get(indexOrLength));
			return;
		}
		
		Collection<Object> value = (Collection<Object>)property.get(holder);
		if (value != null)
			value.clear();
		else
			value = (Collection<Object>)property.init(this, holder);
		sharedObjects.add(value);
		
		Type elementType = TypeUtil.getElementType(property.getGenericType());
		
		CollectionPathSegmentImpl segment = new CollectionPathSegmentImpl(value);
		path.push(segment);
		for (segment.index = 0; segment.index < indexOrLength; segment.index++)
			value.add(readAny(elementType));
		path.pop();
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<?, ?> readMap(int parameterizedType, Type targetType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType))
			return (Map<?, ?>)sharedObjects.get(indexOrLength);
		
		Map<Object, Object> value = null;
		Type keyType = null;
		Type valType = null;
		if (targetType != null && Map.class.isAssignableFrom(TypeUtil.classOfType(targetType))) {		
			try {
				value = (Map<Object, Object>)context.instantiate(targetType);
			} 
			catch (Exception e) {
				throw new RuntimeException("Couldn't instantiate type: " + targetType, e);
			}
			Type[] keyValueTypes = TypeUtil.getKeyValueType(targetType);
			keyType = keyValueTypes[0];
			valType = keyValueTypes[1];
		}
		else
			value = new LinkedHashMap<Object, Object>(indexOrLength);
		
		sharedObjects.add(value);
		
		MapPathSegmentImpl segment = new MapPathSegmentImpl(value);
		path.push(segment);
		for (int i = 0; i < indexOrLength; i++) {
			segment.key = null;
			Object key = readAny(keyType);
			segment.key = key;
			Object val = readAny(valType);
			value.put(key, val);
		}
		path.pop();
		
		return value;
	}
	
	private void printMap(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType))
			printer.printMapReference(indexOrLength);
		else {
			int index = sharedObjects.size();
			printer.printMapStart(index, indexOrLength);
			sharedObjects.add(null);
			
			for (int i = 0; i < indexOrLength; i++) {
				printer.printMapKeyStart(index, i);
				printAny(printer);
				printer.printMapKeyEnd(index, i);
				
				printer.printMapValueStart(index, i);
				printAny(printer);
				printer.printMapValueEnd(index, i);
			}
			
			printer.printMapEnd(index);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readMap(int parameterizedType, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType)) {
			property.set(holder, sharedObjects.get(indexOrLength));
			return;
		}
		
		Map<Object, Object> value = (Map<Object, Object>)property.get(holder);
		if (value != null)
			value.clear();
		else
			value = (Map<Object, Object>)property.init(this, holder);
		sharedObjects.add(value);

		Type[] keyValueTypes = TypeUtil.getKeyValueType(property.getGenericType());
		Type keyType = keyValueTypes[0];
		Type valType = keyValueTypes[1];
		
		MapPathSegmentImpl segment = new MapPathSegmentImpl(value);
		path.push(segment);
		for (int i = 0; i < indexOrLength; i++) {
			segment.key = null;
			Object key = readAny(keyType);
			segment.key = key;
			Object val = readAny(valType);
			value.put(key, val);
		}
		path.pop();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Enum<?> readEnum(int parameterizedType, Type targetType) throws IOException {
		String className = readStringData(parameterizedType);
		Class<? extends Enum> cls = (Class<? extends Enum>)context.loadClass(className, targetType);
		String value = readString(readNextByte());
		return Enum.valueOf(cls, value);
	}
	
	private void printEnum(SpearalPrinter printer, int parameterizedType) throws IOException {
		StringData className = getStringData(parameterizedType);
		parameterizedType = readNextByte();
		StringData value = getStringData(parameterizedType);
		printer.printEnum(className, value);
	}

	@Override
	public Class<?> readClass(int parameterizedType, Type targetType) throws IOException {
		String className = readStringData(parameterizedType);
		return context.loadClass(className, targetType);
	}
	
	private void printClass(SpearalPrinter printer, int parameterizedType) throws IOException {
		printer.printClass(getStringData(parameterizedType));
	}

	@Override
	public Object readBean(int parameterizedType, Type targetType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType)) {
			Object value = sharedObjects.get(indexOrLength);
			partialObjectsMap.appendIfPresent(value, path.peek());
			return value;
		}
		
		String classDescription = readStringData(parameterizedType, indexOrLength);
		ClassDescriptor descriptor = descriptors.putIfAbsent(context, classDescription, targetType);

		try {
			Class<?> cls = descriptor.cls;
			
			Object value;
			if (cls == ClassNotFound.class)
				value = new ClassNotFound(classDescription);
			else if (!descriptor.partial)
				value = context.instantiate(cls);
			else {
				value = context.instantiatePartial(cls, descriptor.properties);
				partialObjectsMap.put(value, path.peek());
			}
			sharedObjects.add(value);
			
			BeanPathSegmentImpl segment = new BeanPathSegmentImpl(value);
			path.push(segment);
			for (Property property : descriptor.properties) {
				segment.property = property;
				int propertyType = readNextByte();
				if (property != null)
					property.read(this, value, propertyType);
				else
					skipAny(propertyType);
			}
			path.pop();
			
			return value;
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	private void printBean(SpearalPrinter printer, int parameterizedType) throws IOException {
		final int indexOrLength = readIndexOrLength(parameterizedType);
		
		if (isObjectReference(parameterizedType))
			printer.printBeanReference(indexOrLength);
		else {
			StringData classDescription = getStringData(parameterizedType, indexOrLength);
			String[] classNames = ClassDescriptionUtil.splitClassNames(classDescription.value);
			
			final int index = sharedObjects.size();
			sharedObjects.add(null);
			printer.printBeanStart(index, classDescription, classNames);
			
			String[] propertyNames = ClassDescriptionUtil.splitPropertyNames(classDescription.value);
			int propertyIndex = 0;
			for (String propertyName : propertyNames) {
				printer.printBeanPropertyStart(index, propertyName, propertyIndex);
				printAny(printer);
				printer.printBeanPropertyEnd(index, propertyIndex++);
			}
			
			printer.printBeanEnd(index);
		}
	}
	
	private int readNextByte() throws IOException {
		ensureAvailable(1);
		return (buffer[position++] & 0xff);
	}
	
	private int readIndexOrLength(int parameterizedType) throws IOException {
		int length0 = (parameterizedType & 0x03);
		ensureAvailable(length0 + 1);
		return readUnsignedIntegerValue(length0);
	}
	
	private int readUnsignedIntegerValue(int length0) {
		int v = 0;

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
		
		return v;
	}
	
	private String readStringData(int parameterizedType) throws IOException {
		int indexOrLength = readIndexOrLength(parameterizedType);
		return readStringData(parameterizedType, indexOrLength);
	}
	
	private String readStringData(int parameterizedType, int indexOrLength) throws IOException {
		if (isStringReference(parameterizedType))
			return sharedStrings.get(indexOrLength);
		
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
		
		sharedStrings.add(value);
		
		return value;
	}
	
	private StringData getStringData(int parameterizedType) throws IOException {
		int indexOrLength = readIndexOrLength(parameterizedType);
		return getStringData(parameterizedType, indexOrLength);
	}
	
	private StringData getStringData(int parameterizedType, int indexOrLength) throws IOException {
		if (isStringReference(parameterizedType))
			return new StringData(sharedStrings.get(indexOrLength), indexOrLength, true);
		
		if (indexOrLength == 0)
			return new StringData("", -1, false);
		
		String value = readStringData(parameterizedType, indexOrLength);
		return new StringData(value, sharedStrings.size() - 1, false);
	}
	
	private String readBigNumberData(int length) throws IOException {
		char[] chars = new char[length];
		
		final byte[] buffer = this.buffer;
		final int bufferLength = buffer.length;
		int position = this.position;
		int size = this.size;
		
		final int count = (length / 2) + (length % 2);
		int iChar = 0;
		for (int i = 0; i < count; i++) {
			if (position == size) {
				this.position = position;
				fillBuffer(Math.min(count - i, bufferLength));
				position = 0;
				size = this.size;
			}
			int b = (buffer[position++] & 0xff);
			chars[iChar++] = BIG_NUMBER_ALPHA[(b & 0xf0) >>> 4];
			if (iChar == length)
				break;
			chars[iChar++] = BIG_NUMBER_ALPHA[b & 0x0f];
		}
		this.position = position;
		
		String representation = new String(chars);
		sharedStrings.add(representation);
		return representation;
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
		if (size - position < count)
			fillBuffer(count);
	}

	private void fillBuffer(int count) throws IOException {
		if (position > 0) {
			size -= position;
			System.arraycopy(buffer, position, buffer, 0, size);
			position = 0;
		}
		
		do {
			int read = in.read(buffer, size, buffer.length - size);
			if (read == -1)
				throw new EOFException();
			size += read;
		}
		while (size < count);
	}
	
	private static boolean isObjectReference(int parameterizedType) {
		return ((parameterizedType & 0x08) != 0);
	}
	
	private static boolean isStringReference(int parameterizedType) {
		return ((parameterizedType & 0x04) != 0);
	}
	
	private static class ClassDescriptor {
		
		public final Class<?> cls;
		public final Property[] properties;
		public final boolean partial;

		public static ClassDescriptor forDescription(SpearalContext context, String description, Type targetType) {
			String classNames = ClassDescriptionUtil.classNames(description);
			String[] propertyNames = ClassDescriptionUtil.splitPropertyNames(description);

			Class<?> cls = context.loadClass(classNames, targetType);
			
			if (cls == null) {
				Property[] classNotFoundProperties = new Property[propertyNames.length];
				for (int i = 0; i < propertyNames.length; i++) {
					String propertyName = propertyNames[i];
					classNotFoundProperties[i] = new ClassNotFoundProperty(propertyName);
				}
				return new ClassDescriptor(ClassNotFound.class, classNotFoundProperties, false);
			}

			
			Property[] properties = context.getProperties(cls);

			Property[] serializedProperties = new Property[propertyNames.length];
			boolean partial = false;
			
			propertiesLoop:
			for (Property property : properties) {
				String propertyName = property.getName();
				for (int i = 0; i < propertyNames.length; i++) {
					if (propertyName.equals(propertyNames[i])) {
						serializedProperties[i] = property;
						continue propertiesLoop;
					}
				}
				partial = true;
			}
			
			return new ClassDescriptor(cls, serializedProperties, partial);
		}
		
		public ClassDescriptor(Class<?> cls, Property[] properties, boolean partial) {
			this.cls = cls;
			this.properties = properties;
			this.partial = partial;
		}
	}
	
	public static class ClassNotFound extends HashMap<String, Object> {

		private static final long serialVersionUID = 1L;

		private final String classNotFoundDescription;
		
		public ClassNotFound(String classNotFoundDescription) {
			this.classNotFoundDescription = classNotFoundDescription;
		}

		public String getClassNotFoundDescription() {
			return classNotFoundDescription;
		}
	}
	
	private static class ClassNotFoundProperty implements Property {

		private final String name;
		
		public ClassNotFoundProperty(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return Object.class;
		}

		@Override
		public Type getGenericType() {
			return Object.class;
		}

		@Override
		public Field getField() {
			return null;
		}

		@Override
		public Method getGetter() {
			return null;
		}

		@Override
		public Method getSetter() {
			return null;
		}

		@Override
		public Class<?> getDeclaringClass() {
			return ClassNotFound.class;
		}

		@Override
		public Object init(ExtendedSpearalDecoder decoder, Object holder)
			throws InstantiationException, IllegalAccessException, InvocationTargetException {
			return null;
		}

		@Override
		public Object get(Object holder)
			throws IllegalAccessException, InvocationTargetException {
			return ((ClassNotFound)holder).get(name);
		}

		@Override
		public void set(Object holder, Object value)
			throws IllegalAccessException, InvocationTargetException {
			((ClassNotFound)holder).put(name, value);
		}

		@Override
		public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
			return false;
		}

		@Override
		public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
			return null;
		}

		@Override
		public boolean isReadOnly() {
			return false;
		}

		@Override
		public void write(ExtendedSpearalEncoder encoder, Object holder)
			throws IOException, IllegalAccessException, InvocationTargetException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void read(ExtendedSpearalDecoder decoder, Object holder, int parameterizedType)
			throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException {
			
			set(holder, decoder.readAny(parameterizedType));
		}
	}
	
	public static class CollectionPathSegmentImpl implements CollectionPathSegment {
		
		private final Collection<?> collection;
		private int index;
		
		public CollectionPathSegmentImpl(Collection<?> collection) {
			this.collection = collection;
		}

		public Collection<?> getCollection() {
			return collection;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public CollectionPathSegment copy() {
			CollectionPathSegmentImpl copy = new CollectionPathSegmentImpl(collection);
			copy.index = index;
			return copy;
		}

		@Override
		public String toString() {
			return "collection: " + index;
		}
	}
	
	public static class MapPathSegmentImpl implements MapPathSegment {
		
		private final Map<?, ?> map;
		private Object key;

		public MapPathSegmentImpl(Map<?, ?> map) {
			this.map = map;
		}

		public Map<?, ?> getMap() {
			return map;
		}

		public Object getKey() {
			return key;
		}

		@Override
		public MapPathSegment copy() {
			MapPathSegmentImpl copy = new MapPathSegmentImpl(map);
			copy.key = key;
			return copy;
		}

		@Override
		public String toString() {
			return "map: " + key;
		}
	}
	
	public static class BeanPathSegmentImpl implements BeanPathSegment {
		
		private final Object bean;
		private Property property;

		public BeanPathSegmentImpl(Object bean) {
			this.bean = bean;
		}

		public Object getBean() {
			return bean;
		}

		public Property getProperty() {
			return property;
		}

		@Override
		public BeanPathSegment copy() {
			BeanPathSegmentImpl copy = new BeanPathSegmentImpl(bean);
			copy.property = property;
			return copy;
		}

		@Override
		public String toString() {
			return "bean: " + property;
		}
	}
	
	private static class Path {
		
		private static final int DEFAULT_SIZE_INCREMENT = 8;
		
		private PathSegment[] segments;
		private int size; 
		
		public Path() {
			this.segments = new PathSegment[0];
			this.size = 0;
		}

		public void push(PathSegment segment) {
			if (size == segments.length) {
				PathSegment[] newSegments = new PathSegment[segments.length + DEFAULT_SIZE_INCREMENT];
				if (segments.length > 0)
					System.arraycopy(segments, 0, newSegments, 0, segments.length);
				segments = newSegments;
			}
			segments[size++] = segment;
		}
		
		public void pop() {
			if (size == 0)
				throw new IndexOutOfBoundsException("Empty path");
			size--;
		}
		
		public PathSegment peek() {
			return (size == 0 ? null : segments[size - 1]);
		}
	}
	
	private static class PartialObjectMap extends IdentityHashMap<Object, List<PathSegment>> {

		private static final long serialVersionUID = 1L;
		
		public void put(Object partialObject, PathSegment segment) {
			List<PathSegment> segments = new ArrayList<PathSegment>();
			if (segment != null)
				segments.add(segment.copy());
			put(partialObject, segments);
		}
		
		public void appendIfPresent(Object partialObject, PathSegment segment) {
			if (segment != null) {
				List<PathSegment> segments = get(partialObject);
				if (segments != null)
					segments.add(segment.copy());
			}
		}
	}
}
