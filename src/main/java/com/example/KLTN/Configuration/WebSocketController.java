package com.example.KLTN.Configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendNotification(String publicKey, String message) {
        messagingTemplate.convertAndSend("/topic/wallet/" + publicKey, message);
    }
}