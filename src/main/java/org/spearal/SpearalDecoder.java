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
package org.spearal;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.spearal.configuration.PropertyFactory.Property;

/**
 * @author Franck WOLFF
 */
public interface SpearalDecoder {
	
	public interface PathSegment {
		
		PathSegment copy();
	}

	public interface CollectionPathSegment extends PathSegment {
		
		public Collection<?> getCollection();
		public int getIndex();
	}

	public interface ArrayPathSegment extends PathSegment {
		
		public Object getArray();
		public int getIndex();
	}
	
	public interface MapPathSegment extends PathSegment {

		public Map<?, ?> getMap();
		public Object getKey();
	}
	
	public interface BeanPathSegment extends PathSegment {

		public Object getBean();
		public Property getProperty();
	}

	SpearalContext getContext();
	
	Object readAny() throws IOException;
	<T> T readAny(Type targetType) throws IOException;
	
	boolean containsPartialObjects();
	Map<Object, List<PathSegment>> getPartialObjectsMap();
	
	void skipAny() throws IOException;
	
	void printAny(SpearalPrinter printer) throws IOException;
}
