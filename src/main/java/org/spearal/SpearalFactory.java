package org.spearal;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public interface SpearalFactory {

	SpearalContext getContext();

	SpearalEncoder newEncoder(OutputStream out);

	SpearalEncoder newEncoder(OutputStream out, SpearalPropertyFilter request);

	SpearalDecoder newDecoder(InputStream in);

	SpearalPrinter newPrinter(PrintStream out);

}