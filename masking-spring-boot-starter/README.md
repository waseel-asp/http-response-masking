# masking-spring-boot-starter

Starter artifact for enabling HTTP response masking in Spring Boot applications.

This is the recommended dependency for end users.

## Maven

```xml
<dependency>
    <groupId>com.waseel.http-response-masking</groupId>
    <artifactId>masking-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

## How It Works

- Brings `masking-spring-boot-autoconfigure` transitively.
- Auto-registers masking integration for Spring MVC response bodies.
- Uses `@Mask` from `masking-core` for field-level opt-in masking.

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
