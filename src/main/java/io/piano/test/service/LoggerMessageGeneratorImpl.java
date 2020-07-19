package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.model.EntranceStatus;
import org.springframework.stereotype.Service;

@Service
public class LoggerMessageGeneratorImpl implements LoggerMessageGenerator {
    @Override
    public String successEntranceMessage(PassCheckDto dto) {
        return String.format(
            "User (id: %d) has %s the room-%d successfully",
            dto.getKeyId(),
            dto.isEntrance() ? "entered" : "left",
            dto.getRoomId()
        );
    }
    
    @Override
    public String failEntranceMessage(PassCheckDto dto, EntranceStatus status) {
        return String.format(
            "User (id: %d) has failed to %s the room-%d. Cause: %s",
            dto.getKeyId(),
            dto.isEntrance() ? "enter" : "leave",
            dto.getRoomId(),
            status.toString()
        );
    }
}
