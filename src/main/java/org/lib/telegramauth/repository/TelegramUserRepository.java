package org.lib.telegramauth.repository;

import org.lib.telegramauth.entity.TelegramUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TelegramUserRepository extends JpaRepository<TelegramUser, Long> {

}
