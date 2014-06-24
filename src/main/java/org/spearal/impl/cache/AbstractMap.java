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
public abstract class AbstractMap {
	
	protected static final int MAXIMUM_CAPACITY = 1 << 30;
	protected static final int DEFAULT_INITIAL_CAPACITY = 1 << 6;
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;

	protected int initialCapacity;
	protected int threshold;
	protected int size;

	public AbstractMap() {
		init(DEFAULT_INITIAL_CAPACITY);
	}

	public AbstractMap(int capacity) {
		init(roundUpToPowerOf2(capacity));
	}
	
	protected void init(int capacity) {
		this.initialCapacity = capacity;
        this.threshold = (int)Math.min(capacity * DEFAULT_LOAD_FACTOR, MAXIMUM_CAPACITY + 1);
        this.size = 0;
	}

	public int size() {
		return size;
	}
	
	public void clear() {
		init(initialCapacity);
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
}
