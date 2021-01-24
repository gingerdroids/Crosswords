package com.gingerdroids.utils_java;

import java.io.IOException;
import java.io.Writer;

/**
 * Does nothing. 
 * This is useful as a placeholder when a method needs a {@link Writer} argument, but you don't need what's written to it. 
 */
public class DummyWriter extends Writer {

	@Override
	public void close() throws IOException {}

	@Override
	public void flush() throws IOException {}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException {}

}
