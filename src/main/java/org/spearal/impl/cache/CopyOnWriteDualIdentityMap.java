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
public final class CopyOnWriteDualIdentityMap<K1, K2, V> {

	protected volatile DualIdentityMap<K1, K2, V> map;
	
	public CopyOnWriteDualIdentityMap(DualIdentityMap.ValueProvider<K1, K2, V> provider) {
		this.map = new DualIdentityMap<K1, K2, V>(provider, 1);
	}
	
	public V get(K1 key1, K2 key2) {
		return get().get(key1, key2);
	}
	
	public synchronized V putIfAbsent(SpearalContext context, K1 key1, K2 key2) {
		DualIdentityMap<K1, K2, V> map = get();
		
		V value = map.get(key1, key2);
		if (value == null) {
			map = map.clone();
			value = map.putIfAbsent(context, key1, key2);
			set(map);
		}
		return value;
	}
	
	public V getOrPutIfAbsent(SpearalContext context, K1 key1, K2 key2) {
		V value = get().get(key1, key2);
		return (value != null ? value : putIfAbsent(context, key1, key2));
	}
	
	public int size() {
		return get().size();
	}
	
	@Override
	public String toString() {
		return get().toString();
	}

	private DualIdentityMap<K1, K2, V> get() {
		return map;
	}
	
	private void set(DualIdentityMap<K1, K2, V> map) {
		this.map = map;
	}
}
