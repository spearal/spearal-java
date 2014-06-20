package org.spearal.impl.coder;

import java.io.IOException;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

public class ArrayCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public ArrayCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeArray(value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return (valueClass.isArray() ? coder : null);
	}
}
