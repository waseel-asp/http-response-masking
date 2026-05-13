package com.waseel.http_response_masking.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import com.waseel.http_response_masking.core.StringMasker;

class MaskingAutoConfigurationTest {

	private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
			.withUserConfiguration(MaskingAutoConfiguration.class);

	@Test
	void createsMaskerAndAdviceBeansByDefault() {
		this.contextRunner.run(context -> {
			assertNotNull(context.getBean(StringMasker.class));
			assertNotNull(context.getBean(MaskingResponseBodyAdvice.class));
			assertEquals(1, context.getBeanNamesForType(StringMasker.class).length);
			assertEquals(1, context.getBeanNamesForType(MaskingResponseBodyAdvice.class).length);
		});
	}

	@Test
	void doesNotCreateAdviceWhenMaskingIsDisabled() {
		this.contextRunner
				.withPropertyValues("waseel.http-response-masking.enabled=false")
				.run(context -> {
					assertNotNull(context.getBean(StringMasker.class));
					assertEquals(0, context.getBeanNamesForType(MaskingResponseBodyAdvice.class).length);
				});
	}

	@Test
	void backsOffWhenCustomStringMaskerBeanExists() {
		StringMasker customMasker = new StringMasker();

		this.contextRunner
				.withBean(StringMasker.class, () -> customMasker)
				.run(context -> {
					assertEquals(1, context.getBeanNamesForType(StringMasker.class).length);
					assertSame(customMasker, context.getBean(StringMasker.class));
				});
	}
}
