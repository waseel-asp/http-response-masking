# masking-spring-boot-starter

Starter artifact for enabling HTTP response masking in Spring Boot applications.

This is the recommended dependency for end users.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-starter</artifactId>
    <version>0.0.5</version>
</dependency>
```

## How It Works

- Brings `masking-spring-boot-autoconfigure` transitively.
- Auto-registers masking integration for Spring MVC response bodies.
- Uses `@Mask` from `masking-core` for field-level opt-in masking.

- The masking engine traverses common container types (collections, maps, optionals, arrays) and will recurse into contained elements.
- If Spring Data `Page`/`Slice`/`Streamable` types are present at runtime they will be mapped reflectively so the original runtime type is preserved.

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

Quick examples

```java
// National ID: keep first 1 and last 3 -> "1******890"
@Mask(type = MaskType.CUSTOM, keepFirst = 1, keepLast = 3)

// Member ID: keep last 4 -> "****5678"
@Mask(type = MaskType.CUSTOM, keepLast = 4)

// Full name: per-word keepFirst 1 -> "A**** M******"
@Mask(type = MaskType.PER_WORD, keepFirst = 1)

// Phone: keep first 2 and last 3 -> "05*****890"
@Mask(type = MaskType.CUSTOM, keepFirst = 2, keepLast = 3)
```

## Configuration

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
