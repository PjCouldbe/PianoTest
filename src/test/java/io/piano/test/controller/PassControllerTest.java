package io.piano.test.controller;

import io.piano.test.service.LoggerMessageGenerator;
import io.piano.test.service.LoggerServiceImpl;
import io.piano.test.service.PassService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringRunner.class)
@WebFluxTest(PassController.class)
public class PassControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private LoggerMessageGenerator loggerMessageGenerator;
    @Autowired
    private PassService passService;
    @MockBean
    private LoggerServiceImpl loggerService;
    private final ConcurrentLinkedDeque<String> logStorage = new ConcurrentLinkedDeque<>();
    
    @Configuration
    @ComponentScan("io.piano.test")
    public static class SpringTestConfig { }
    
    @Before
    public void setUp() {
        final Answer<?> onSuccess = a -> {
            String msg = loggerMessageGenerator.successEntranceMessage( a.getArgument(0) );
            logStorage.add(msg);
    
            return null;
        };
        final Answer<?> onFail = a -> {
            String msg = loggerMessageGenerator.failEntranceMessage(
                a.getArgument(0),
                a.getArgument(1)
            );
            logStorage.add(msg);
        
            return null;
        };
        
        doAnswer(onSuccess).when(loggerService).logEntranceSuccess(any());
        doAnswer(onFail).when(loggerService).logEntranceFailure(any(), any());
    }
    
    @After
    public void tearDown() {
        this.logStorage.clear();
        this.passService.clear();
    }
    
    
    @Test
    public void testSuccessEntrance() {
        runRequest(1, 1, true)
            .expectStatus().isOk();
    
        Assert.assertEquals(
            "User (id: 1) has entered the room-1 successfully",
            getLogsCollection()
        );
    }
    
    @Test
    public void testSuccessQuit() {
        runRequest(1, 1, true)
            .expectStatus().isOk();
        runRequest(1, 1, false)
            .expectStatus().isOk();
        
        Assert.assertEquals(
            "User (id: 1) has entered the room-1 successfully\n" +
                "User (id: 1) has left the room-1 successfully",
            getLogsCollection()
        );
    }
    
    @Test
    public void testFailEntrance_AlreadyOccupied() {
        runRequest(1, 1, true)
            .expectStatus().isOk();
        runRequest(1, 2, true)
            .expectStatus().isForbidden();
        
        Assert.assertEquals(
            "User (id: 1) has entered the room-1 successfully\n" +
                "User (id: 1) has failed to enter the room-2. Cause: current occupied room is 1",
            getLogsCollection()
        );
    }
    
    @Test
    public void testFailEntrance_RoomNotAvailable() {
        runRequest(3, 2, true)
            .expectStatus().isForbidden();
        
        Assert.assertEquals(
            "User (id: 3) has failed to enter the room-2. Cause: selected room-2 is unavailable",
            getLogsCollection()
        );
    }
    
    @Test
    public void testFailQuit_IsOut() {
        runRequest(1, 1, true)
            .expectStatus().isOk();
        runRequest(1, 1, false)
            .expectStatus().isOk();
        runRequest(1, 2, false)
            .expectStatus().isForbidden();
        
        Assert.assertEquals(
            "User (id: 1) has entered the room-1 successfully\n" +
                "User (id: 1) has left the room-1 successfully\n" +
                "User (id: 1) has failed to leave the room-2. Cause: no room is occupied at the moment",
            getLogsCollection()
        );
    }
    
    @Test
    public void testFailQuit_AnotherOccupied() {
        runRequest(1, 1, true)
            .expectStatus().isOk();
        runRequest(1, 2, false)
            .expectStatus().isForbidden();
        
        Assert.assertEquals(
            "User (id: 1) has entered the room-1 successfully\n" +
                "User (id: 1) has failed to leave the room-2. Cause: current occupied room is 1",
            getLogsCollection()
        );
    }
    
    @Test
    public void testFail_InvalidUserIdMin() {
        runRequest(0, 0, true)
            .expectStatus().is5xxServerError()
            .expectBody(String.class).isEqualTo("User's keyId is out of bounds. Available values: [1 - 10000]");
    }
    
    @Test
    public void testFail_InvalidUserIdMax() {
        runRequest(10_001, 1, false)
            .expectStatus().is5xxServerError()
            .expectBody(String.class).isEqualTo("User's keyId is out of bounds. Available values: [1 - 10000]");
    }
    
    @Test
    public void testFail_InvalidRoomIdMin() {
        runRequest(1, 0, true)
            .expectStatus().is5xxServerError()
            .expectBody(String.class).isEqualTo("Room number is out of bounds. Available values: [1 - 5]");
    }
    
    @Test
    public void testFail_InvalidRoomIdMax() {
        runRequest(1, 10, true)
            .expectStatus().is5xxServerError()
            .expectBody(String.class).isEqualTo("Room number is out of bounds. Available values: [1 - 5]");
    }
    
    @Test
    public void testFail_AnotherError() {
        final Answer<?> onSuccess = a -> {
            throw new IllegalStateException("Forcedly encountered exception");
        };
        doAnswer(onSuccess).when(loggerService).logEntranceSuccess(any());
    
        runRequest(1, 1, true)
            .expectStatus().is5xxServerError()
            .expectBody(String.class).isEqualTo("Forcedly encountered exception");
    }
    
    @Test
    public void testManyThreads() {
        final Answer<?> onSuccess = a -> {
            String msg = loggerMessageGenerator.successEntranceMessage( a.getArgument(0) );
            System.out.println(msg);
        
            return null;
        };
        final Answer<?> onFail = a -> {
            String msg = loggerMessageGenerator.failEntranceMessage(
                a.getArgument(0),
                a.getArgument(1)
            );
            System.out.println(msg);
    
            return null;
        };
    
        doAnswer(onSuccess).when(loggerService).logEntranceSuccess(any());
        doAnswer(onFail).when(loggerService).logEntranceFailure(any(), any());
    
        AtomicBoolean hasServerErrors = new AtomicBoolean(false);
        Runnable testProcess = () -> {
            for (int i = 0; i < 200; i++) {
                runRequest(
                    ThreadLocalRandom.current().nextInt(1, 10000),
                    ThreadLocalRandom.current().nextInt(1, 5),
                    ThreadLocalRandom.current().nextBoolean()
                )
                .expectStatus()
                .value(status -> hasServerErrors.set(status == 500));
            }
        };
        
        List<Thread> clients = Stream
            .generate(() -> new Thread(testProcess))
            .limit(200)
            .collect(Collectors.toList());
        
        clients.forEach(c -> {
            c.start();
            try {
                c.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Assert.fail();
            }
        });
        
        Assert.assertFalse( hasServerErrors.get() );
    }
    
    
    
    private WebTestClient.ResponseSpec runRequest(int keyId, int roomId, boolean entrance) {
        final String request = String.format("/check?keyId=%d&roomId=%d&entrance=%s", keyId, roomId, entrance);
        
        return webTestClient.post()
            .uri(request)
            .accept(MediaType.APPLICATION_JSON)
            .exchange();
    }
    
    private String getLogsCollection() {
        return logStorage.stream()
            .map(String::trim)
            .collect( joining("\n") );
    }
    
}