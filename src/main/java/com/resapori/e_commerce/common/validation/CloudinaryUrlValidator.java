package com.resapori.e_commerce.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryUrlValidator implements ConstraintValidator<CloudinaryUrl, String> {

    private static final String CLOUDINARY_BASE = "https://res.cloudinary.com/";

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // null or blank is allowed — use @NotBlank separately if the field is required
        if (value == null || value.isBlank()) {
            return true;
        }

        String expectedPrefix = CLOUDINARY_BASE + cloudName + "/";

        if (!value.startsWith(expectedPrefix)) {
            // Replace the default message with a more descriptive one showing the expected prefix
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "imageUrl must start with: " + expectedPrefix
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
