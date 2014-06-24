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
public abstract class AbstractKeyIndexMap<K> extends AbstractMap implements KeyIndexMap<K> {

	protected Entry<K>[] entries;

	public AbstractKeyIndexMap() {
	}

	public AbstractKeyIndexMap(int capacity) {
		super(capacity);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void init(int capacity) {
		super.init(capacity);
		
		this.entries = new Entry[capacity];
	}
    
	protected final int resize(int newCapacity) {
        Entry<K>[] oldEntries = entries;

        if (oldEntries.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return (MAXIMUM_CAPACITY - 1);
        }

		final int indexMask = newCapacity - 1;
        
		@SuppressWarnings("unchecked")
		Entry<K>[] newEntries = new Entry[newCapacity];
        for (Entry<K> entry : oldEntries) {
            while (entry != null) {
                int i = entry.hash & indexMask;
                Entry<K> next = entry.next;
                entry.next = newEntries[i];
                newEntries[i] = entry;
                entry = next;
            }
        }
        
        entries = newEntries;
        threshold = (int)Math.min(newCapacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        
        return indexMask;
    }

	protected final static class Entry<K> {
		
		public final K key;
		public final int hash;
		public final int index;
		
		public Entry<K> next;
		
		public Entry(K key, int hash, int index, Entry<K> next) {
			this.key = key;
			this.hash = hash;
			this.index = index;
			this.next = next;
		}
	}
}
