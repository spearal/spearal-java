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
package org.spearal.impl.cache;

/**
 * @author Franck WOLFF
 */
public final class IdentityIndexMap extends AbstractIndexMap<Object> {

	public IdentityIndexMap() {
	}

	public IdentityIndexMap(int capacity) {
		super(capacity);
	}

	@Override
	public int putIfAbsent(Object key) {
		final int hash = System.identityHashCode(key);
        
		int index = hash & (entries.length - 1);
		Entry<Object> head = entries[index];
		
		if (head != null) {
			Entry<Object> entry = head;
			do {
				if (key == entry.key)
					return entry.index;
				entry = entry.next;
			}
			while (entry != null);
			
			if (size >= threshold) {
	            index = hash & resize(entries.length * 2);
	            head = entries[index];
			}
		}

        entries[index] = new Entry<Object>(key, hash, size, head);
        size++;
        
        return -1;
	}
}
