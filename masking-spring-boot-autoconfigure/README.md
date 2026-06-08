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

Additionally, this module exposes a `@Masked` annotation in the
`com.waseel.http_response_masking.autoconfigure.annotations` package. Apply
`@Masked` to a controller method or controller class to opt-in at the endpoint
level; framework components will detect that annotation and apply masking to
responses from the annotated handler(s).

Example annotation usage:

```java
import com.waseel.http_response_masking.core.annotations.Mask;
import com.waseel.http_response_masking.core.models.MaskType;

public record CustomerResponse(
    String name,
    @Mask(type = MaskType.CUSTOM, keepLast = 4) String phone,
    @Mask(type = MaskType.CUSTOM, keepFirst = 2) String email
) {}

Example controller using the above response type and opting-in via
`@Masked`:

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
        // example payload; in real code build from service/repository
        CustomerResponse resp = new CustomerResponse("Alice", "0123456789", "alice@example.com");
        return ResponseEntity.ok(resp);
    }
}
```

```

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-autoconfigure</artifactId>
    <version>0.0.6</version>
</dependency>
```
