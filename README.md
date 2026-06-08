# HTTP Response Masking Spring Boot Starter

`masking-spring-boot-starter` adds automatic masking for sensitive fields in Spring MVC response bodies.

Masking is opt-in per field using `@Mask`, and is applied only during REST response writing.

You can also opt-in at the endpoint level by annotating a controller method or
controller class with `@Masked` (from the autoconfigure module). This will
cause responses produced by that handler (or all handlers in the annotated
controller) to be considered for masking in addition to the field-level
`@Mask` annotations.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-starter</artifactId>
    <version>0.0.6</version>
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

Controller example (automatic masking)

```java
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.waseel.http_response_masking.autoconfigure.annotations.Masked;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Masked
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String id) {
        CustomerResponse resp = new CustomerResponse("Alice", "0123456789", "alice@example.com");
        return ResponseEntity.ok(resp);
    }
}
```

Programmatic example (manual masking)

```java
import com.waseel.http_response_masking.core.StringMasker;

StringMasker masker = new StringMasker();
CustomerResponse original = new CustomerResponse("Alice", "0123456789", "alice@example.com");
CustomerResponse masked = (CustomerResponse) masker.mask(original);
// masked.phone and masked.email will have masking applied according to @Mask
```

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
