package org.spearal.impl.converter;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.spearal.configurable.ConverterProvider;
import org.spearal.impl.ExtendedSpearalDecoder;
import org.spearal.impl.SpearalDateTime;

public class SimpleConvertersProvider implements ConverterProvider {
	
	private final Map<Class<?>, Converter<?>> converters;

	public SimpleConvertersProvider() {
		this.converters = new HashMap<Class<?>, Converter<?>>();
		
		// Boolean
		
		Converter<?> converter = new Converter<Boolean>() {
			@Override
			public Boolean convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == boolean.class ? Boolean.FALSE : null);

				Class<?> cls = value.getClass();
				
				if (cls == Boolean.class)
					return (Boolean)value;
				if (cls == String.class)
					return Boolean.valueOf(Boolean.parseBoolean((String)value));
				if (Number.class.isAssignableFrom(cls))
					return Boolean.valueOf(((Number)value).intValue() != 0);
				if (cls == Character.class)
					return Boolean.valueOf(((Character)value).charValue() != '\0');

				return Boolean.TRUE;
			}
		};
		converters.put(Boolean.class, converter);
		converters.put(boolean.class, converter);
		
		// Number
		
		converter = new Converter<Byte>() {
			@Override
			public Byte convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == byte.class ? Byte.valueOf((byte)0) : null);

				Class<?> cls = value.getClass();
				
				if (cls == Byte.class)
					return (Byte)value;
				if (Number.class.isAssignableFrom(cls))
					return Byte.valueOf(((Number)value).byteValue());
				if (cls == String.class) {
					try {
						return Byte.valueOf(Byte.parseByte((String)value));
					}
					catch (NumberFormatException e) {
						return Byte.valueOf((byte)0);
					}
				}
				if (cls == Boolean.class)
					return Byte.valueOf((byte)(((Boolean)value).booleanValue() ? 1 : 0));
				if (cls == Character.class)
					return Byte.valueOf((byte)((Character)value).charValue());

				return Byte.valueOf((byte)1);
			}
		};
		converters.put(Byte.class, converter);
		converters.put(byte.class, converter);
		
		converter = new Converter<Short>() {
			@Override
			public Short convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == int.class ? Short.valueOf((short)0) : null);

				Class<?> cls = value.getClass();
				
				if (cls == Short.class)
					return (Short)value;
				if (Number.class.isAssignableFrom(cls))
					return Short.valueOf(((Number)value).shortValue());
				if (cls == String.class) {
					try {
						return Short.valueOf(Short.parseShort((String)value));
					}
					catch (NumberFormatException e) {
						return Short.valueOf((short)0);
					}
				}
				if (cls == Character.class)
					return Short.valueOf((short)((Character)value).charValue());
				if (cls == Boolean.class)
					return Short.valueOf((short)(((Boolean)value).booleanValue() ? 1 : 0));

				return Short.valueOf((short)1);
			}
		};
		converters.put(Short.class, converter);
		converters.put(short.class, converter);
		
		converter = new Converter<Integer>() {
			@Override
			public Integer convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == int.class ? Integer.valueOf(0) : null);

				Class<?> cls = value.getClass();
				
				if (cls == Integer.class)
					return (Integer)value;
				if (Number.class.isAssignableFrom(cls))
					return Integer.valueOf(((Number)value).intValue());
				if (cls == String.class) {
					try {
						return Integer.valueOf(Integer.parseInt((String)value));
					}
					catch (NumberFormatException e) {
						return Integer.valueOf(0);
					}
				}
				if (cls == Character.class)
					return Integer.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return Integer.valueOf(((Boolean)value).booleanValue() ? 1 : 0);

				return Integer.valueOf(1);
			}
		};
		converters.put(Integer.class, converter);
		converters.put(int.class, converter);
		
		converter = new Converter<Long>() {
			@Override
			public Long convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == long.class ? Long.valueOf(0L) : null);
				
				Class<?> cls = value.getClass();
				
				if (cls == Long.class)
					return (Long)value;
				if (Number.class.isAssignableFrom(cls))
					return Long.valueOf(((Number)value).longValue());
				if (cls == String.class) {
					try {
						return Long.valueOf(Long.parseLong((String)value));
					}
					catch (NumberFormatException e) {
						return Long.valueOf(0);
					}
				}
				if (cls == Character.class)
					return Long.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return Long.valueOf(((Boolean)value).booleanValue() ? 1L : 0L);

				return Long.valueOf(1L);
			}
		};
		converters.put(Long.class, converter);
		converters.put(long.class, converter);
		
		converter = new Converter<BigInteger>() {
			@Override
			public BigInteger convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == BigInteger.class)
					return (BigInteger)value;
				if (Number.class.isAssignableFrom(cls))
					return BigInteger.valueOf(((Number)value).longValue());
				if (cls == String.class) {
					try {
						return new BigInteger((String)value);
					}
					catch (NumberFormatException e) {
						return BigInteger.ZERO;
					}
				}
				if (cls == Character.class)
					return BigInteger.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return (((Boolean)value).booleanValue() ? BigInteger.ONE : BigInteger.ZERO);

				return BigInteger.ONE;
			}
		};
		converters.put(BigInteger.class, converter);
		
		
		final Float FLOAT_ZERO = Float.valueOf(0.0f);
		final Float FLOAT_ONE = Float.valueOf(1.0f);
		
		converter = new Converter<Float>() {
			
			@Override
			public Float convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == float.class ? Float.valueOf(0.0f) : null);
				
				Class<?> cls = value.getClass();
				
				if (cls == Float.class)
					return (Float)value;
				if (Number.class.isAssignableFrom(cls))
					return Float.valueOf(((Number)value).floatValue());
				if (cls == String.class) {
					try {
						return Float.valueOf(Float.parseFloat((String)value));
					}
					catch (NumberFormatException e) {
						return FLOAT_ZERO;
					}
				}
				if (cls == Character.class)
					return Float.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return (((Boolean)value).booleanValue() ? FLOAT_ONE : FLOAT_ZERO);

				return FLOAT_ONE;
			}
		};
		converters.put(Float.class, converter);
		converters.put(float.class, converter);
		
		final Double DOUBLE_ZERO = Double.valueOf(0.0);
		final Double DOUBLE_ONE = Double.valueOf(1.0);

		converter = new Converter<Double>() {
			@Override
			public Double convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return (targetType == double.class ? Double.valueOf(0.0) : null);
				
				Class<?> cls = value.getClass();
				
				if (cls == Double.class)
					return (Double)value;
				if (Number.class.isAssignableFrom(cls))
					return Double.valueOf(((Number)value).doubleValue());
				if (cls == String.class) {
					try {
						return Double.valueOf(Double.parseDouble((String)value));
					}
					catch (NumberFormatException e) {
						return DOUBLE_ZERO;
					}
				}
				if (cls == Character.class)
					return Double.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return (((Boolean)value).booleanValue() ? DOUBLE_ONE : DOUBLE_ZERO);

				return DOUBLE_ONE;
			}
		};
		converters.put(Double.class, converter);
		converters.put(double.class, converter);
		
		converter = new Converter<BigDecimal>() {
			@Override
			public BigDecimal convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == BigDecimal.class)
					return (BigDecimal)value;
				if (Number.class.isAssignableFrom(cls))
					return BigDecimal.valueOf(((Number)value).doubleValue());
				if (cls == String.class) {
					try {
						return new BigDecimal((String)value);
					}
					catch (NumberFormatException e) {
						return BigDecimal.ZERO;
					}
				}
				if (cls == Character.class)
					return BigDecimal.valueOf(((Character)value).charValue());
				if (cls == Boolean.class)
					return (((Boolean)value).booleanValue() ? BigDecimal.ONE : BigDecimal.ZERO);

				return BigDecimal.ONE;
			}
		};
		converters.put(BigDecimal.class, converter);
		
		// String / Character
		
		converter = new Converter<String>() {
			@Override
			public String convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == String.class)
					return (String)value;
				if (cls == char[].class)
					return String.valueOf((char[])value);
				if (cls == Character[].class) {
					Character[] characters = (Character[])value;
					StringBuilder sb = new StringBuilder(characters.length);
					for (Character c : characters)
						sb.append((c != null ? c.charValue() : '\0'));
					return sb.toString();
				}
				
				return value.toString();
			}
		};
		converters.put(String.class, converter);
		
		converter = new Converter<Character>() {
			@Override
			public Character convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				if (cls == Character.class)
					return (Character)value;
				if (cls == char[].class) {
					char[] chars = (char[])value;
					return (chars.length > 0 ? Character.valueOf(chars[0]) : null);
				}
				if (cls == Character[].class) {
					Character[] characters = (Character[])value;
					return (characters.length > 0 ? characters[0] : null);
				}
				
				String string = value.toString();
				return (string.length() > 0 ? Character.valueOf(string.charAt(0)) : null);
			}
		};
		converters.put(Character.class, converter);
		
		converter = new Converter<Character>() {
			@Override
			public Character convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return Character.valueOf('\0');
				
				Class<?> cls = value.getClass();
				if (cls == Character.class)
					return (Character)value;
				if (cls == char[].class) {
					char[] chars = (char[])value;
					return Character.valueOf(chars.length > 0 ? chars[0] : '\0');
				}
				if (cls == Character[].class) {
					Character[] characters = (Character[])value;
					return (characters.length > 0 && characters[0] != null ? characters[0] : Character.valueOf('\0'));
				}
				
				String string = value.toString();
				return (string.length() > 0 ? Character.valueOf(string.charAt(0)) : Character.valueOf('\0'));
			}
		};
		converters.put(char.class, converter);
		
		converter = new Converter<Character[]>() {
			@Override
			public Character[] convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == Character[].class)
					return (Character[])value;
				
				if (cls != char[].class)
					value = value.toString().toCharArray();

				char[] chars = (char[])value;
				Character[] characters = new Character[chars.length];
				for (int i = 0; i < chars.length; i++)
					characters[i] = Character.valueOf(chars[i]);
				return characters;
			}
		};
		converters.put(Character[].class, converter);
		
		converter = new Converter<char[]>() {
			@Override
			public char[] convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == char[].class)
					return (char[])value;
				if (cls == Character[].class) {
					Character[] characters = (Character[])value;
					char[] chars = new char[characters.length];
					for (int i = 0; i < characters.length; i++) {
						Character c = characters[i];
						chars[i] = (c != null ? c.charValue() : '\0');
					}
					return chars;
				}

				return value.toString().toCharArray();
			}
		};
		converters.put(char[].class, converter);
		
		// Byte array
		
		final Charset UTF8 = Charset.forName("UTF-8");
		
		converter = new Converter<byte[]>() {
			@Override
			public byte[] convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == byte[].class)
					return (byte[])value;
				return value.toString().getBytes(UTF8);
			}
		};
		converters.put(byte[].class, converter);
		
		// Temporal types.
		
		final TimeZone UTC = TimeZone.getTimeZone("UTC");
		
		converter = new Converter<GregorianCalendar>() {
			@Override
			public GregorianCalendar convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				if (cls == SpearalDateTime.class)
					return ((SpearalDateTime)value).toGregorianCalendar();
				
				Date date;
				if (value instanceof Date)
					date = (Date)value;
				else if (value instanceof Number)
					date = new Date(((Number)value).longValue());
				else
					date = parseDate(value.toString());
				
				if (date != null) {
					GregorianCalendar calendar = new GregorianCalendar(UTC, Locale.US);
					calendar.setTime(date);
					return calendar;
				}
				
				return null;
			}
		};
		converters.put(Calendar.class, converter);
		converters.put(GregorianCalendar.class, converter);
		
		converter = new Converter<Date>() {
			@Override
			public Date convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == SpearalDateTime.class)
					return ((SpearalDateTime)value).toDate();
				if (cls == Date.class)
					return (Date)value;
				if (value instanceof Number)
					return new Date(((Number)value).longValue());
				
				return parseDate(value.toString());
			}
		};
		converters.put(Date.class, converter);
		
		converter = new Converter<java.sql.Date>() {
			@Override
			public java.sql.Date convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == SpearalDateTime.class)
					return ((SpearalDateTime)value).toSQLDate();
				if (cls == java.sql.Date.class)
					return (java.sql.Date)value;
				if (value instanceof Date)
					return new java.sql.Date(((Date)value).getTime());
				if (value instanceof Number)
					return new java.sql.Date(((Number)value).longValue());
				
				Date date = parseDate(value.toString());
				return (date != null ? new java.sql.Date(date.getTime()) : null);
			}
		};
		converters.put(java.sql.Date.class, converter);
		
		converter = new Converter<java.sql.Time>() {
			@Override
			public java.sql.Time convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == SpearalDateTime.class)
					return ((SpearalDateTime)value).toSQLTime();
				if (cls == java.sql.Time.class)
					return (java.sql.Time)value;
				if (value instanceof Date)
					return new java.sql.Time(((Date)value).getTime());
				if (value instanceof Number)
					return new java.sql.Time(((Number)value).longValue());
				
				Date date = parseDate(value.toString());
				return (date != null ? new java.sql.Time(date.getTime()) : null);
			}
		};
		converters.put(java.sql.Time.class, converter);
		
		converter = new Converter<java.sql.Timestamp>() {
			@Override
			public java.sql.Timestamp convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				Class<?> cls = value.getClass();
				
				if (cls == SpearalDateTime.class)
					return ((SpearalDateTime)value).toSQLTimestamp();
				if (cls == java.sql.Timestamp.class)
					return (java.sql.Timestamp)value;
				if (value instanceof Date)
					return new java.sql.Timestamp(((Date)value).getTime());
				if (value instanceof Number)
					return new java.sql.Timestamp(((Number)value).longValue());
				
				Date date = parseDate(value.toString());
				return (date != null ? new java.sql.Timestamp(date.getTime()) : null);
			}
		};
		converters.put(java.sql.Timestamp.class, converter);
		
		// Class.
		
		converter = new Converter<Class<?>>() {
			@Override
			public Class<?> convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value == null)
					return null;
				
				if (value.getClass() == Class.class)
					return ((Class<?>)value);

				try {
					return decoder.getContext().loadClass(value.toString());
				}
				catch (Exception e) {
					return null;
				}
			}
		};
		converters.put(Class.class, converter);
		
		// SpearalDateTime -> Object.
		
		converter = new Converter<Object>() {
			@Override
			public Object convert(ExtendedSpearalDecoder decoder, Object value, Type targetType) {
				if (value != null && value.getClass() == SpearalDateTime.class)
					return ((SpearalDateTime)value).toDate();
				return value;
			}
		};
		converters.put(Object.class, converter);
	}

	@Override
	public Converter<?> getConverter(Class<?> valueClass, Type targetType) {
		return converters.get(targetType);
	}
	
	private static Date parseDate(String s) {
		// TODO: support ISO-8601 format.
		DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.US);
		try {
			return format.parse(s);
		}
		catch (Exception e) {
			return null;
		}
	}
}
