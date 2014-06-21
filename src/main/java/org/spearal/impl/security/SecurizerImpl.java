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
package org.spearal.impl.security;

import java.io.Serializable;
import java.lang.reflect.Type;

import org.spearal.configuration.Securizer;
import org.spearal.impl.util.TypeUtil;

/**
 * @author Franck WOLFF
 */
public class SecurizerImpl implements Securizer {

	@Override
	public void checkDecodable(Type type) throws SecurityException {
		if (!Serializable.class.isAssignableFrom(TypeUtil.classOfType(type)))
			throw new SecurityException("Not serializable: " + type);
	}

	@Override
	public void checkEncodable(Class<?> cls) throws SecurityException {
		if (!Serializable.class.isAssignableFrom(cls))
			throw new SecurityException("Not serializable: " + cls);
	}
}
