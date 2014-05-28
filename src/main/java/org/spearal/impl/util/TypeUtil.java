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

//    public static Object newInstance(String type)
//        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//        return forName(type).newInstance();
//    }
//
//    public static <T> T newInstance(String type, Class<T> cast)
//        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//        return forName(type, cast).newInstance();
//    }
//
//    public static Object newInstance(String type, Class<?>[] argsClass, Object[] argsValues)
//        throws ClassNotFoundException, InstantiationException, IllegalAccessException {
//        return newInstance(forName(type), argsClass, argsValues);
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <T> T newInstance(Class<?> type, Class<T> cast)
//        throws InstantiationException, IllegalAccessException {
//        return (T)type.newInstance();
//    }
//
//    public static <T> T newInstance(Class<T> type, Class<?>[] argsClass, Object[] argsValues)
//        throws InstantiationException, IllegalAccessException {
//        T instance = null;
//        try {
//            Constructor<T> constructorDef = type.getConstructor(argsClass);
//            instance = constructorDef.newInstance(argsValues);
//        } catch (SecurityException e) {
//            throw new InstantiationException(e.getMessage());
//        } catch (NoSuchMethodException e) {
//            throw new InstantiationException(e.getMessage());
//        } catch (IllegalArgumentException e) {
//            throw new InstantiationException(e.getMessage());
//        } catch (InvocationTargetException e) {
//            Logger.getLogger(TypeUtil.class).error(e.getTargetException(), "Could not create instance of type %s", type.getName());
//            throw new InstantiationException(e.getTargetException().getMessage());
//        }
//        return instance;
//    }
//
//    public static Class<?> forName(String type) throws ClassNotFoundException {
//    	try {
//    		return TypeUtil.class.getClassLoader().loadClass(type);
//    	}
//    	catch (ClassNotFoundException e) {
//    		return Thread.currentThread().getContextClassLoader().loadClass(type);
//    	}
//    }
//
//    @SuppressWarnings("unchecked")
//    public static <T> Class<T> forName(String type, Class<T> cast) throws ClassNotFoundException {
//    	try {
//    		return (Class<T>)TypeUtil.class.getClassLoader().loadClass(type);
//    	}
//    	catch (ClassNotFoundException e) {
//    		return (Class<T>)Thread.currentThread().getContextClassLoader().loadClass(type);
//    	}
//    }
//
//    public static Constructor<?> getConstructor(String type, Class<?>[] paramTypes)
//        throws ClassNotFoundException, NoSuchMethodException {
//        return getConstructor(forName(type), paramTypes);
//    }
//
//    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>[] paramTypes)
//        throws NoSuchMethodException {
//        return type.getConstructor(paramTypes);
//    }
//
//    public static <T> List<T> emptyList(Class<T> type) {
//        return Collections.emptyList();
//    }
//
//    public static <T> Set<T> emptySet(Class<T> type) {
//        return Collections.emptySet();
//    }
//
//    public static <T, U> Map<T, U> emptyMap(Class<T> keyType, Class<U> valueType) {
//        return Collections.emptyMap();
//    }
//
//    public static boolean isPrimitive(Type type) {
//        return type instanceof Class<?> && ((Class<?>)type).isPrimitive();
//    }
//    
//	public static Class<?> componentClassOfType(Type arrayOrCollectionType) {
//		Class<?> arrayOrCollectionClass = classOfType(arrayOrCollectionType);
//		if (!arrayOrCollectionClass.isArray() && !Collection.class.isAssignableFrom(arrayOrCollectionClass))
//			throw new IllegalArgumentException("Not an array or Collection: " + arrayOrCollectionType);
//		
//		if (arrayOrCollectionType instanceof Class) {
//			Class<?> cls = (Class<?>)arrayOrCollectionType;
//			
//			if (cls.isArray())
//				return cls.getComponentType();
//			
//			Type genericSuperclass = cls.getGenericSuperclass();
//			if (genericSuperclass != null && Collection.class.isAssignableFrom(classOfType(genericSuperclass)))
//				return componentClassOfType(genericSuperclass);
//			
//			Type[] genericInterfaces = cls.getGenericInterfaces();
//			for (Type genericInterface : genericInterfaces) {
//				if (Collection.class.isAssignableFrom(classOfType(genericInterface)))
//					return componentClassOfType(genericInterface);
//			}
//			
//			return Object.class;
//		}
//		if (arrayOrCollectionType instanceof ParameterizedType) {
//			ParameterizedType pType = (ParameterizedType)arrayOrCollectionType;
//			Type[] args = pType.getActualTypeArguments();
//			
//			if (args.length > 0)
//				return classOfType(args[0]);
//			
//			return Object.class; 
//		}
//		if (arrayOrCollectionType instanceof GenericArrayType)
//			return classOfType(((GenericArrayType)arrayOrCollectionType).getGenericComponentType());
//		
//		return Object.class;
//	}

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
    
//    public static Type getBoundType(TypeVariable<?> typeVariable) {
//    	Type[] ubs = typeVariable.getBounds();
//    	if (ubs.length > 0)
//    		return ubs[0];
//    	
//    	// should never happen...
//    	if (typeVariable.getGenericDeclaration() instanceof Type)
//    		return (Type)typeVariable.getGenericDeclaration();
//    	return typeVariable;
//    }
//    
//    public static ParameterizedType[] getDeclaringTypes(Class<?> type) {
//		List<ParameterizedType> supertypes = new ArrayList<ParameterizedType>();
//		
//		Type stype = type.getGenericSuperclass();
//		Class<?> sclass = type.getSuperclass();
//		while (sclass != null && sclass != Object.class) {
//			if (stype instanceof ParameterizedType)
//				supertypes.add((ParameterizedType)stype);
//			stype = sclass.getGenericSuperclass();
//			sclass = sclass.getSuperclass();
//		}
//		
//		collectGenericInterfaces(type.getGenericInterfaces(), supertypes);
//		
//		return supertypes.isEmpty() ? null : supertypes.toArray(new ParameterizedType[supertypes.size()]);
//    }
//    
//    private static void collectGenericInterfaces(Type[] types, List<ParameterizedType> supertypes) {
//    	if (types == null)
//    		return;
//		for (Type t : types) {
//			if (t instanceof ParameterizedType)
//				supertypes.add((ParameterizedType)t);
//			else
//				collectGenericInterfaces(((Class<?>)t).getGenericInterfaces(), supertypes);
//		}
//    }
//   
//    public static Type resolveTypeVariable(Type genericType, Class<?> declaringClass, ParameterizedType[] declaringTypes) {
//    	if (genericType instanceof TypeVariable && declaringTypes != null) {
//    		int index = -1;
//    		TypeVariable<?> typeVariable = (TypeVariable<?>)genericType;
//    		ParameterizedType declaringType = null;
//    		for (int j = 0; j < declaringClass.getTypeParameters().length; j++) {
//    			Type typeParameter = declaringClass.getTypeParameters()[j];
//    			if (typeParameter == typeVariable)
//    				index = j;
//    			else if (typeVariable.getBounds() != null) {
//    				for (Type t : typeVariable.getBounds()) {
//    					if (typeParameter == t) {
//    						index = j;
//    						break;
//    					}
//    				}
//    			}
//    			if (index >= 0) {
//					for (ParameterizedType t : declaringTypes) {
//						if (declaringClass.isAssignableFrom(classOfType(t))) {
//							declaringType = t;
//							break;
//						}
//					}
//					break;
//    			}
//    		}
//    		if (declaringType != null && index >= 0 && index < declaringType.getActualTypeArguments().length)
//    			return declaringType.getActualTypeArguments()[index];
//    	}
//    	return genericType;
//    }
//    public static String getPackageName(Class<?> clazz) {
//        return clazz.getPackage() != null ? clazz.getPackage().getName() : "";
//    }
//    
//    public static PropertyDescriptor[] getProperties(Class<?> clazz) {
//        try {
//        	PropertyDescriptor[] properties = Introspector.getPropertyDescriptors(clazz);
//        	Field[] fields = clazz.getDeclaredFields();
//        	for (Field field : fields) {
//        		if (Boolean.class.equals(field.getType())) {
//        			boolean found = false;
//        			for (PropertyDescriptor property : properties) {
//        				if (property.getName().equals(field.getName())) {
//        					found = true;
//        					if (property.getReadMethod() == null) {
//        						try {
//        							Method readMethod = clazz.getDeclaredMethod(getIsMethodName(field.getName()));
//        							if (Modifier.isPublic(readMethod.getModifiers()) && !Modifier.isStatic(readMethod.getModifiers()))
//        								property.setReadMethod(readMethod);
//        						}
//        						catch (NoSuchMethodException e) {
//        						}
//        					}
//        					break;
//        				}
//        			}
//        			if (!found) {
//						try {
//							Method readMethod = clazz.getDeclaredMethod(getIsMethodName(field.getName()));
//							if (Modifier.isPublic(readMethod.getModifiers()) && !Modifier.isStatic(readMethod.getModifiers())) {
//								PropertyDescriptor[] propertiesTmp = new PropertyDescriptor[properties.length + 1];
//								System.arraycopy(properties, 0, propertiesTmp, 0, properties.length);
//								propertiesTmp[properties.length] = new PropertyDescriptor(field.getName(), readMethod, null);
//								properties = propertiesTmp;
//							}
//						}
//						catch (NoSuchMethodException e) {
//						}
//        			}
//        		}
//        	}
//            return properties;
//        } catch (Exception e) {
//            throw new RuntimeException("Could not introspect properties of class: " + clazz, e);
//        }
//    }
//    
//    private static String getIsMethodName(String name) {
//    	return "is" + name.substring(0, 1).toUpperCase() + name.substring(1);
//    }
//   
//    public static ClassLoader getClassLoader(Class<?> clazz) {
//        return (clazz.getClassLoader() != null ? clazz.getClassLoader() : ClassLoader.getSystemClassLoader());
//    }
//
//    public static URL findResource(Class<?> clazz) {
//        while (clazz.isArray())
//            clazz = clazz.getComponentType();
//        if (clazz.isPrimitive())
//            return null;
//        URL url = getClassLoader(clazz).getResource(toResourceName(clazz));
//        String path = url.toString();
//        if (path.indexOf(' ') != -1) {
//        	try {
//				url = new URL(path.replace(" ", "%20"));
//			} catch (MalformedURLException e) {
//				// should never happen...
//			}
//        }
//        return url;
//    }
//
//    public static String toResourceName(Class<?> clazz) {
//        return clazz.getName().replace('.', '/').concat(".class");
//    }
//    
//    public static String getMethodSignature(Method method) {
//    	StringBuilder sb = new StringBuilder();
//    	sb.append(method.getName()).append('(');
//    	Class<?>[] params = method.getParameterTypes();
//    	for (int i = 0; i < params.length; i++) {
//    		if (i > 0)
//    			sb.append(',');
//    		sb.append(getTypeSignature(params[i]));
//    	}
//    	sb.append(')');
//    	return sb.toString();
//    }
//	
//    public static String getTypeSignature(Class<?> type) {
//		if (type.isArray()) {
//		    try {
//				int dimensions = 1;
//				Class<?> clazz = type.getComponentType();
//				while (clazz.isArray()) {
//					dimensions++;
//					clazz = clazz.getComponentType();
//				}
//				
//				StringBuffer sb = new StringBuffer(clazz.getName());
//				while (dimensions-- > 0)
//				    sb.append("[]");
//				return sb.toString();
//		    } catch (Throwable e) {
//		    	// fallback...
//		    }
//		}
//		return type.getName();
//	}
//    
//    public static Method getMethod(Class<?> clazz, String signature) throws NoSuchMethodException {
//    	signature = StringUtil.removeSpaces(signature);
//		
//    	if (!signature.endsWith(")"))
//			signature += "()";
//		
//		for (Method method : clazz.getMethods()) {
//			if (signature.equals(getMethodSignature(method)))
//				return method;
//		}
//		
//		throw new NoSuchMethodException("Could not find method: " + signature + " in class: " + clazz);
//    }
//    
//    public static boolean isAnnotationPresent(AnnotatedElement elmt, Class<? extends Annotation> annotationClass) {
//    	return getAnnotation(elmt, annotationClass) != null;
//    }
//    
//    public static <T extends Annotation> DeclaredAnnotation<T> getAnnotation(AnnotatedElement elmt, Class<T> annotationClass) {
//    	T annotation = elmt.getAnnotation(annotationClass);
//    	
//    	if (annotation != null) {
//    		Class<?> declaringClass = (elmt instanceof Member ? ((Member)elmt).getDeclaringClass() : (Class<?>)elmt);
//    		return new DeclaredAnnotation<T>(annotation, elmt, declaringClass);
//    	}
//    	
//    	if (elmt instanceof Field)
//    		return null;
//    	
//    	if (elmt instanceof Method) {
//    		Method m = (Method)elmt;
//    		return getMethodAnnotation(m.getDeclaringClass(), m.getName(), m.getParameterTypes(), annotationClass);
//    	}
//    	
//    	if (elmt instanceof Constructor) {
//    		Constructor<?> c = (Constructor<?>)elmt;
//    		return getConstructorAnnotation(c.getDeclaringClass(), annotationClass);
//    	}
//    	
//    	if (elmt instanceof Class) {
//    		Class<?> c = (Class<?>)elmt;
//    		return getClassAnnotation(c.getDeclaringClass(), annotationClass);
//    	}
//    	
//    	throw new RuntimeException("Unsupported annotated element: " + elmt);
//    }
//    
//    public static <T extends Annotation> DeclaredAnnotation<T> getMethodAnnotation(Class<?> clazz, String name, Class<?>[] parameterTypes, Class<T> annotationClass) {
//    	DeclaredAnnotation<T> declaredAnnotation = null;
//    	
//    	try {
//    		Method method = clazz.getDeclaredMethod(name, parameterTypes);
//    		T annotation = clazz.getDeclaredMethod(name, parameterTypes).getAnnotation(annotationClass);
//    		if (annotation != null)
//    			declaredAnnotation = new DeclaredAnnotation<T>(annotation, method, clazz);
//    	}
//    	catch (NoSuchMethodException e) {
//    		// fallback...
//    	}
//    	
//    	if (declaredAnnotation == null && clazz.getSuperclass() != null)
//    		declaredAnnotation = getMethodAnnotation(clazz.getSuperclass(), name, parameterTypes, annotationClass);
//    	
//    	if (declaredAnnotation == null) {
//	    	for (Class<?> interfaze : clazz.getInterfaces()) {
//	    		declaredAnnotation = getMethodAnnotation(interfaze, name, parameterTypes, annotationClass);
//	    		if (declaredAnnotation != null)
//	    			break;
//	    	}
//    	}
//    		
//    	return declaredAnnotation;
//    }
//    
//    public static <T extends Annotation> DeclaredAnnotation<T> getConstructorAnnotation(Class<?> clazz, Class<T> annotationClass) {
//    	DeclaredAnnotation<T> declaredAnnotation = null;
//    	
//    	for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
//    		T annotation = constructor.getAnnotation(annotationClass);
//    		if (annotation != null) {
//    			declaredAnnotation = new DeclaredAnnotation<T>(annotation, constructor, clazz);
//    			break;
//    		}
//    	}
//    	
//    	if (declaredAnnotation == null && clazz.getSuperclass() != null)
//    		declaredAnnotation = getConstructorAnnotation(clazz.getSuperclass(), annotationClass);
//    		
//    	return declaredAnnotation;
//    }
//    
//    public static <T extends Annotation> DeclaredAnnotation<T> getClassAnnotation(Class<?> clazz, Class<T> annotationClass) {
//    	DeclaredAnnotation<T> declaredAnnotation = null;
//    	
//    	T annotation = clazz.getAnnotation(annotationClass);
//    	if (annotation != null)
//    		declaredAnnotation = new DeclaredAnnotation<T>(annotation, clazz, clazz);
//    	else {
//	    	if (clazz.getSuperclass() != null)
//	    		declaredAnnotation = getClassAnnotation(clazz.getSuperclass(), annotationClass);
//	    	
//	    	if (declaredAnnotation == null) {
//	    		for (Class<?> interfaze : clazz.getInterfaces()) {
//		    		declaredAnnotation = getClassAnnotation(interfaze, annotationClass);
//		    		if (declaredAnnotation != null)
//		    			break;
//		    	}
//	    	}
//    	}
//    		
//    	return declaredAnnotation;
//    }
//    
//    public static class DeclaredAnnotation<T extends Annotation> {
//
//    	public final T annotation;
//    	public final AnnotatedElement annotatedElement;
//    	public final Class<?> declaringClass;
//		
//    	public DeclaredAnnotation(T annotation, AnnotatedElement annotatedElement, Class<?> declaringClass) {
//			this.annotation = annotation;
//			this.annotatedElement = annotatedElement;
//			this.declaringClass = declaringClass;
//		}
//
//		@Override
//		public String toString() {
//			return getClass().getName() + "{annotation=" + annotation + ", annotatedElement=" + annotatedElement + ", declaringClass=" + declaringClass + "}";
//		}
//    }
}
