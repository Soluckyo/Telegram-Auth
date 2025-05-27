package org.lib.telegramauth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TelegramUser {

    @Id
    private String id;
    private String username;
    private String firstName;
    private String lastName;
}
