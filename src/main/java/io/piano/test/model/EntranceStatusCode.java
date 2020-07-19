package io.piano.test.model;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum EntranceStatusCode {
    ROOM_PROHIBITED() {
        @Override
        public String statusAsStringTemplate() {
            return "selected room-%d is unavailable";
        }
    },
    ROOM_OCCUPIED() {
        @Override
        public String statusAsStringTemplate() {
            return "current occupied room is %d";
        }
    },
    IS_OUT() {
        @Override
        public String statusAsStringTemplate() {
            return "no room is occupied at the moment";
        }
    };
    
    public abstract String statusAsStringTemplate();
    
}
