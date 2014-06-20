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

/**
 * @author Franck WOLFF
 */
public final class ClassCache<T> {
	
	private static final int MAXIMUM_CAPACITY = 1 << 30;
	private static final int DEFAULT_INITIAL_CAPACITY = 1 << 6;
	private static final float DEFAULT_LOAD_FACTOR = 0.75f;

	private final ValueProvider<T> provider;
	private int initialCapacity;
	private Entry[] entries;
	private int threshold;
	private int size;

	public ClassCache(ValueProvider<T> provider) {
		this.provider = provider;

		init(DEFAULT_INITIAL_CAPACITY);
	}
	
	public ClassCache(ValueProvider<T> provider, int capacity) {
		this.provider = provider;

		init(roundUpToPowerOf2(capacity));		
	}
	
	private void init(int capacity) {
		this.initialCapacity = capacity;
        this.entries = new Entry[capacity];
        this.threshold = (int)Math.min(capacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        this.size = 0;
	}
	
	@SuppressWarnings("unchecked")
	public T putIfAbsent(Class<?> key) {
		final int hash = key.hashCode();
		
		int index = hash & (entries.length - 1);
		Entry head = entries[index];
		
		if (head != null) {
			Entry entry = head;
			do {
				if (key == entry.key)
					return (T)entry.value;
				entry = entry.next;
			}
			while (entry != null);
			
			if (size >= threshold) {
	            index = hash & resize(entries.length * 2);
	            head = entries[index];
			}
		}

		T value = provider.createValue(key);
        entries[index] = new Entry(key, hash, value, head);
        size++;
		return value;
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		init(initialCapacity);
	}
    
	private int resize(int newCapacity) {
        Entry[] oldEntries = entries;

        if (oldEntries.length == MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return (MAXIMUM_CAPACITY - 1);
        }

		final int indexMask = newCapacity - 1;
        
		Entry[] newEntries = new Entry[newCapacity];
        for (Entry entry : oldEntries) {
            while (entry != null) {
                int i = entry.hash & indexMask;
                Entry next = entry.next;
                entry.next = newEntries[i];
                newEntries[i] = entry;
                entry = next;
            }
        }
        
        entries = newEntries;
        threshold = (int)Math.min(newCapacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        
        return indexMask;
    }
    
	private static int roundUpToPowerOf2(int number) {
    	if (number >= MAXIMUM_CAPACITY)
    		return MAXIMUM_CAPACITY;
    	int rounded = Integer.highestOneBit(number);
    	if (rounded == 0)
    		return 1;
    	if (Integer.bitCount(number) > 1)
    		return rounded << 1;
    	return rounded;
    }
	
	private final static class Entry {
		
		public final Class<?> key;
		public final int hash;
		public final Object value;
		
		public Entry next;
		
		public Entry(Class<?> key, int hash, Object value, Entry next) {
			this.key = key;
			this.hash = hash;
			this.value = value;
			this.next = next;
		}
	}
	
	public interface ValueProvider<T> {
		
		T createValue(Class<?> key);
	}
}
