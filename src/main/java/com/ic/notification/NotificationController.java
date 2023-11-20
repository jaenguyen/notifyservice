package com.ic.notification;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/apis")
@RestController
public class NotificationController  {

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/push")
    public String sendNotification(@RequestParam Map<String, String> params) throws Exception {
        return notificationService.sendNotification(params);
    }
}
