package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.model.EntranceStatus;

public interface LoggerService {
    void logEntranceSuccess(PassCheckDto dto);
    
    void logEntranceFailure(PassCheckDto dto, EntranceStatus currentStatus);
}
