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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.spearal.SpearalInput;
import org.spearal.configurable.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface ExtendedSpearalInput extends SpearalInput {

//	Object readAny(Type type) throws IOException;
	Object readAny(int type) throws IOException;
	void skipAny(int type) throws IOException;
	
	Object readBean(int type) throws IOException;
	String readString(int type) throws IOException;
	long readIntegral(int type) throws IOException;
	double readFloating(int type) throws IOException;
	Date readDate(int type) throws IOException;
	
	Collection<?> readCollection(int type) throws IOException;
	void readCollection(int type, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;

	Map<?, ?> readMap(int type) throws IOException;
	void readMap(int type, Object holder, Property property)
		throws IOException, InstantiationException, IllegalAccessException, InvocationTargetException;
}
