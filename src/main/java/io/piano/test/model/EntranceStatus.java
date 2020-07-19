package io.piano.test.model;

import lombok.Value;

@Value
public class EntranceStatus {
    EntranceStatusCode statusCode;
    int roomId;
    
    @Override
    public String toString() {
        return String.format(statusCode.statusAsStringTemplate(), roomId);
    }
}
