package com.gingerdroids.utils_java;

/**
 * Useful to neatly exit an application from deep within its classes & method calls. 
 * <p>
 * In the <code>main()</code> method, put a wrapper <pre>
 * try { 
 *     ...
 * } catch (NextExitException byee) { 
 *     System.out.println("NeatExitException: "+byee.getMessage()); 
 * }
 * </pre>
 */
public class NeatExitException extends RuntimeException { 
	public NeatExitException(String message) { 
		super(message); 
	}
}
