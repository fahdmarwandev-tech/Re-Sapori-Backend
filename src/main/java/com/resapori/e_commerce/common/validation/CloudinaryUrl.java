package com.resapori.e_commerce.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a String field is either null/blank (field is optional)
 * or is a valid Cloudinary URL belonging to this project's cloud account.
 *
 * <p>Expected prefix: {@code https://res.cloudinary.com/<cloud-name>/}
 */
@Documented
@Constraint(validatedBy = CloudinaryUrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CloudinaryUrl {

    String message() default "imageUrl must be a valid Cloudinary URL from this project";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
