package com.waseel.http_response_masking.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * External configuration for HTTP response masking behavior.
 * <p>
 * Properties are bound from the {@code waseel.http-response-masking.*}
 * namespace.
 * <ul>
 * <li>{@code waseel.http-response-masking.enabled}: enables or disables
 * response masking.</li>
 * <li>{@code waseel.http-response-masking.fail-fast}: when enabled, failures
 * during masking abort response processing instead of returning the original
 * body.</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "waseel.http-response-masking")
public record MaskingProperties (
	/**
	 * Enables masking for supported MVC response bodies.
	 */
	@DefaultValue("true")
	boolean enabled,
	/**
	 * Fails the response flow when masking cannot be completed.
	 */
	@DefaultValue("true")
	boolean failFast
) {

}
