package com.waseel.http_response_masking.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

class StringMaskerTest {

	private final StringMasker masker = new StringMasker();

	@Test
	void maskWithFullStrategyMasksAllCharacters() {
		MaskOptions options = new MaskOptions(MaskType.FULL, '*', 0, 0);

		String masked = masker.mask("abcdef", options);

		assertEquals("******", masked);
	}

	@Test
	void maskWithCustomStrategyMasksMiddleCharacters() {
		MaskOptions options = new MaskOptions(MaskType.CUSTOM, '*', 1, 1);

		String masked = masker.mask("abcdef", options);

		assertEquals("a****f", masked);
	}

	@Test
	void maskWithPerWordStrategyMasksMiddleCharactersOfWords() {
		MaskOptions options = new MaskOptions(MaskType.PER_WORD, '*', 2, 1);

		String masked = masker.mask("Lorem ipsum dolor sit amet", options);

		assertEquals("Lo**m ip**m do**r sit am*t", masked);
	}

	@Test
	void maskWithKeepLastStrategyKeepsConfiguredSuffix() {
		MaskOptions options = new MaskOptions(MaskType.CUSTOM, '#', 0, 4);

		String masked = masker.mask("1234567890", options);

		assertEquals("######7890", masked);
	}

	@Test
	void maskWithKeepFirstStrategyKeepsConfiguredPrefix() {
		MaskOptions options = new MaskOptions(MaskType.CUSTOM, 'X', 3, 0);

		String masked = masker.mask("ABCDEFGHIJ", options);

		assertEquals("ABCXXXXXXX", masked);
	}

	@Test
	void maskReturnsRawWhenInputIsEmpty() {
		MaskOptions options = new MaskOptions(MaskType.FULL, '*', 0, 0);

		String masked = masker.mask("", options);

		assertEquals("", masked);
	}

	@Test
	void maskReturnsRawWhenInputShorterThanKeptCharsForPartialStrategies() {
		MaskOptions keepLast = new MaskOptions(MaskType.CUSTOM, '*', 0, 6);
		MaskOptions keepFirst = new MaskOptions(MaskType.CUSTOM, '*', 6, 0);

		assertEquals("abc", masker.mask("abc", keepLast));
		assertEquals("abc", masker.mask("abc", keepFirst));
	}

	@Test
	void maskAllowsLengthEqualToKeptCharsForPartialStrategies() {
		MaskOptions keepLast = new MaskOptions(MaskType.CUSTOM, '*', 0, 4);
		MaskOptions keepFirst = new MaskOptions(MaskType.CUSTOM, '*', 4, 0);

		assertEquals("1234", masker.mask("1234", keepLast));
		assertEquals("1234", masker.mask("1234", keepFirst));
	}

	@Test
	void maskThrowsWhenRawIsNull() {
		MaskOptions options = new MaskOptions(MaskType.FULL, '*', 0, 0);

		assertThrows(NullPointerException.class, () -> masker.mask(null, options));
	}

	@Test
	void maskThrowsWhenOptionsAreNull() {
		assertThrows(NullPointerException.class, () -> masker.mask("abc", null));
	}

	@Test
	void maskThrowsWhenMaskTypeIsNull() {
		MaskOptions options = new MaskOptions(null, '*', 0, 0);

		assertThrows(NullPointerException.class, () -> masker.mask("abc", options));
	}

	@Test
	void maskThrowsWhenKeptCharsCountIsNegative() {
		MaskOptions options = new MaskOptions(MaskType.FULL, '*', -1, 0);

		assertThrows(IllegalArgumentException.class, () -> masker.mask("abc", options));
	}

	@Test
	void maskReturnsRawWhenRawIsShorterThanKeptCharsCount() {
		MaskOptions options = new MaskOptions(MaskType.CUSTOM, '*', 0, 5);

		String masked = masker.mask("123", options);

		assertEquals("123", masked);
	}


	@Test
	void maskObjectHandlesTypeWithNoMaskableFields() throws IllegalAccessException {
		NonMaskedRecord original = new NonMaskedRecord("plain");

		NonMaskedRecord masked = masker.mask(original);

		assertEquals("plain", masked.value);
	}

	@Test
	void maskObjectCanMaskEmptyAnnotatedStringValue() throws IllegalAccessException {
		CustomerRecord original = new CustomerRecord("Name", "", "", 20, "nick");

		CustomerRecord masked = masker.mask(original);

		assertNotNull(masked);
		assertEquals("", masked.phoneNumber);
		assertEquals("", masked.email);
	}

	private static class CustomerRecord implements Serializable {
		private static final long serialVersionUID = 1L;

		private String name;

		@Mask(type = MaskType.CUSTOM, keepLast = 4)
		private String phoneNumber;

		@Mask(type = MaskType.CUSTOM, keepFirst = 1)
		private String email;

		private int age;

		@Mask(type = MaskType.FULL)
		private String nickname;

		private CustomerRecord(String name, String phoneNumber, String email, int age, String nickname) {
			this.name = name;
			this.phoneNumber = phoneNumber;
			this.email = email;
			this.age = age;
			this.nickname = nickname;
		}
	}

	private static class NonMaskedRecord implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String value;

		private NonMaskedRecord(String value) {
			this.value = value;
		}
	}
}
