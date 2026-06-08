# masking-spring-boot-starter

Starter artifact for enabling HTTP response masking in Spring Boot applications.

This is the recommended dependency for end users.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-starter</artifactId>
    <version>0.0.6</version>
</dependency>
```

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

## Examples

Programmatic (manual) masking

If you want to mask objects outside of the MVC flow you can use the core
masking engine directly. The `StringMasker` will traverse the object graph and
apply field-level `@Mask` annotations.

```java
import com.waseel.http_response_masking.core.StringMasker;

StringMasker masker = new StringMasker();
CustomerResponse original = new CustomerResponse("Alice", "0123456789", "alice@example.com");
CustomerResponse masked = (CustomerResponse) masker.mask(original);
// masked.phone and masked.email will have masking applied according to @Mask
```

Automatic masking via controller response

When using Spring MVC, responses from controller handlers can be masked
automatically by the starter's auto-configuration. Annotate the handler method
or controller class with `@Masked` to enable masking for that endpoint.

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
