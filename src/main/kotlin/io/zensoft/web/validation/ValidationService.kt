package io.zensoft.web.validation

import org.springframework.stereotype.Component
import javax.validation.ConstraintViolationException
import javax.validation.Validation

@Component
class ValidationService {

    private val validatorFactory = Validation.buildDefaultValidatorFactory()

    fun validateBean(bean: Any) {
        val validator = validatorFactory.validator
        val violations = validator.validate(bean)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

}