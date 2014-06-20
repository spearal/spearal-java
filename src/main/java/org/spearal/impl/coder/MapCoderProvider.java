package org.spearal.impl.coder;

import java.io.IOException;
import java.util.Map;

import org.spearal.configurable.CoderProvider;
import org.spearal.impl.ExtendedSpearalEncoder;

public class MapCoderProvider implements CoderProvider {

	private final Coder coder;
	
	public MapCoderProvider() {
		this.coder = new Coder() {
			@Override
			public void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException {
				encoder.writeMap((Map<?, ?>)value);
			}
		};
	}

	@Override
	public Coder getCoder(Class<?> valueClass) {
		return (Map.class.isAssignableFrom(valueClass) ? coder : null);
	}
}
