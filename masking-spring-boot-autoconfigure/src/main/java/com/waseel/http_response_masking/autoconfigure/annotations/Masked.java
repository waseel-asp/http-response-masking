import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation that indicates a controller method or controller class
 * whose responses should be considered for HTTP response masking.
 *
 * <p>Apply this annotation to a controller method or to a controller class.
 * At runtime framework components such as {@code MaskingResponseBodyAdvice}
 * may detect the presence of this annotation to decide whether response
 * masking should be applied.
 *
 * <p>This annotation is retained at runtime so it can be detected by
 * instrumentation or framework components that perform response processing.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Masked {

}
