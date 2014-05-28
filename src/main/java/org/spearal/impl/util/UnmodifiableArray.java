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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author Franck WOLFF
 */
public final class UnmodifiableArray<E> implements Collection<E> {

	private static final UnmodifiableArray<Object> empty = new UnmodifiableArray<Object>(new Object[0]);
	
	private final E[] array;
	private Integer hash;
	
	public UnmodifiableArray(E[] array) {
		this.array = array.clone();
	}
	
	public static <T> UnmodifiableArray<T> of(T[] elements) {
		return new UnmodifiableArray<T>(elements);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> UnmodifiableArray<T> empty() {
		return (UnmodifiableArray<T>)empty;
	}

	@Override
	public int size() {
		return array.length;
	}

	@Override
	public boolean isEmpty() {
		return array.length == 0;
	}

	@Override
	public boolean contains(Object o) {
		if (o == null) {
			for (E e : array) {
				if (e == null)
					return true;
			}
		}
		else {
			for (E e : array) {
				if (o.equals(e))
					return true;
			}
		}
		return false;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for (Object o : c) {
			if (!contains(o))
				return false;
		}
		return true;
	}

	@Override
	public Iterator<E> iterator() {
		return new Iterator<E>() {

			private int position = 0;
			
			@Override
			public boolean hasNext() {
				return position < array.length;
			}

			@Override
			public E next() {
				return array[position++];
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return array.clone();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return Arrays.asList(array).toArray(a);
	}

	@Override
	public int hashCode() {
		if (hash == null)
			hash = Integer.valueOf(Arrays.deepHashCode(array));
		return hash.intValue();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == null || obj.getClass() != this.getClass())
			return false;
		return Arrays.deepEquals(array, ((UnmodifiableArray<?>)obj).array);
	}

	@Override
	public String toString() {
		return Arrays.toString(array);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}
}
