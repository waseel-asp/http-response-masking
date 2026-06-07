package com.waseel.http_response_masking.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

class StringMaskerPageTest {

    private final StringMasker masker = new StringMasker();

    @Test
    void maskPageOfCustomObjectsRecursesAndMasks() throws IllegalAccessException {
        Custom c = new Custom("n", "12345678");
        Page<Custom> page = new PageImpl<>(List.of(c));

        Page<Custom> masked = masker.mask(page);

        assertEquals("******78", masked.getContent().get(0).secret);
    }

    private static class Custom implements Serializable {
        private static final long serialVersionUID = 1L;

        String name;

        @Mask(type = MaskType.CUSTOM, keepLast = 2)
        String secret;

        Custom(String name, String secret) {
            this.name = name;
            this.secret = secret;
        }
    }
}
