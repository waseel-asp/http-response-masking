package com.waseel.http_response_masking.core.models;


public record MaskOptions (
	MaskType type,
	char maskingChar,
	int keepFirst,
	int keepLast
) {}
