package io.piano.test.dto;

import lombok.Value;

@Value
public class PassCheckDto {
    int keyId;
    int roomId;
    boolean entrance;
}
