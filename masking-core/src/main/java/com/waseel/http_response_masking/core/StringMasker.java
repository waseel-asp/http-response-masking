package com.waseel.http_response_masking.core;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

public class StringMasker {

	/**
	 * Masks the input string using the provided masking options.
	 * For partial masking strategies, the original value is returned when its
	 * length is shorter than the configured kept character count.
	 *
	 * @param raw     input string to mask
	 * @param options masking configuration to apply
	 * @return masked string based on the selected masking strategy
	 * @throws NullPointerException     if {@code raw}, {@code options}, or
	 *                                  {@code options.type()} is {@code null}
	 * @throws IllegalArgumentException if {@code options.keptCharsCount()} is
	 *                                  negative
	 */
	public String mask(String raw, MaskOptions options) {
		Objects.requireNonNull(raw, "raw must not be null");
		validateMaskingOptions(options);
		if (raw.length() == 0 || (options.type() != MaskType.FULL && raw.length() < options.keptCharsCount())) {
			return raw;
		}
		return switch (options.type()) {
			case MaskType.FULL -> String
					.valueOf(options.maskingChar())
					.repeat(raw.length());
			case MaskType.KEEP_LAST -> String
					.valueOf(options.maskingChar())
					.repeat(raw.length() - options.keptCharsCount())
					.concat(raw.substring(raw.length() - options.keptCharsCount()));
			case MaskType.KEEP_FIRST -> raw.substring(0, options.keptCharsCount())
					.concat(String
							.valueOf(options.maskingChar())
							.repeat(raw.length() - options.keptCharsCount()));
			default -> raw;
		};
	}

	/**
	 * Creates a deep-copied masked version of the provided target object.
	 * Fields discovered as maskable are processed when their declared type is
	 * {@link String}; each value is masked using the field's {@link Mask}
	 * annotation options.
	 *
	 * @param <T>    serializable target type
	 * @param target object to copy and mask
	 * @return deep-copied object with masked string fields
	 * @throws IllegalAccessException if reflective field access fails
	 * @throws NullPointerException   if {@code target} is {@code null} or a
	 *                                maskable string field value is {@code null}
	 */
	public <T extends Serializable> T mask(T target) throws IllegalAccessException {
		T output = MaskReflectionHelper.deepCopy(target);
		List<Field> maskableFields = MaskReflectionHelper.findMaskableFields(output.getClass());
		for (Field field : maskableFields) {
			if (field.getType() == String.class) {
				MaskOptions options = MaskReflectionHelper.toMaskOptions(field.getAnnotation(Mask.class));
				String rawValue = (String) MaskReflectionHelper.readFieldValue(output, field);
				if (rawValue != null) {
					MaskReflectionHelper.writeFieldValue(output, field, this.mask(rawValue, options));
				}
			}
		}
		return output;
	}

	private void validateMaskingOptions(MaskOptions options) {
		Objects.requireNonNull(options, "options must not be null");
		Objects.requireNonNull(options.type(), "masking type must not be null");
		if (options.keptCharsCount() < 0) {
			throw new IllegalArgumentException("keptCharsCount must not be negative");
		}
	}
}
