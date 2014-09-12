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
package org.spearal.impl.util;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

/**
 * @author Franck WOLFF
 */
public abstract class TypeUtil {

	public static Class<?> classOfType(Type type) {
        if (type instanceof Class<?>)
            return (Class<?>)type;
        if (type instanceof ParameterizedType)
            return (Class<?>)((ParameterizedType)type).getRawType();
        if (type instanceof WildcardType) {
            // Forget lower bounds and only deal with first upper bound...
            Type[] ubs = ((WildcardType)type).getUpperBounds();
            if (ubs.length > 0)
                return classOfType(ubs[0]);
        }
        if (type instanceof GenericArrayType) {
            Class<?> ct = classOfType(((GenericArrayType)type).getGenericComponentType());
            return (ct != null ? Array.newInstance(ct, 0).getClass() : Object[].class);
        }
        if (type instanceof TypeVariable<?>) {
            // Only deal with first (upper) bound...
            Type[] ubs = ((TypeVariable<?>)type).getBounds();
            if (ubs.length > 0)
                return classOfType(ubs[0]);
        }
        // Should never append...
        return Object.class;
    }
	
	public static Type unwrapTypeVariable(Type type) {
		if (type instanceof TypeVariable)
			return getBoundType((TypeVariable<?>)type);
		return type;
	}
	
	public static Type getBoundType(TypeVariable<?> typeVariable) {
    	Type[] ubs = typeVariable.getBounds();
    	if (ubs.length > 0)
    		return ubs[0];
    	
    	// should never happen...
    	if (typeVariable.getGenericDeclaration() instanceof Type)
    		return (Type)typeVariable.getGenericDeclaration();
    	return typeVariable;
    }
	
    public static Type getElementType(Type collectionType) {
    	if (collectionType instanceof ParameterizedType) {
            Type[] componentTypes = ((ParameterizedType)collectionType).getActualTypeArguments();
            if (componentTypes != null && componentTypes.length == 1)
                return componentTypes[0];
        }
        return Object.class;
	}

    public static Type getKeyType(Type mapType) {
    	if (mapType instanceof ParameterizedType) {
            Type[] componentTypes = ((ParameterizedType)mapType).getActualTypeArguments();
            if (componentTypes != null && componentTypes.length == 2)
                return componentTypes[0];
        }
        return Object.class;
	}
	
    public static Type getValueType(Type mapType) {
    	if (mapType instanceof ParameterizedType) {
            Type[] componentTypes = ((ParameterizedType)mapType).getActualTypeArguments();
            if (componentTypes != null && componentTypes.length == 2)
                return componentTypes[1];
        }
        return Object.class;
	}
	
    public static Type[] getKeyValueType(Type mapType) {
    	if (mapType instanceof ParameterizedType) {
            Type[] componentTypes = ((ParameterizedType)mapType).getActualTypeArguments();
            if (componentTypes != null && componentTypes.length == 2)
                return componentTypes;
        }
        return new Type[] {Object.class, Object.class};
	}
}
