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
package org.spearal.configuration;

/**
 * @author Franck WOLFF
 */
public class ClassNameAlias implements Configurable {
	
	private final String className;
	private final String alias;

	public ClassNameAlias(Class<?> cls, Class<?> clsAlias) {
		this(cls.getName(), clsAlias.getName());
	}

	public ClassNameAlias(Class<?> cls, String alias) {
		this(cls.getName(), alias);
	}

	public ClassNameAlias(String className, Class<?> clsAlias) {
		this(className, clsAlias.getName());
	}

	public ClassNameAlias(String className, String alias) {
		if (className == null || alias == null)
			throw new NullPointerException();
		
		this.className = className;
		this.alias = alias;
	}

	public String getClassName() {
		return className;
	}

	public String getAlias() {
		return alias;
	}
}
