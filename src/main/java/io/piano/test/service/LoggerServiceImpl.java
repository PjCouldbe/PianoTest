package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.model.EntranceStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoggerServiceImpl implements LoggerService {
    private final LoggerMessageGenerator loggerMessageGenerator;
    
    @Override
    public void logEntranceSuccess(PassCheckDto dto) {
        log.info(
            loggerMessageGenerator.successEntranceMessage(dto)
        );
    }
    
    @Override
    public void logEntranceFailure(PassCheckDto dto, EntranceStatus currentStatus) {
        log.warn(
            loggerMessageGenerator.failEntranceMessage(dto, currentStatus)
        );
    }
}
