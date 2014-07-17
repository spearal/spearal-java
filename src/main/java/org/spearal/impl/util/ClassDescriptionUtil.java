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

import java.lang.reflect.Proxy;

import org.spearal.SpearalContext;
import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public class ClassDescriptionUtil {
	
	public static String classNames(String description) {
		int iSharp = description.indexOf('#');
		if (iSharp == -1)
			return description;
		return description.substring(0, iSharp);
	}
	
	public static String[] splitClassNames(String description) {
		int iSharp = description.indexOf('#');
		return split(description, 0, (iSharp != -1 ? iSharp : description.length()));
	}
	
	public static String[] splitPropertyNames(String description) {
		int iSharp = description.indexOf('#');
		if (iSharp == -1)
			return new String[0];
		return split(description, iSharp + 1, description.length());
	}
	
	public static int propertiesCount(String description) {
		int iSharp = description.indexOf('#');
		if (iSharp == -1)
			return 0;
		
		int count = 1;
		for (int i = iSharp + 1; i < description.length(); i++) {
			if (description.charAt(i) == ',')
				count++;
		}
		return count;
	}
	
	public static String createAliasedDescription(SpearalContext context, Class<?> cls, Property[] properties) {
		StringBuilder sb = new StringBuilder(64);

		if (!Proxy.isProxyClass(cls))
			sb.append(context.alias(cls)).append('#');
		else {
			Class<?>[] interfaces = cls.getInterfaces();
			if (interfaces.length > 0) {
				sb.append(context.alias(interfaces[0]));
				for (int i = 1; i < interfaces.length; i++)
					sb.append(',').append(context.alias(interfaces[i]));
			}
			sb.append('#');
		}

		boolean first = true;
		for (Property property : properties) {
			if (property == null)
				continue;
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(property.getName());
		}

		return sb.toString();
	}
	
	public static String createAliasedDescription(SpearalContext context, Class<?> cls, String[] properties) {
		StringBuilder sb = new StringBuilder(64);

		if (!Proxy.isProxyClass(cls))
			sb.append(context.alias(cls)).append('#');
		else {
			Class<?>[] interfaces = cls.getInterfaces();
			if (interfaces.length > 0) {
				sb.append(context.alias(interfaces[0]));
				for (int i = 1; i < interfaces.length; i++)
					sb.append(',').append(context.alias(interfaces[i]));
			}
			sb.append('#');
		}

		boolean first = true;
		for (String property : properties) {
			if (property == null)
				continue;
			if (first)
				first = false;
			else
				sb.append(',');
			sb.append(property);
		}

		return sb.toString();
	}
	
	private static String[] split(String s, int from, int to) {
		int count = 1;
		for (int i = from; i < to; i++) {
			if (s.charAt(i) == ',')
				count++;
		}
		
		if (count == 1)
			return new String[]{ s.substring(from, to) };
		
		String[] result = new String[count];
		int iResult = 0, iStart = from;
		for (int i = from; i < to; i++) {
			if (s.charAt(i) == ',') {
				result[iResult++] = s.substring(iStart, i);
				iStart = i + 1;
			}
		}
		result[iResult] = s.substring(iStart, to);
		return result;
	}
}
