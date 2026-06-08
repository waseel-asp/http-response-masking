package com.waseel.http_response_masking.autoconfigure;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.waseel.http_response_masking.core.StringMasker;
import com.waseel.http_response_masking.autoconfigure.annotations.Masked;

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
		boolean hasMethodLevelAnnotation = false;
		boolean hasClassLevelAnnotation = false;
		if (returnType != null) {
			if (returnType.getMethod() != null) {
				Method method = returnType.getMethod();
				hasMethodLevelAnnotation = AnnotatedElementUtils.hasAnnotation(method, Masked.class);
			}
			if (returnType.getContainingClass() != null) {
				Class<?> controller = returnType.getContainingClass();
				hasClassLevelAnnotation = AnnotatedElementUtils.hasAnnotation(controller, Masked.class);
			}
		}
		return hasMethodLevelAnnotation || hasClassLevelAnnotation;
	}

	@Override
	public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
			Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
			ServerHttpResponse response) {
		if (body == null) {
			return null;
		}

		try {
			// ResponseEntity first so we preserve status and headers
			if (body instanceof ResponseEntity<?> re) {
				Object payload = re.getBody();
				Object maskedPayload;
				if (payload == null) {
					maskedPayload = null;
				} else {
					Class<?> pc = payload.getClass();
					// avoid masking JDK/core types (String, boxed primitives, java.time, etc.)
					if (pc.getClassLoader() == null) {
						maskedPayload = payload;
					} else {
						maskedPayload = this.masker.mask(payload);
					}
				}
				return ResponseEntity.status(re.getStatusCode()).headers(re.getHeaders()).body(maskedPayload);
			}

			// Generic HttpEntity (covers other HttpEntity subclasses)
			if (body instanceof HttpEntity<?> he) {
				Object payload = he.getBody();
				Object maskedPayload;
				if (payload == null) {
					maskedPayload = null;
				} else {
					Class<?> pc = payload.getClass();
					if (pc.getClassLoader() == null) {
						maskedPayload = payload;
					} else {
						maskedPayload = this.masker.mask(payload);
					}
				}
				return new HttpEntity<>(maskedPayload, he.getHeaders());
			}

			return this.masker.mask(body);
		} catch (IllegalAccessException e) {
			if (this.properties.failFast()) {
				throw new RuntimeException("Failed to mask response body", e);
			}
			logger.warn("Failed to mask response body, returning original body", e);
			return body;
		}
	}
}
