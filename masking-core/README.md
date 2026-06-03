# masking-core

Core masking primitives shared by all modules.

## Contains

- `@Mask` annotation
- `MaskType` and `MaskOptions`
- `StringMasker` masking engine

## Typical Usage

Use this module directly if you need masking logic without Spring Boot auto-configuration.

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-core</artifactId>
    <version>0.0.2</version>
</dependency>
```

```java
import com.waseel.http_response_masking.core.StringMasker;
import com.waseel.http_response_masking.core.models.MaskOptions;
import com.waseel.http_response_masking.core.models.MaskType;

StringMasker masker = new StringMasker();
// keep last 4 characters
String masked = masker.mask("1234567890", new MaskOptions(MaskType.CUSTOM, '*', 0, 4));
// masked = "******7890"

Annotation usage with the new attributes:

```java
import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

public record CustomerResponse(
    String name,
    @Mask(type = MaskType.CUSTOM, keepLast = 4) String phone,
    @Mask(type = MaskType.CUSTOM, keepFirst = 2) String email
) {}
```
```
