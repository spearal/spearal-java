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
package org.spearal;

import java.io.IOException;

import org.spearal.impl.SpearalDateTime;

/**
 * @author Franck WOLFF
 */
public interface SpearalPrinter {

	void printNull() throws IOException;
	void printBoolean(boolean value) throws IOException;
	
	void printIntegral(long value) throws IOException;
	void printBigIntegral(StringData value) throws IOException;
	void printFloating(double value) throws IOException;
	void printBigFloating(StringData value) throws IOException;

	void printString(StringData value) throws IOException;
	void printByteArray(byte[] value, int index, boolean reference) throws IOException;

	void printDateTime(SpearalDateTime value) throws IOException;
	
	void printCollectionStart(int index, int length) throws IOException;
	void printCollectionItemStart(int index, int itemIndex) throws IOException;
	void printCollectionItemEnd(int index, int itemIndex) throws IOException;
	void printCollectionEnd(int index) throws IOException;
	void printCollectionReference(int index) throws IOException;
	
	void printMapStart(int index, int length) throws IOException;
	void printMapKeyStart(int index, int entryIndex) throws IOException;
	void printMapKeyEnd(int index, int entryIndex) throws IOException;
	void printMapValueStart(int index, int entryIndex) throws IOException;
	void printMapValueEnd(int index, int entryIndex) throws IOException;
	void printMapEnd(int index) throws IOException;
	void printMapReference(int index) throws IOException;
	
	void printEnum(StringData className, StringData value) throws IOException;
	void printClass(StringData className) throws IOException;
	
	void printBeanStart(int index, StringData classDescription, String[] classNames)
		throws IOException;
	void printBeanPropertyStart(int index, String propertyName, int propertyIndex);
	void printBeanPropertyEnd(int index, int propertyIndex);
	void printBeanEnd(int index) throws IOException;
	void printBeanReference(int index) throws IOException;
	
	public static class StringData {
		
		public final String value;
		public final int index;
		public final boolean reference;

		public StringData(String value, int index, boolean reference) {
			this.value = value;
			this.index = index;
			this.reference = reference;
		}
	}
}
