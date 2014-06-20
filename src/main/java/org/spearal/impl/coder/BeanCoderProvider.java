package org.spearal.impl.coder;

import java.io.IOException;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

public class BeanCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public BeanCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeBean(value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return coder;
	}
}
