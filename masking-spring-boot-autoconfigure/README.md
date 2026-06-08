# masking-spring-boot-autoconfigure

Spring Boot auto-configuration module for HTTP response masking.

This module wires masking into Spring MVC response serialization using `ResponseBodyAdvice`.

## What It Registers

- `StringMasker` bean (when missing)
- `MaskingResponseBodyAdvice` (conditional on servlet web app and enabled property)
- `MaskingProperties` bound from `waseel.http-response-masking.*`

Notes

- The core masking engine supports traversing collections, maps, optionals and arrays and will recurse into contained elements.
- If Spring Data types such as `Page`/`Slice`/`Streamable` are present at runtime they will be handled by the masker via reflection so the original runtime type is preserved.

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

Common masking examples

```java
// National ID -> show 1 first and 3 last: "1******890"
@Mask(type = MaskType.CUSTOM, keepFirst = 1, keepLast = 3)

// Member ID -> show last 4: "****5678"
@Mask(type = MaskType.CUSTOM, keepLast = 4)

// Full name -> show first letter of each word: "A**** M******"
@Mask(type = MaskType.PER_WORD, keepFirst = 1)

// Phone -> show first 2 and last 3: "05*****890"
@Mask(type = MaskType.CUSTOM, keepFirst = 2, keepLast = 3)
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
    <version>0.0.5</version>
</dependency>
```
