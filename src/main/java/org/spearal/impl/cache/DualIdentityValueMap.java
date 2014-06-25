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

import org.spearal.SpearalContext;

/**
 * @author Franck WOLFF
 */
public final class DualIdentityValueMap<K1, K2, V> extends AbstractMap {

	protected ValueProvider<K1, K2, V> provider;
	protected Entry<K1, K2, V>[] entries;

	public DualIdentityValueMap(ValueProvider<K1, K2, V> provider) {
		super();

		this.provider = provider;
	}

	public DualIdentityValueMap(ValueProvider<K1, K2, V> provider, int capacity) {
		super(capacity);
		
		this.provider = provider;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void init(int capacity) {
		super.init(capacity);
		
		this.entries = new Entry[capacity];
	}

	public V get(K1 key1, K2 key2) {
		int hash = hash(key1, key2);
		
		Entry<K1, K2, V>[] entries = this.entries;
		for (Entry<K1, K2, V> entry = entries[hash & (entries.length - 1)];
			entry != null;
			entry = entry.next) {
			
			if (key1 == entry.key1 && key2 == entry.key2)
				return entry.value;
		}
		return null;
	}
	
	public V putIfAbsent(SpearalContext context, K1 key1, K2 key2) {
		Entry<K1, K2, V>[] entries = this.entries;

		int hash = hash(key1, key2);
		int index = hash & (entries.length - 1);
		Entry<K1, K2, V> head = entries[index];
		
		if (head != null) {
			Entry<K1, K2, V> entry = head;
			do {
				if (key1 == entry.key1 && key2 == entry.key2)
					return entry.value;
				entry = entry.next;
			}
			while (entry != null);
			
			if (size >= threshold) {
	            index = hash & resize(entries.length * 2);
	            entries = this.entries;
	            head = entries[index];
			}
		}

		V value = provider.createValue(context, key1, key2);
        entries[index] = new Entry<K1, K2, V>(key1, key2, hash, value, head);
        size++;
		return value;
	}

	@Override
	public String toString() {
		Entry<K1, K2, V>[] entries = this.entries;
		boolean first = true;

		StringBuilder sb = new StringBuilder(256);
		sb.append('{');
		for (Entry<K1, K2, V> entry : entries) {
			if (entry == null)
				continue;
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(entry.key1).append('#').append(entry.key2).append('=').append(entry.value);
			for (Entry<K1, K2, V> next = entry.next; next != null; next = next.next)
				sb.append(", ").append(entry.key1).append('#').append(entry.key2).append('=').append(entry.value);
		}
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public DualIdentityValueMap<K1, K2, V> clone() {
		Entry<K1, K2, V>[] entries = this.entries;

		DualIdentityValueMap<K1, K2, V> clone = new DualIdentityValueMap<K1, K2, V>(provider, entries.length);
		
		clone.threshold = threshold;
		clone.size = size;
		
		for (int i = 0; i < entries.length; i++) {
			Entry<K1, K2, V> entry = entries[i];
			if (entry != null)
				clone.entries[i] = entry.clone();
		}
		
		return clone;
	}
    
	private int resize(int newCapacity) {
        Entry<K1, K2, V>[] oldEntries = entries;

        if (oldEntries.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return (MAXIMUM_CAPACITY - 1);
        }

		final int indexMask = newCapacity - 1;
        
		@SuppressWarnings("unchecked")
		Entry<K1, K2, V>[] newEntries = new Entry[newCapacity];
        for (Entry<K1, K2, V> entry : oldEntries) {
            while (entry != null) {
                int i = entry.hash & indexMask;
                Entry<K1, K2, V> next = entry.next;
                entry.next = newEntries[i];
                newEntries[i] = entry;
                entry = next;
            }
        }
        
        entries = newEntries;
        threshold = (int)Math.min(newCapacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        
        return indexMask;
    }
	
	private static int hash(Object key1, Object key2) {
		return System.identityHashCode(key1) + System.identityHashCode(key2);
	}
	
	private final static class Entry<K1, K2, V> implements Cloneable {
		
		public final K1 key1;
		public final K2 key2;
		public final int hash;
		public final V value;
		
		public Entry<K1, K2, V> next;
		
		public Entry(K1 key1, K2 key2, int hash, V value, Entry<K1, K2, V> next) {
			if ((key1 == null && key2 == null) || value == null)
				throw new NullPointerException();
			
			this.key1 = key1;
			this.key2 = key2;
			this.hash = hash;
			this.value = value;
			this.next = next;
		}

		@Override
		public Entry<K1, K2, V> clone() {
			return new Entry<K1, K2, V>(key1, key2, hash, value, (next != null ? next.clone() : null));
		}
	}
	
	public interface ValueProvider<K1, K2, V> {
		
		V createValue(SpearalContext context, K1 key1, K2 key2);
	}
}
