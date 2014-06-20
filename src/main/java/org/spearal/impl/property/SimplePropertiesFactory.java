package org.spearal.impl.property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import org.spearal.configurable.PropertyFactory;

public class SimplePropertiesFactory implements PropertyFactory {
	
	@Override
	public Property createProperty(String name, Field field, Method getter, Method setter) {
		Class<?> type = AbstractProperty.typeOf(field, getter);
		
		if (type.isPrimitive()) {
			if (type == boolean.class)
				return new PrimitiveBooleanProperty(name, field, getter, setter);
			if (type == int.class)
				return new PrimitiveIntProperty(name, field, getter, setter);
			if (type == long.class)
				return new PrimitiveLongProperty(name, field, getter, setter);
			if (type == double.class)
				return new PrimitiveDoubleProperty(name, field, getter, setter);
		}
		else {
			if (type == String.class)
				return new StringProperty(name, field, getter, setter);
			if (Collection.class.isAssignableFrom(type))
				return new CollectionProperty(name, field, getter, setter);
			if (Map.class.isAssignableFrom(type))
				return new MapProperty(name, field, getter, setter);
		}
		
		return new GeneralProperty(name, field, getter, setter);
	}
}
