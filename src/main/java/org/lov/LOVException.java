package org.lov;

public class LOVException extends RuntimeException {

	public LOVException(String msg) {
		super(msg);
	}

	public LOVException(Exception cause) {
		super(cause);
	}

	private static final long serialVersionUID = 3767985967561221189L;
}
