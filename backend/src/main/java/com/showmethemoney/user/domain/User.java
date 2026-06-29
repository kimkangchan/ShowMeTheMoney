package com.showmethemoney.user.domain;

import java.time.LocalDateTime;

public class User {

    private Long uuid;
    private String username;
    private String email;
    private String password;
    private String nickname;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public static User of(String username, String email, String encodedPassword, String nickname) {
        User user = new User();
        user.username = username;
        user.email = email;
        user.password = encodedPassword;
        user.nickname = nickname;
        return user;
    }

    public Long getUuid() { return uuid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getNickname() { return nickname; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
