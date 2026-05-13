package com.waseel.http_response_masking.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;

public final class MaskReflectionHelper {

	public static List<Field> findMaskableFields(Class<?> type) {
		List<Field> fields = new ArrayList<>();
		for (Field field : type.getDeclaredFields()) {
			if (field.isAnnotationPresent(Mask.class)) {
				fields.add(field);
			}
		}
		return fields;
	}

	public static Object readFieldValue(Object target, Field field) throws IllegalAccessException {
		field.setAccessible(true);
		return field.get(target);
	}

	public static void writeFieldValue(Object target, Field field, Object value) throws IllegalAccessException {
		field.setAccessible(true);
		field.set(target, value);
	}

	public static MaskOptions toMaskOptions(Mask mask) {
		return new MaskOptions(mask.type(), mask.maskingChar(), mask.keptCharsCount());
	}

	public static <T extends Serializable> T deepCopy(T source) {
		if (source == null) {
			return null;
		}
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos)) {
			out.writeObject(source);
			out.flush();
			try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					ObjectInputStream in = new ObjectInputStream(bis)) {
				@SuppressWarnings("unchecked")
				T copy = (T) in.readObject();
				return copy;
			}
		} catch (IOException | ClassNotFoundException e) {
			throw new IllegalArgumentException("Failed to deep copy object", e);
		}
	}
}
