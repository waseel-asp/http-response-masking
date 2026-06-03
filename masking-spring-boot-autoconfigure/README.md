# masking-spring-boot-autoconfigure

Spring Boot auto-configuration module for HTTP response masking.

This module wires masking into Spring MVC response serialization using `ResponseBodyAdvice`.

## What It Registers

- `StringMasker` bean (when missing)
- `MaskingResponseBodyAdvice` (conditional on servlet web app and enabled property)
- `MaskingProperties` bound from `waseel.http-response-masking.*`

## Usage

Fields are annotated with `@Mask` from `masking-core` to opt-in masking.

Example annotation usage:

```java
import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

public record CustomerResponse(
    String name,
    @Mask(type = MaskType.CUSTOM, keepLast = 4) String phone,
    @Mask(type = MaskType.CUSTOM, keepFirst = 2) String email
) {}
```

Programmatic masking using `StringMasker` uses the new MaskOptions record:

```java
import com.waseel.http_response_masking.core.StringMasker;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

StringMasker masker = new StringMasker();
// keep last 4 characters
String masked = masker.mask("1234567890", new MaskOptions(MaskType.CUSTOM, '*', 0, 4));
```

## Intended Audience

Use this module directly only if you want fine-grained dependency control.

Most applications should use `masking-spring-boot-starter` instead.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-autoconfigure</artifactId>
    <version>0.0.1</version>
</dependency>
```
