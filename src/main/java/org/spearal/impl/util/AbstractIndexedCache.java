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
public abstract class AbstractIndexedCache<T> {
	
	protected static final int MAXIMUM_CAPACITY = 1 << 30;
	protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 6;
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected int initialCapacity;
	protected Entry[] entries;
	protected int threshold;
	protected int size;
	
	protected final void init(int capacity) {
		this.initialCapacity = capacity;
        this.entries = new Entry[capacity];
        this.threshold = (int)Math.min(capacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        this.size = 0;
	}
	
	public abstract int putIfAbsent(T key);
	
	public final int size() {
		return size;
	}
	
	public final void clear() {
		init(initialCapacity);
	}
    
	protected final int resize(int newCapacity) {
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
    
	protected static int roundUpToPowerOf2(int number) {
    	if (number >= MAXIMUM_CAPACITY)
    		return MAXIMUM_CAPACITY;
    	int rounded = Integer.highestOneBit(number);
    	if (rounded == 0)
    		return 1;
    	if (Integer.bitCount(number) > 1)
    		return rounded << 1;
    	return rounded;
    }
	
	protected final static class Entry {
		
		public final Object key;
		public final int hash;
		public final int index;
		
		public Entry next;
		
		public Entry(Object key, int hash, int index, Entry next) {
			this.key = key;
			this.hash = hash;
			this.index = index;
			this.next = next;
		}
	}
}
