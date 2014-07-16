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
package org.spearal.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.spearal.SpearalPrinter;

/**
 * @author Franck WOLFF
 */
@SuppressWarnings("boxing")
public class SpearalPrinterImpl implements SpearalPrinter {
	
	protected final PrintStream out;
	protected final int maxStringLength;
	protected final int maxByteArrayLength;
	
	protected int indent;

	public SpearalPrinterImpl(PrintStream out) {
		this(out, 256, 256);
	}
	
	public SpearalPrinterImpl(PrintStream out, int maxStringLength, int maxByteArrayLength) {
		if (out == null)
			throw new NullPointerException();
		
		this.out = out;
		this.maxStringLength = maxStringLength;
		this.maxByteArrayLength = maxByteArrayLength;
		
		this.indent = 0;
	}

	public PrintStream getOut() {
		return out;
	}

	public int getMaxStringLength() {
		return maxStringLength;
	}

	public int getMaxByteArrayLength() {
		return maxByteArrayLength;
	}

	@Override
	public void printNull() throws IOException {
		out.print("null");
	}

	@Override
	public void printBoolean(boolean value) throws IOException {
		out.print(value);
	}

	@Override
	public void printDateTime(SpearalDateTime value) throws IOException {
		out.print(value);
	}

	@Override
	public void printIntegral(long value) throws IOException {
		out.print(value);
	}

	@Override
	public void printBigIntegral(BigInteger value) throws IOException {
		out.print(value);
	}

	@Override
	public void printFloating(double value) throws IOException {
		out.print(value);
	}

	@Override
	public void printBigFloating(BigDecimal value) throws IOException {
		out.print(value);
	}

	@Override
	public void printString(StringData value) throws IOException {
		out.printf("\"%s\"", escape(value.value));
	}

	@Override
	public void printByteArray(byte[] value, int index, boolean reference) throws IOException {
		if (reference)
			out.printf("(^%d)", index);
		else {
			out.printf("<#%d> [", index);
			for (int i = 0; i < value.length; i++) {
				if (i > 0)
					out.print(", ");
				out.printf("0x%02x", value[i] & 0xff);
				if (i >= maxByteArrayLength) {
					out.printf(" (and %d more...)", value.length - i);
					break;
				}
			}
			out.print(']');
		}
	}

	@Override
	public void printCollectionStart(int index, int length) throws IOException {
		out.printf("<#%d> [", index);
		indent++;
	}

	@Override
	public void printCollectionItemStart(int index) throws IOException {
		if (index != 0)
			out.print(',');
		indent();
	}

	@Override
	public void printCollectionItemEnd(int index) throws IOException {
	}

	@Override
	public void printCollectionEnd() throws IOException {
		indent--;
		indent();
		out.print(']');
	}

	@Override
	public void printCollectionReference(int index) throws IOException {
		out.printf("(^%d)", index);
	}

	@Override
	public void printMapStart(int index, int length) throws IOException {
		out.printf("<#%d> {", index);
		indent++;
	}

	@Override
	public void printMapKeyStart(int index) throws IOException {
		if (index != 0)
			out.print(',');
		indent();
	}

	@Override
	public void printMapKeyEnd(int index) throws IOException {
		out.print(" -> ");
	}

	@Override
	public void printMapValueStart(int index) throws IOException {
		indent++;
	}

	@Override
	public void printMapValueEnd(int index) throws IOException {
		indent--;
	}

	@Override
	public void printMapEnd() throws IOException {
		indent--;
		indent();
		out.print('}');
	}

	@Override
	public void printMapReference(int index) throws IOException {
		out.printf("(^%d)", index);
	}

	@Override
	public void printEnum(StringData className, StringData value) throws IOException {
		out.printf("%s.%s", className.value, value.value);
	}

	@Override
	public void printClass(StringData className) throws IOException {
		out.print(className.value);
	}

	@Override
	public void printBeanStart(int index, StringData classDescription, String[] classNames)
		throws IOException {
		boolean first = true;
		for (String className : classNames) {
			if (first)
				first = false;
			else
				out.print(", ");
			out.print(className);
		}
		
		out.printf(" <#%d> {", index);
		indent++;
	}

	@Override
	public void printBeanPropertyStart(String propertyName, boolean first) {
		if (!first)
			out.print(',');
		
		indent();
		out.printf("%s: ", propertyName);
	}

	@Override
	public void printBeanPropertyEnd() {
	}

	@Override
	public void printBeanEnd() throws IOException {
		indent--;
		indent();
		out.print('}');
	}

	@Override
	public void printBeanReference(int index) throws IOException {
		out.printf("(^%d)", index);
	}
	
	protected void indent() {
		out.println();
		for (int i = 0; i < indent; i++)
			out.print("    ");
	}
	
	protected String escape(String s) {
		if (s == null || s.length() == 0)
			return s;
		
		StringBuilder sb = new StringBuilder(Math.min(s.length(), maxStringLength));
		final int max = s.length();
		for (int i = 0; i < max; i++) {
			char c = s.charAt(i);
			escape(c, sb);
			
			if (i >= maxStringLength) {
				sb.append(" (and ");
				sb.append(s.length() - 1);
				sb.append(" more...)");
				break;
			}
		}
		return sb.toString();
	}
	
	protected void escape(char c, StringBuilder sb) {
		if (c >= 0x20 && c <= 0x7F) {
			if (c == '"' || c == '\\')
				sb.append('\\');
			sb.append(c);
		}
		else {
			switch (c) {
				case '\b': sb.append("\\b"); break;
				case '\t': sb.append("\\t"); break;
				case '\n': sb.append("\\n"); break;
				case '\f': sb.append("\\f"); break;
				case '\r': sb.append("\\r"); break;
				default: {
					String hex = Integer.toHexString(c);
					switch (hex.length()) {
						case 1: sb.append("\\u000"); break;
						case 2: sb.append("\\u00"); break;
						case 3: sb.append("\\u0"); break;
						default: sb.append("\\u"); break;
					}
					sb.append(hex);
					break;
				}
			}
		}
	}
}
