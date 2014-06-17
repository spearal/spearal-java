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

	@Override
	public void printNull() throws IOException {
		indent();
		
		out.print("null");
	}

	@Override
	public void printBoolean(boolean value) throws IOException {
		indent();
		
		out.print("(boolean) ");
		out.print(value);
	}

	@Override
	public void printDateTime(SpearalDateTime value) throws IOException {
		indent();
		
		out.print("(date time) ");
		out.print(value);
	}

	@Override
	public void printIntegral(long value) throws IOException {
		indent();
		
		out.print("(integral) ");
		out.print(value);
	}

	@Override
	public void printBigIntegral(BigInteger value) throws IOException {
		indent();
		
		out.print("(big integral) ");
		out.print(value);
	}

	@Override
	public void printFloating(double value) throws IOException {
		indent();
		
		out.print("(floating) ");
		out.print(value);
	}

	@Override
	public void printBigFloating(BigDecimal value) throws IOException {
		indent();
		
		out.print("(big floating) ");
		out.print(value);
	}

	@Override
	public void printString(StringData value) throws IOException {
		indent();
		
		out.print("(string");
		out.print(value.reference ? '^' : '@');
		out.print(value.index);
		out.print(") ");

		if (!value.reference) {
			out.print("<length=");
			out.print(value.value.length());
			out.print("> ");
		}
		
		out.print('"');
		out.print(escape(value.value));
		out.print('"');
	}

	@Override
	public void printByteArray(byte[] value, int index, boolean reference) throws IOException {
		indent();
		
		out.print("(byte array");
		out.print(reference ? '^' : '@');
		out.print(index);
		out.print(")");
		
		if (!reference) {
			out.print(" <length=");
			out.print(value.length);
			out.print("> [");
			for (int i = 0; i < value.length; i++) {
				if (i > 0)
					out.print(" ");
				out.print(String.format("0x%02x", Integer.valueOf(value[i] & 0xff)));
				if (i >= maxByteArrayLength) {
					out.print(" (and ");
					out.print(value.length - i);
					out.print(" more...)");
					break;
				}
			}
			out.print(']');
		}
	}

	@Override
	public void printCollectionStart(int index, int length) throws IOException {
		indent();
		indent++;

		out.print("(collection@");
		out.print(index);
		out.print(") <length=");
		out.print(length);
		out.print("> [");
	}

	@Override
	public void printCollectionEnd() throws IOException {
		indent--;
		indent();
		
		out.print(']');
	}

	@Override
	public void printCollectionReference(int index) throws IOException {
		indent();
		
		out.print("(collection^");
		out.print(index);
		out.print(") [...]");
	}

	@Override
	public void printMapStart(int index, int length) throws IOException {
		indent();
		indent++;

		out.print("(map@");
		out.print(index);
		out.print(") <length=");
		out.print(length);
		out.print("> {");
	}

	@Override
	public void printMapEnd() throws IOException {
		indent--;
		indent();

		out.print('}');
	}

	@Override
	public void printMapReference(int index) throws IOException {
		indent();

		out.print("(map^");
		out.print(index);
		out.print(") {...}");
	}

	@Override
	public void printEnum(StringData className, StringData value) throws IOException {
		indent();

		out.print("(enum) ");
		
		out.print("<class");
		out.print(className.reference ? '^' : '@');
		out.print(className.index);
		if (!className.reference) {
			out.print("=\"");
			out.print(className.value);
			out.print("\"");
		}
		
		out.print(", value");
		out.print(value.reference ? '^' : '@');
		out.print(value.index);
		if (!value.reference) {
			out.print("=\"");
			out.print(value.value);
			out.print("\"");
		}
		out.print("> ");

		out.print(className.value);
		out.print('.');
		out.print(value.value);
	}

	@Override
	public void printClass(StringData className) throws IOException {
		indent();

		out.print("(class");
		out.print(className.reference ? '^' : '@');
		out.print(className.index);
		out.print(") ");

		out.print(className.value);
	}

	@Override
	public void printBeanStart(int index, StringData classDescription, String[] classNames)
		throws IOException {
		
		indent();
		
		out.print("(bean@");
		out.print(index);
		out.print(") ");

		out.print("<desc");
		out.print(classDescription.reference ? '^' : '@');
		out.print(classDescription.index);
		if (!classDescription.reference) {
			out.print("=\"");
			out.print(classDescription.value);
		}
		out.print("\"> ");
		
		boolean first = true;
		for (String className : classNames) {
			if (first)
				first = false;
			else
				out.print(", ");
			out.print(className);
		}

		out.print(" {");

		indent++;
	}

	@Override
	public void printBeanPropertyStart(String propertyName) {
		indent();

		out.print(propertyName);
		out.print(':');
	
		indent++;
	}

	@Override
	public void printBeanPropertyEnd() {
		indent--;
	}

	@Override
	public void printBeanEnd() throws IOException {
		indent--;
		indent();
		
		out.print('}');
	}

	@Override
	public void printBeanReference(int index) throws IOException {
		indent();
		
		out.print("(bean^");
		out.print(index);
		out.print(") ");
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
