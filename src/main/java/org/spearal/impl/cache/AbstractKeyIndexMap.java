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
    
	@Override
	public String toString() {
		Object[] keys = new Object[size];

		Entry<K>[] entries = this.entries;
		for (Entry<K> entry : entries) {
			if (entry == null)
				continue;
			keys[entry.index] = entry.key;
			for (Entry<K> next = entry.next; next != null; next = next.next)
				keys[next.index] = next.key;
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append('{');
		if (keys.length > 0) {
			sb.append("0:").append(keys[0]);
			for (int i = 1; i < keys.length; i++)
				sb.append(", ").append(i).append(":").append(keys[i]);
		}
		sb.append('}');
		return sb.toString();
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
			if (key == null)
				throw new NullPointerException();
			
			this.key = key;
			this.hash = hash;
			this.index = index;
			this.next = next;
		}
	}
}
