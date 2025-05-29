package org.lib.telegramauth.service;

import org.lib.telegramauth.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITelegramUserService{
    TelegramUser savedTelegramUser(Long id, String username, String firstname, String lastname);
}
