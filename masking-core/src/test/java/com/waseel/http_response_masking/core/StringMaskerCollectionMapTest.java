package com.waseel.http_response_masking.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;
import java.util.*;

import org.junit.jupiter.api.Test;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

class StringMaskerCollectionMapTest {

    private final StringMasker masker = new StringMasker();

    @Test
    void maskFieldMapOfStringsMasksValues() throws IllegalAccessException {
        HolderMap holder = new HolderMap();
        holder.map = new HashMap<>();
        holder.map.put("a", "123456");
        holder.map.put("b", "abcdef");

        HolderMap masked = masker.mask(holder);

        assertNotNull(masked);
        assertEquals("******", masked.map.get("a"));
        assertEquals("******", masked.map.get("b"));
    }

    @Test
    void maskFieldOptionalStringMasksValue() throws IllegalAccessException {
        HolderOptional holder = new HolderOptional();
        holder.opt = Optional.of("secret1234");

        HolderOptional masked = masker.mask(holder);

        assertNotNull(masked);
        assertEquals("******1234", masked.opt.get());
    }

    @Test
    void maskFieldArrayOfStringsMasksElements() throws IllegalAccessException {
        HolderArray holder = new HolderArray();
        holder.arr = new String[] { "oneone", "twotwo" };

        HolderArray masked = masker.mask(holder);

        assertNotNull(masked);
        assertEquals("******", masked.arr[0]);
        assertEquals("******", masked.arr[1]);
    }

    @Test
    void maskFieldCollectionOfCustomObjectsRecurses() throws IllegalAccessException {
        HolderCollection holder = new HolderCollection();
        Custom c = new Custom("hello", "12345678");
        holder.coll = new ArrayList<>();
        holder.coll.add(c);

        HolderCollection masked = masker.mask(holder);

        assertNotNull(masked);
        assertEquals("******78", masked.coll.get(0).secret);
    }

    private static class HolderMap {
        @Mask(type = MaskType.FULL)
        Map<String, String> map;
    }

    private static class HolderOptional {
        @Mask(type = MaskType.CUSTOM, keepLast = 4)
        Optional<String> opt;
    }

    private static class HolderArray {
        @Mask(type = MaskType.FULL)
        String[] arr;
    }

    private static class HolderCollection {
        List<Custom> coll;
    }

    private static class Custom {
        String name;
        @Mask(type = MaskType.CUSTOM, keepLast = 2)
        String secret;

        Custom(String name, String secret) {
            this.name = name;
            this.secret = secret;
        }
    }
}
