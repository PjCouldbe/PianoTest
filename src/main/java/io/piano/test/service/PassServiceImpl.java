package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.model.EntranceStatus;
import io.piano.test.model.EntranceStatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PassServiceImpl implements PassService {
    private static final int NO_ROOM = -1;
    
    private final LoggerService loggerService;
    private final ConcurrentMap<Integer, Integer> keyIdToCurrentRoomMap;
    
    @Autowired
    public PassServiceImpl(
        LoggerService loggerService,
        @Value("${env.max.users:10000}") int maxUsers
    ) {
        this.loggerService = loggerService;
        this.keyIdToCurrentRoomMap = new ConcurrentHashMap<>(maxUsers);
    }
    
    @Override
    public boolean checkEntrance(PassCheckDto dto) {
        EntranceStatus status = null;
        boolean successed;
        
        int keyId = dto.getKeyId();
        int roomId = dto.getRoomId();
        int currentRoomId = keyIdToCurrentRoomMap.getOrDefault(keyId, NO_ROOM);
        
        //If need to enter
        if (dto.isEntrance()) {
            if (userIsInside(currentRoomId)) {
                status = new EntranceStatus(EntranceStatusCode.ROOM_OCCUPIED, currentRoomId);
                successed = false;
            } else if (roomIsNotAllowed(keyId, roomId)) {
                status = new EntranceStatus(EntranceStatusCode.ROOM_PROHIBITED, roomId);
                successed = false;
            } else {
                keyIdToCurrentRoomMap.put(keyId, roomId);
                successed = true;
            }
        //If need to quit
        } else {
            if ( ! userIsInside(currentRoomId)) {
                status = new EntranceStatus(EntranceStatusCode.IS_OUT, currentRoomId);
                successed = false;
            } else if (isNotCurrentRoom(roomId, currentRoomId)) {
                status = new EntranceStatus(EntranceStatusCode.ROOM_OCCUPIED, currentRoomId);
                successed = false;
            } else {
                keyIdToCurrentRoomMap.put(keyId, NO_ROOM);
                successed = true;
            }
        }
        
        if (successed) {
            loggerService.logEntranceSuccess(dto);
        } else {
            loggerService.logEntranceFailure(dto, status);
        }
        
        return successed;
    }
    
    private boolean userIsInside(int currentRoomId) {
        return currentRoomId != NO_ROOM;
    }
    
    private boolean roomIsNotAllowed(int keyId, int roomId) {
        return keyId % roomId != 0;
    }
    
    private boolean isNotCurrentRoom(int roomId, int currentRoomId) {
        return roomId != currentRoomId;
    }
    
    
    @Override
    public void clear() {
        this.keyIdToCurrentRoomMap.clear();
    }
    
}
