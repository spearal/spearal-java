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
import org.spearal.impl.cache.KeyValueMap.ValueProvider;

/**
 * @author Franck WOLFF
 */
public final class CopyOnWriteKeyValueMap<K, V> {

	protected volatile KeyValueMap<K, V> map;
	
	public CopyOnWriteKeyValueMap(boolean useIdentity) {
		this(useIdentity, null);
	}
	
	public CopyOnWriteKeyValueMap(boolean useIdentity, ValueProvider<K, V> provider) {
		if (useIdentity)
			 this.map = new IdentityValueMap<K, V>(provider, 1);
		else
			 this.map = new EqualityValueMap<K, V>(provider, 1);
	}
	
	public V get(K key) {
		return get().get(key);
	}
	
	public synchronized V putIfAbsent(SpearalContext context, K key) {
		KeyValueMap<K, V> map = get();
		
		V value = map.get(key);
		if (value == null) {
			map = map.clone();
			value = map.putIfAbsent(context, key);
			set(map);
		}
		return value;
	}
	
	public V getOrPutIfAbsent(SpearalContext context, K key) {
		V value = get().get(key);
		return (value != null ? value : putIfAbsent(context, key));
	}
	
	public int size() {
		return get().size();
	}
	
	@Override
	public String toString() {
		return get().toString();
	}

	private KeyValueMap<K, V> get() {
		return map;
	}
	
	private void set(KeyValueMap<K, V> map) {
		this.map = map;
	}
}
