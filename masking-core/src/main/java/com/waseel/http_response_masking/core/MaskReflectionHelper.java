package com.waseel.http_response_masking.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;

public final class MaskReflectionHelper {

	private static Map<String, Boolean> customClasses = new ConcurrentHashMap<>();
	private static final List<String> skipAblePkgs = List.of("java.", "javax.", "jakarta.",
			"org.springframewor.",
			"com.fasterxml",
			"org.apache.");

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
		int mods = field.getModifiers();
		if (Modifier.isFinal(mods) || field.getType().isEnum()) {
			return;
		}
		field.set(target, value);
	}

	public static MaskOptions toMaskOptions(Mask mask) {
		return new MaskOptions(mask.type(), mask.maskingChar(), mask.keepFirst(), mask.keepLast());
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

	public static boolean isCustomClass(Class<?> c) {
		if (c == null) {
			return false;
		}
		if (customClasses.containsKey(c)) {
			return customClasses.get(c);
		}

		var cName = c.getName();
		if (c.getName().contains("$$") && c.getSuperclass() != null)
			c = c.getSuperclass();

		if (c.isPrimitive() || c.isArray() || c.isEnum()) {
			customClasses.put(cName, false);
			return false;
		}

		if (Collection.class.isAssignableFrom(c)
				|| Number.class.isAssignableFrom(c)
				|| Date.class.isAssignableFrom(c)
				|| Temporal.class.isAssignableFrom(c)
				|| c == UUID.class || c == Class.class) {
			customClasses.put(cName, false);
			return false;
		}

		if (c.getClassLoader() == null) {
			customClasses.put(cName, false);
			return false;
		}

		String pkg = c.getPackageName();

		if (skipAblePkgs.stream().anyMatch(skipAblePkg -> pkg.startsWith(skipAblePkg))) {
			customClasses.put(cName, false);
			return false;
		}
		customClasses.put(cName, true);
		return true;
	}

	public static boolean isMaskAbleClass(Class<?> c) {
		return Collection.class.isAssignableFrom(c) || c.isArray()
				|| Map.class.isAssignableFrom(c) || Optional.class.isAssignableFrom(c)
				|| isCustomClass(c);
	}
}
