package io.piano.test.validation;

import io.piano.test.dto.PassCheckDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PassValidatorImpl implements PassValidator {
    private final int maxUsers;
    private final int maxRooms;
    
    @Autowired
    public PassValidatorImpl(
        @Value("${env.max.users:10000}") int maxUsers,
        @Value("${env.max.rooms:5}") int maxRooms
    ) {
        this.maxUsers = maxUsers;
        this.maxRooms = maxRooms;
    }
    
    @Override
    public void validate(PassCheckDto dto) {
        String errMsg = null;
        if (dto.getKeyId() <= 0 || dto.getKeyId() > maxUsers) {
            errMsg = "User's keyId is out of bounds. Available values: [1 - " + maxUsers + "]";
        } else if (dto.getRoomId() <= 0 || dto.getRoomId() > maxRooms) {
            errMsg = "Room number is out of bounds. Available values: [1 - " + maxRooms + "]";
        }
        
        if (errMsg != null) {
            throw new IllegalArgumentException(errMsg);
        }
    }
}
