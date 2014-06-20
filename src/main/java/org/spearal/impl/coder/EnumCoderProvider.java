package org.spearal.impl.coder;

import java.io.IOException;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

public class EnumCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public EnumCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeEnum((Enum<?>)value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return (Enum.class.isAssignableFrom(valueClass) ? coder : null);
	}
}
