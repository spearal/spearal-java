package org.spearal.configurable;

import java.io.IOException;

import org.spearal.impl.ExtendedSpearalEncoder;

public interface CoderProvider extends Configurable {

	public interface Coder {
		
		void writeObject(ExtendedSpearalEncoder encoder, Object value) throws IOException;
	}
	
	Coder getCoder(Class<?> valueClass);
}
