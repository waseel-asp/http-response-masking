package com.waseel.http_response_masking.core.models;

/**
 * Supported masking strategies.
 */
public enum MaskType {
	/**
	 * Mask all characters in the target value.
	 */
	FULL,

	/**
	 * Mask based on the value of {@code keepFirst} and {@code keepLast}
	 */
	CUSTOM,

	/**
	 * Mask per word based on the value of {@code keepFirst} and {@code keepLast}
	 */
	PER_WORD
}
