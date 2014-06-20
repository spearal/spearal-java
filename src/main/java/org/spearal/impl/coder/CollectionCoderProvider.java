package org.spearal.impl.coder;

import java.io.IOException;
import java.util.Collection;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

public class CollectionCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public CollectionCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeCollection((Collection<?>)value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return (Collection.class.isAssignableFrom(valueClass) ? coder : null);
	}
}
