package com.payams.atlas.catalog.domain;

/**
 * Thrown when a cosmetic definition is invalid or violates catalog constraints.
 */
public class InvalidCosmeticException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new exception with the specified detail message.
	 */
	public InvalidCosmeticException(String message) {
		super(message);
	}
}
