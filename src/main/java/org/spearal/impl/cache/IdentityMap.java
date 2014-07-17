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
public class IdentityMap<K, P, V> extends AbstractAnyMap<K, P, V> {

	public IdentityMap(ValueProvider<K, P, V> provider) {
		super(provider);
	}

	public IdentityMap(ValueProvider<K, P, V> provider, int capacity) {
		super(provider, capacity);
	}

	@Override
	public V get(K key) {
		Entry<K, V>[] entries = this.entries;
		for (Entry<K, V> entry = entries[System.identityHashCode(key) & (entries.length - 1)];
			entry != null;
			entry = entry.next) {
			
			if (key == entry.key)
				return entry.value;
		}
		return null;
	}

	@Override
	public V putIfAbsent(SpearalContext context, K key) {
		return putIfAbsent(context, key, null);
	}


	@Override
	public V putIfAbsent(SpearalContext context, K key, P param) {
		Entry<K, V>[] entries = this.entries;

		int hash = System.identityHashCode(key);
		int index = hash & (entries.length - 1);
		Entry<K, V> head = entries[index];
		
		if (head != null) {
			Entry<K, V> entry = head;
			do {
				if (key == entry.key)
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

		V value = provider.createValue(context, key, param);
        entries[index] = new Entry<K, V>(key, hash, value, head);
        size++;
		return value;
	}

	@Override
	protected AbstractAnyMap<K, P, V> create(ValueProvider<K, P, V> provider, int capacity) {
		return new IdentityMap<K, P, V>(provider, capacity);
	}
}
