package com.waseel.http_response_masking.autoconfigure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import com.waseel.http_response_masking.core.StringMasker;
import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

class MaskingResponseBodyAdviceTest {

    private final StringMasker masker = new StringMasker();
    private final MaskingProperties props = new MaskingProperties(true, true);
    private final MaskingResponseBodyAdvice advice = new MaskingResponseBodyAdvice(masker, props);

    @Test
    void masksResponseEntityBody() {
        TestBody tb = new TestBody("1234567890");
        ResponseEntity<TestBody> resp = ResponseEntity.ok(tb);

        Object result = advice.beforeBodyWrite(resp, null, null, null, null, null);

        assertEquals(ResponseEntity.class, result.getClass());
        @SuppressWarnings("unchecked")
        ResponseEntity<TestBody> out = (ResponseEntity<TestBody>) result;
        assertEquals("******7890", out.getBody().secret);
    }

    @Test
    void masksHttpEntityBody() {
        TestBody tb = new TestBody("abcdefg");
        HttpEntity<TestBody> entity = new HttpEntity<>(tb);

        Object result = advice.beforeBodyWrite(entity, null, null, null, null, null);

        assertEquals(HttpEntity.class, result.getClass());
        @SuppressWarnings("unchecked")
        HttpEntity<TestBody> out = (HttpEntity<TestBody>) result;
        assertEquals("***defg", out.getBody().secret);
    }

    private static class TestBody {
        @Mask(type = MaskType.CUSTOM, keepLast = 4)
        String secret;

        TestBody(String secret) { this.secret = secret; }
    }
}
