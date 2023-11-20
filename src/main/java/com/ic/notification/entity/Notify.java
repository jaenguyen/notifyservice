package com.ic.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Notify {
    
    private String id;
    private String title;
    private String body;
    private long timestamp;
    private boolean unread;
    private String parentId;

    public Notify(String title, String body, long timestamp, boolean unread, String parentId) {
        this.title = title;
        this.body = body;
        this.timestamp = timestamp;
        this.unread = unread;
        this.parentId = parentId;
    } 
}
