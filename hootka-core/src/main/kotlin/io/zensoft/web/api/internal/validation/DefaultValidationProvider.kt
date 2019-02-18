package io.zensoft.web.api.internal.validation

import io.zensoft.web.api.ValidationProvider
import org.springframework.stereotype.Component
import javax.validation.ConstraintViolationException
import javax.validation.Validation

@Component
class DefaultValidationProvider : ValidationProvider {

    private val validatorFactory = Validation.buildDefaultValidatorFactory()

    override fun validate(bean: Any) {
        val validator = validatorFactory.validator
        val violations = validator.validate(bean)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

}