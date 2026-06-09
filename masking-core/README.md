# masking-core

Core masking primitives shared by all modules.

## Contains

- `@Mask` annotation
- `MaskType` and `MaskOptions`
- `StringMasker` masking engine

Additional runtime-supported containers

- The masking engine traverses common container types (collections, maps, optionals, arrays) and will recurse into their elements.
- Spring Data `Page`/`Slice`/`Streamable` are supported at runtime via reflection: when present, the masker will call `map(Function)` on the instance so the returned object preserves the original runtime type (no compile-time Spring Data dependency in this module).

## Typical Usage

Use this module directly if you need masking logic without Spring Boot auto-configuration.

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-core</artifactId>
    <version>0.0.6</version>
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

public class CustomerResponse {
    private String name;

    @Mask(type = MaskType.CUSTOM, keepLast = 4)
    private String phone;

    @Mask(type = MaskType.CUSTOM, keepFirst = 2)
    private String email;

    public CustomerResponse(String name, String phone, String email) {
        this.name = name;
        this.phone = phone;
        this.email = email;
    }

    // getters/setters omitted for brevity
}
```

Common example rules

Below are concise examples (annotation and programmatic) that show how to express common masking rules.

- National ID: show only first 1 and last 3 digits -> example masked: 1******890

Annotation:

```java
public class NationalIdResponse {
    @Mask(type = MaskType.CUSTOM, keepFirst = 1, keepLast = 3)
    private String nationalId;

    public NationalIdResponse(String nationalId) {
        this.nationalId = nationalId;
    }

    // Example: raw "1234567890" -> masked "1******890"
}
```

Programmatic:

```java
StringMasker masker = new StringMasker();
String masked = masker.mask("1234567890", new com.waseel.http_response_masking.core.models.MaskOptions(MaskType.CUSTOM, '*', 1, 3));
// masked == "1******890"
```

- Member ID: show only last 4 digits -> example masked: ****5678

Annotation:

```java
public class MemberResponse {
    @Mask(type = MaskType.CUSTOM, keepLast = 4)
    private String memberId;

    public MemberResponse(String memberId) {
        this.memberId = memberId;
    }

    // raw "ABCD5678" -> masked "****5678"
}
```

Programmatic:

```java
String masked = masker.mask("ABCD5678", new com.waseel.http_response_masking.core.models.MaskOptions(MaskType.CUSTOM, '*', 0, 4));
// masked == "****5678"
```

- Full Name: show only first letter of each name -> example masked: A**** M******

Annotation:

```java
public class NameResponse {
    @Mask(type = MaskType.PER_WORD, keepFirst = 1)
    private String fullName;

    public NameResponse(String fullName) {
        this.fullName = fullName;
    }

    // raw "Ahmed Mohamed" -> masked "A**** M******"
}
```

Programmatic:

```java
String masked = masker.mask("Ahmed Mohamed", new com.waseel.http_response_masking.core.models.MaskOptions(MaskType.PER_WORD, '*', 1, 0));
// masked == "A**** M******"
```

- Phone Number: show only first 2 and last 3 digits -> example masked: 05*****890

Annotation:

```java
public class PhoneResponse {
    @Mask(type = MaskType.CUSTOM, keepFirst = 2, keepLast = 3)
    private String phone;

    public PhoneResponse(String phone) {
        this.phone = phone;
    }

    // raw "0512345890" -> masked "05*****890"
}
```

Programmatic:

```java
String masked = masker.mask("0512345890", new com.waseel.http_response_masking.core.models.MaskOptions(MaskType.CUSTOM, '*', 2, 3));
// masked == "05*****890"
```

- Full (mask everything):

Annotation:

```java
public class SecretResponse {
    @Mask(type = MaskType.FULL)
    private String secret;

    public SecretResponse(String secret) {
        this.secret = secret;
    }

    // raw "password" -> masked "********"
}
```

Programmatic:

```java
String all = masker.mask("password", new com.waseel.http_response_masking.core.models.MaskOptions(MaskType.FULL, '*', 0, 0));
// all == "********"
```
```
