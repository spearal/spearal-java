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
package org.spearal.impl.partial;

import java.lang.reflect.Proxy;

import org.spearal.SpearalContext;
import org.spearal.configuration.PartialObjectFactory;
import org.spearal.configuration.PropertyFactory.Property;
import org.spearal.impl.cache.AnyMap.ValueProvider;
import org.spearal.impl.instantiator.ProxyInstantiator;

/**
 * @author William DRAI
 */
public class NoProxyPartialObjectFactory implements PartialObjectFactory, ValueProvider<Class<?>, Object, Class<?>> {
	
	public NoProxyPartialObjectFactory() {
	}

	@Override
	public Class<?> createValue(SpearalContext context, Class<?> key, Object unused) {
		context.getSecurizer().checkDecodable(key);
		
		return key;
	}

	@Override
	public Object instantiatePartial(SpearalContext context, Class<?> cls, Property[] partialProperties)
		throws InstantiationException, IllegalAccessException {
		
		if (Proxy.isProxyClass(cls))
			return ProxyInstantiator.instantiatePartial(context, cls, partialProperties);
		
		return context.instantiate(cls, null);
	}
}
