package edu.umd.cs.guitar.replayer;

public class TestCaseNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Construct a new {@code TestCaseNotFoundException} with no detail
	 * message or cause.
	 * 
	 * @see RuntimeException#RuntimeException()
	 */
	// inheritDoc doesn't work for constructors
	public TestCaseNotFoundException() {
		super();
	}

	/**
	 * Construct a new {@code TestCaseNotFoundException} with the given
	 * detail message.
	 * 
	 * @param message
	 *            the detail message
	 * 
	 * @see RuntimeException#RuntimeException(String)
	 */
	public TestCaseNotFoundException(String message) {
		super(message);
	}

	/**
	 * Construct a new {@code TestCaseNotFoundException} with the given
	 * cause.
	 * 
	 * @param cause
	 *            the cause of this exception
	 * @see RuntimeException#RuntimeException(Throwable)
	 */
	public TestCaseNotFoundException(Throwable cause) {
		super(cause);
	}

	/**
	 * Construct a new {@code TestCaseNotFoundException} with the given
	 * detail message and cause.
	 * 
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the cause of this exception
	 * 
	 * @see RuntimeException#RuntimeException(String, Throwable)
	 */
	public TestCaseNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
