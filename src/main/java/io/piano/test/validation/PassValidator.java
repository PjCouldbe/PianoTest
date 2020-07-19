package io.piano.test.validation;

import io.piano.test.dto.PassCheckDto;

public interface PassValidator {
    void validate(PassCheckDto dto);
}
