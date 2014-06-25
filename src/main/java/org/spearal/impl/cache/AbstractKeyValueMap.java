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
public abstract class AbstractKeyValueMap<K, V> extends AbstractMap implements KeyValueMap<K, V> {

	protected ValueProvider<K, V> provider;
	protected Entry<K, V>[] entries;
	
	public AbstractKeyValueMap() {
		this.provider = null;
	}

	public AbstractKeyValueMap(ValueProvider<K, V> provider) {
		this.provider = provider;
	}

	public AbstractKeyValueMap(int capacity) {
		super(capacity);
		this.provider = null;
	}

	public AbstractKeyValueMap(int capacity, ValueProvider<K, V> provider) {
		super(capacity);
		this.provider = provider;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void init(int capacity) {
		super.init(capacity);
		
		entries = new Entry[capacity];
	}

	
	public ValueProvider<K, V> getProvider() {
		return provider;
	}

	protected void setProvider(ValueProvider<K, V> provider) {
		this.provider = provider;
	}

	@Override
	public String toString() {
		Entry<K, V>[] entries = this.entries;
		boolean first = true;

		StringBuilder sb = new StringBuilder(256);
		sb.append('{');
		for (Entry<K, V> entry : entries) {
			if (entry == null)
				continue;
			if (first)
				first = false;
			else
				sb.append(", ");
			sb.append(entry.key).append('=').append(entry.value);
			for (Entry<K, V> next = entry.next; next != null; next = next.next)
				sb.append(", ").append(entry.key).append('=').append(entry.value);
		}
		sb.append('}');
		return sb.toString();
	}

	protected abstract AbstractKeyValueMap<K, V> create(int capacity, ValueProvider<K, V> provider);
	
	@Override
	public KeyValueMap<K, V> clone() {
		Entry<K, V>[] entries = this.entries;

		AbstractKeyValueMap<K, V> clone = create(entries.length, provider);
		
		clone.threshold = threshold;
		clone.size = size;
		
		for (int i = 0; i < entries.length; i++) {
			Entry<K, V> entry = entries[i];
			if (entry != null)
				clone.entries[i] = entry.clone();
		}
		
		return clone;
	}
    
	protected final int resize(int newCapacity) {
        Entry<K, V>[] oldEntries = entries;

        if (oldEntries.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return (MAXIMUM_CAPACITY - 1);
        }

		final int indexMask = newCapacity - 1;
        
		@SuppressWarnings("unchecked")
		Entry<K, V>[] newEntries = new Entry[newCapacity];
        for (Entry<K, V> entry : oldEntries) {
            while (entry != null) {
                int i = entry.hash & indexMask;
                Entry<K, V> next = entry.next;
                entry.next = newEntries[i];
                newEntries[i] = entry;
                entry = next;
            }
        }
        
        entries = newEntries;
        threshold = (int)Math.min(newCapacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        
        return indexMask;
    }

	protected final static class Entry<K, V> implements Cloneable {
		
		public final K key;
		public final int hash;
		public final V value;
		
		public Entry<K, V> next;
		
		public Entry(K key, int hash, V value, Entry<K, V> next) {
			this.key = key;
			this.hash = hash;
			this.value = value;
			this.next = next;
		}

		@Override
		public Entry<K, V> clone() {
			return new Entry<K, V>(key, hash, value, (next != null ? next.clone() : null));
		}
	}
}
