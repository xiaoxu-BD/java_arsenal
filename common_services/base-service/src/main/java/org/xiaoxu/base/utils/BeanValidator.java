package org.xiaoxu.base.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidationException;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;

import java.util.Set;

/**
 * @className: BeanValidator
 * @author: xiaoxu
 * @date: 2025/10/19 16:27
 * @Version: 1.0
 * @description:
 */
public class BeanValidator {

    private static Validator validator = Validation.byProvider(HibernateValidator.class).configure().failFast(true)
            .buildValidatorFactory().getValidator();

    /**
     * @param object object
     * @param groups groups
     */
    public static void validateObject(Object object, Class<?>... groups) throws ValidationException {
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(object, groups);
        if (constraintViolations.stream().findFirst().isPresent()) {
            throw new ValidationException(constraintViolations.stream().findFirst().get().getMessage());
        }
    }
}
