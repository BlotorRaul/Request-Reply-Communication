package com.example.user_service.events;

public class UserEventMessage {
    private String event;
    private String id;
    private String fullName;
    private String email;
    private boolean active;

    public UserEventMessage() {}

    public UserEventMessage(String event, String id, String fullName, String email, boolean active) {
        this.event = event;
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.active = active;
    }

    public String getEvent() { return event; }
    public void setEvent(String event) { this.event = event; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "UserEventMessage{" +
                "event='" + event + '\'' +
                ", id='" + id + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", active=" + active +
                '}';
    }
}
