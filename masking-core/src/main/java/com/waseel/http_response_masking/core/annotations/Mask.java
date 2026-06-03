package com.waseel.http_response_masking.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.waseel.http_response_masking.core.models.MaskType;

/**
 * Marks a field to be masked when processed by the masking engine.
 * <p>
 * You can use either {@link #value()} (shorthand) or {@link #type()} to set the
 * mask strategy. If both are present, the consuming logic should define
 * precedence.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mask {
	/**
	 * Shorthand mask strategy for concise annotation usage.
	 */
	public MaskType value() default MaskType.FULL;

	/**
	 * Explicit mask strategy.
	 */
	public MaskType type() default MaskType.FULL;

	/**
	 * Character used to replace masked characters.
	 */
	public char maskingChar() default '*';

	/**
	 * Number of characters to keep from the start of the string
	 */
	public int keepFirst() default 0;
	
	/**
	 * Number of characters to keep from the end of the string
	 */
	public int keepLast() default 4;
}
