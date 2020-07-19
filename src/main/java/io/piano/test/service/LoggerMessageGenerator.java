package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.model.EntranceStatus;

public interface LoggerMessageGenerator {
    String successEntranceMessage(PassCheckDto dto);
    
    String failEntranceMessage(PassCheckDto dto, EntranceStatus status);
}
