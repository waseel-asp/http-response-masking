# HTTP Response Masking Spring Boot Starter

`masking-spring-boot-starter` adds automatic masking for sensitive fields in Spring MVC response bodies.

Masking is opt-in per field using `@Mask`, and is applied only during REST response writing.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-starter</artifactId>
    <version>0.0.5</version>
</dependency>
```

## Quick Start

1. Add the starter dependency.
2. Annotate sensitive response fields with `@Mask`.
3. Return objects from `@RestController` endpoints as usual.

```java
package com.example.api;

import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

public record CustomerResponse(
        String name,
        // National ID: keep first 1 and last 3 -> 1******890
        @Mask(type = MaskType.CUSTOM, keepFirst = 1, keepLast = 3) String nationalId,
        // Member ID: keep last 4 -> ****5678
        @Mask(type = MaskType.CUSTOM, keepLast = 4) String memberId,
        // Full name: keep first letter of each name -> A**** M******
        @Mask(type = MaskType.PER_WORD, keepFirst = 1) String fullName,
        // Phone: keep first 2 and last 3 -> 05*****890
        @Mask(type = MaskType.CUSTOM, keepFirst = 2, keepLast = 3) String phone,
        @Mask(type = MaskType.CUSTOM, keepFirst = 2, keepLast = 4) String email, // te********.com
        // Secret: mask everything
        @Mask(type = MaskType.FULL) String secret
        ) {}
```
When serialized as an HTTP response, annotated string fields are masked.

## Configuration

Prefix: `waseel.http-response-masking`

```properties
waseel.http-response-masking.enabled=true
waseel.http-response-masking.fail-fast=true
```

```yaml
waseel:
  http-response-masking:
    enabled: true
    fail-fast: true
```

- `enabled`:
  - `true` (default): response masking is active
  - `false`: masking advice is disabled
- `fail-fast`:
  - `true` (default): masking failures raise an error
  - `false`: original response body is returned and a warning is logged

## Behavior Notes

- Only fields annotated with `@Mask` are masked.
- Only `String` fields are masked.
- Masking runs in Spring MVC response flow (`ResponseBodyAdvice`).
- Internal object usage outside MVC response writing is not masked by default.
 - Collections (`List`, `Set`, etc.), `Map`, `Optional`, arrays and common container types are traversed and masked.
 - Spring Data `Page`/`Slice`/`Streamable` instances are also supported: the masker will invoke their `map(Function)` reflectively so the runtime type is preserved (no compile-time Spring Data dependency required).

## Modules

- `masking-core`: annotation and masking engine
- `masking-spring-boot-autoconfigure`: Spring Boot auto-configuration
- `masking-spring-boot-starter`: end-user dependency entrypoint
