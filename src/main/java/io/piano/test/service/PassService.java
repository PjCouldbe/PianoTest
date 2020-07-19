package io.piano.test.service;

import io.piano.test.dto.PassCheckDto;

public interface PassService {
    boolean checkEntrance(PassCheckDto dto);
    
    void clear();
}
