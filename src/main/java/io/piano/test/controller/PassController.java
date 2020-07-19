package io.piano.test.controller;

import io.piano.test.dto.PassCheckDto;
import io.piano.test.service.PassService;
import io.piano.test.validation.PassValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class PassController {
    private final PassService passService;
    private final PassValidator passValidator;
    
    @Autowired
    public PassController(
        PassService passService,
        PassValidator passValidator
    ) {
        this.passService = passService;
        this.passValidator = passValidator;
    }
    
    
    @PostMapping("check")
    public ResponseEntity<Mono<Void>> doorPassStatus(
        @RequestParam("roomId") int roomId,
        @RequestParam("entrance") boolean entrance,
        @RequestParam("keyId") int keyId
    ) {
        PassCheckDto dto = new PassCheckDto(keyId, roomId, entrance);
        
        passValidator.validate(dto);
        
        boolean success = passService.checkEntrance(dto);
        return ResponseEntity
            .status(success ? HttpStatus.OK : HttpStatus.FORBIDDEN)
            .body( Mono.empty() );
    }
    
    @ExceptionHandler
    public ResponseEntity<String> handle(Exception ex) {
        log.error("Encountered exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
