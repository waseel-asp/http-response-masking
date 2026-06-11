package com.waseel.http_response_masking.core;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

public class StringMasker {

	/**
	 * Masks the input string using the provided masking options.
	 * For partial masking strategies, the original value is returned when its
	 * length is shorter than the configured kept character count.
	 *
	 * @param raw     input string to mask
	 * @param options masking configuration to apply
	 * @return masked string based on the selected masking strategy
	 * @throws NullPointerException     if {@code raw}, {@code options}, or
	 *                                  {@code options.type()} is {@code null}
	 * @throws IllegalArgumentException if {@code options.keepFirst()} or
	 *                                  {@code options.keepLast()} is negative
	 */
	public String mask(String raw, MaskOptions options) {
		Objects.requireNonNull(raw, "raw must not be null");
		validateMaskingOptions(options);
		if (raw.length() == 0
				|| (options.type() != MaskType.FULL && raw.length() < (options.keepFirst() + options.keepLast()))) {
			return raw;
		}
		return switch (options.type()) {
			case FULL -> String.valueOf(options.maskingChar()).repeat(raw.length());
			case CUSTOM -> {
				int middle = raw.length() - (options.keepFirst() + options.keepLast());
				yield raw.substring(0, options.keepFirst())
						.concat(String.valueOf(options.maskingChar()).repeat(middle))
						.concat(raw.substring(raw.length() - options.keepLast()));
			}
			case PER_WORD -> {
				Matcher m = Pattern.compile("\\S+").matcher(raw);
				StringBuilder sb = new StringBuilder();
				int last = 0;
				while (m.find()) {
					sb.append(raw, last, m.start());
					String token = m.group();
					if (token.length() < options.keepFirst() + options.keepLast()) {
						sb.append(token);
					} else {
						int middle = token.length() - (options.keepFirst() + options.keepLast());
						sb.append(token, 0, options.keepFirst())
								.append(String.valueOf(options.maskingChar()).repeat(middle))
								.append(token, token.length() - options.keepLast(), token.length());
					}
					last = m.end();
				}
				sb.append(raw, last, raw.length());
				yield sb.toString();
			}
			default -> raw;
		};
	}

	/**
	 * Masks the annotated String fields in {@code target}
	 * object and String fields found in sub objects.
	 * Each value is masked using the field's {@link Mask}
	 * annotation options.
	 *
	 * @param <T>    target type
	 * @param target object to mask
	 * @return object with masked string fields
	 * @throws IllegalAccessException if reflective field access fails
	 * @throws NullPointerException   if {@code target} is {@code null} or a
	 *                                maskable string field value is {@code null}
	 */
	public <T> T mask(T target) throws IllegalAccessException {
		return mask(target, Collections.newSetFromMap(new IdentityHashMap<>()));
	}

	private <T> T mask(T target, Set<Object> visited) throws IllegalAccessException {
		if (target == null || !visited.add(target)) {
			return target;
		}
		if (target instanceof Collection<?> collection) {
			@SuppressWarnings("unchecked")
			T res = (T) maskCollection(collection, null, visited);
			return res;
		} else if (target instanceof Map<?, ?> map) {
			@SuppressWarnings("unchecked")
			T res = (T) maskMap(map, null, visited);
			return res;
		} else if (target instanceof Optional<?> optional) {
			@SuppressWarnings("unchecked")
			T res = (T) maskOptional(optional, null, visited);
			return res;
		} else if (target instanceof Object[] arr) {
			@SuppressWarnings("unchecked")
			T res = (T) maskArray(arr, null, visited);
			return res;
		} else {
			for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
				List<Field> fields = Arrays.asList(c.getDeclaredFields());
				for (Field field : fields) {
					var fieldValue = MaskReflectionHelper.readFieldValue(target, field);
					if (fieldValue != null) {
						if (fieldValue instanceof String rawValue && field.isAnnotationPresent(Mask.class)) {
							MaskOptions options = MaskReflectionHelper.toMaskOptions(field.getAnnotation(Mask.class));
							MaskReflectionHelper.writeFieldValue(target, field, this.mask(rawValue, options));
						} else if (MaskReflectionHelper.isCustomClass(fieldValue.getClass())) {
							MaskReflectionHelper.writeFieldValue(target, field,
									this.mask(MaskReflectionHelper.readFieldValue(target, field), visited));
						} else if (fieldValue instanceof Collection<?> collection) {
							MaskReflectionHelper.writeFieldValue(target, field,
									maskCollection(collection, field, visited));
						} else if (fieldValue instanceof Map<?, ?> map) {
							MaskReflectionHelper.writeFieldValue(target, field, this.maskMap(map, field, visited));
						} else if (fieldValue instanceof Optional<?> optional) {
							MaskReflectionHelper.writeFieldValue(target, field,
									maskOptional(optional, field, visited));
						} else if (fieldValue instanceof Object[] arr) {
							MaskReflectionHelper.writeFieldValue(target, field, maskArray(arr, field, visited));
						}
					}
				}
			}
		}
		return target;
	}

	private Collection<?> maskCollection(Collection<?> collection, Field field, Set<Object> visited)
			throws IllegalAccessException {
		if (collection != null && collection.size() > 0) {
			Collection<Object> newCollection;
			try {
				newCollection = (Collection<Object>) collection.getClass().getDeclaredConstructor()
						.newInstance();
			} catch (Exception e) {
				newCollection = new ArrayList<>(collection.size());
			}
			for (Object object : collection) {
				if (object != null) {
					if (object instanceof String && field != null && field.isAnnotationPresent(Mask.class)) {
						MaskOptions options = MaskReflectionHelper
								.toMaskOptions(field.getAnnotation(Mask.class));
						newCollection.add(this.mask((String) object, options));
					} else if (MaskReflectionHelper.isMaskAbleClass(object.getClass())) {
						newCollection.add(this.mask(object, visited));
					}
				}
			}

			return newCollection;
		}
		return collection;
	}

	private Map<?, ?> maskMap(Map<?, ?> map, Field field, Set<Object> visited) throws IllegalAccessException {
		if (map != null && map.size() > 0) {
			Map<Object, Object> newMap;
			try {
				newMap = (Map<Object, Object>) map.getClass().getDeclaredConstructor().newInstance();
			} catch (Exception e) {
				newMap = new HashMap<>();
			}
			for (Object object : map.entrySet()) {
				if (object instanceof Entry entry) {
					if (entry.getValue() instanceof String && field != null && field.isAnnotationPresent(Mask.class)) {
						MaskOptions options = MaskReflectionHelper
								.toMaskOptions(field.getAnnotation(Mask.class));
						newMap.put(entry.getKey(), this.mask((String) entry.getValue(), options));
						continue;
					} else if (entry.getValue() != null
							&& MaskReflectionHelper.isMaskAbleClass(entry.getValue().getClass())) {
						newMap.put(entry.getKey(), this.mask(entry.getValue(), visited));
						continue;
					}
					newMap.put(entry.getKey(), entry.getValue());
				}
			}
			return newMap;
		}
		return map;
	}

	private Optional<?> maskOptional(Optional<?> optional, Field field, Set<Object> visited)
			throws IllegalAccessException {
		if (optional == null || optional.isEmpty()) {
			return optional;
		}
		Object value = optional.get();
		if (value == null) {
			return optional;
		}

		if (value instanceof String && field != null && field.isAnnotationPresent(Mask.class)) {
			MaskOptions options = MaskReflectionHelper.toMaskOptions(field.getAnnotation(Mask.class));
			return Optional.of(this.mask((String) value, options));
		} else if (MaskReflectionHelper.isMaskAbleClass(value.getClass())) {
			return Optional.of(this.mask(value, visited));
		}
		return optional;
	}

	private <T> T[] maskArray(T[] array, Field field, Set<Object> visited) throws IllegalAccessException {
		if (array == null || array.length == 0) {
			return array;
		}
		T[] newArray = Arrays.copyOf(array, array.length);
		for (int i = 0; i < newArray.length; i++) {
			Object el = newArray[i];
			if (el == null) {
				continue;
			}
			if (el instanceof String && field != null && field.isAnnotationPresent(Mask.class)) {
				MaskOptions options = MaskReflectionHelper.toMaskOptions(field.getAnnotation(Mask.class));
				newArray[i] = (T) this.mask((String) el, options);
			} else if (MaskReflectionHelper.isMaskAbleClass(el.getClass())) {
				newArray[i] = (T) this.mask(el, visited);
			}
		}
		return newArray;
	}

	private void validateMaskingOptions(MaskOptions options) {
		Objects.requireNonNull(options, "options must not be null");
		Objects.requireNonNull(options.type(), "masking type must not be null");
		if (options.keepFirst() < 0) {
			throw new IllegalArgumentException("keepFirst must not be negative");
		}
		if (options.keepLast() < 0) {
			throw new IllegalArgumentException("keepLast must not be negative");
		}
	}
}
