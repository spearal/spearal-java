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
package org.spearal.impl.alias;

import org.spearal.configuration.AliasStrategy;

/**
 * @author William DRAI
 */
public class PackageTranslatorAliasStrategy implements AliasStrategy {
	
	private final String clientPackagePrefix;
	private final String serverPackagePrefix;
	
	public PackageTranslatorAliasStrategy(String clientPackagePrefix, String serverPackagePrefix) {
		this.clientPackagePrefix = clientPackagePrefix;
		this.serverPackagePrefix = serverPackagePrefix;
	}
	
	@Override
	public String unalias(String alias) {
		if (alias.startsWith(serverPackagePrefix))
			return alias.replaceFirst(serverPackagePrefix.replace(".", "\\."), clientPackagePrefix);
		return alias;
	}
	
	@Override
	public String alias(Class<?> clazz) {
		if (clazz.getPackage().getName().startsWith(clientPackagePrefix))
			return clazz.getName().replaceFirst(clientPackagePrefix.replace(".", "\\."), serverPackagePrefix);
		return clazz.getName();
	}
}
