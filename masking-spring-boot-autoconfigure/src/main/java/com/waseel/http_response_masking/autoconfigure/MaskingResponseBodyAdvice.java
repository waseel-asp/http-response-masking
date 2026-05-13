package com.waseel.http_response_masking.autoconfigure;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.waseel.http_response_masking.core.StringMasker;

@ControllerAdvice
public class MaskingResponseBodyAdvice implements ResponseBodyAdvice<Object> {

	private static final Logger logger = LoggerFactory.getLogger(MaskingResponseBodyAdvice.class);

	private final StringMasker masker;
	private final MaskingProperties properties;

	public MaskingResponseBodyAdvice(StringMasker masker, MaskingProperties properties) {
		this.masker = masker;
		this.properties = properties;
	}

	@Override
	public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
		return true;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (body != null && body instanceof Serializable serializable) {
			try {
				return this.masker.mask(serializable);
			} catch (IllegalAccessException e) {
				if (this.properties.failFast()) {
					throw new RuntimeException("Failed to mask response body", e);
				}
				logger.warn("Failed to mask response body, returning original body", e);
			}
		}

		return body;
	}
}
