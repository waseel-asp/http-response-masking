# masking-spring-boot-autoconfigure

Spring Boot auto-configuration module for HTTP response masking.

This module wires masking into Spring MVC response serialization using `ResponseBodyAdvice`.

## What It Registers

- `StringMasker` bean (when missing)
- `MaskingResponseBodyAdvice` (conditional on servlet web app and enabled property)
- `MaskingProperties` bound from `waseel.http-response-masking.*`

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
