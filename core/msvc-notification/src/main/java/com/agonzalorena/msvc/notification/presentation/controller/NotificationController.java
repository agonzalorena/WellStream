package com.agonzalorena.msvc.notification.presentation.controller;

import com.agonzalorena.msvc.notification.presentation.service.SseService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final SseService sseService;

    public NotificationController(SseService sseService) {
        this.sseService = sseService;
    }
    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(){
        return sseService.createConnection();
    }
}
