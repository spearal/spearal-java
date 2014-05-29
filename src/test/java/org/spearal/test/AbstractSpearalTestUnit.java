package org.spearal.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.spearal.SpearalFactory;
import org.spearal.SpearalInput;
import org.spearal.SpearalOutput;
import org.spearal.SpearalRequest;

public abstract class AbstractSpearalTestUnit {

	public AbstractSpearalTestUnit() {
	}

	protected byte[] encode(Object o) throws IOException {
		return encode(new SpearalFactory(), null, o);
	}

	protected byte[] encode(SpearalRequest request, Object o) throws IOException {
		return encode(new SpearalFactory(), request, o);
	}

	protected byte[] encode(SpearalFactory factory, Object o) throws IOException {
		return encode(factory, null, o);
	}

	protected byte[] encode(SpearalFactory factory, SpearalRequest request, Object o) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		SpearalOutput out = factory.newOutput(baos, request);
		out.writeAny(o);
		return baos.toByteArray();
	}
	
	protected Object decode(byte[] bytes) throws IOException {
		return decode(new SpearalFactory(), bytes);
	}
	
	protected Object decode(SpearalFactory factory, byte[] bytes) throws IOException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		SpearalInput in = factory.newInput(bais);
		return in.readAny();
	}
}
