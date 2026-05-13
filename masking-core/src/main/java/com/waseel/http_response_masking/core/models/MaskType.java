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
	 * Keep the last characters visible and mask the preceding ones.
	 */
	KEEP_LAST,
	/**
	 * Keep the first characters visible and mask the remaining ones.
	 */
	KEEP_FIRST
}
