package com.waseel.http_response_masking.autoconfigure;

import com.waseel.http_response_masking.core.StringMasker;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@AutoConfiguration
@EnableConfigurationProperties(MaskingProperties.class)
public class MaskingAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public StringMasker stringMasker() {
		return new StringMasker();
	}

	@Bean
	@ConditionalOnWebApplication(type = Type.SERVLET)
	@ConditionalOnProperty(prefix = "waseel.http-response-masking", name = "enabled", havingValue = "true", matchIfMissing = true)
	@ConditionalOnClass({ DispatcherServlet.class, ResponseBodyAdvice.class })
	public MaskingResponseBodyAdvice maskingResponseBodyAdvice(StringMasker stringMasker, MaskingProperties properties) {
		return new MaskingResponseBodyAdvice(stringMasker, properties);
	}

}
