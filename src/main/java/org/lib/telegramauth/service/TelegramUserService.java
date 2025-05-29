package org.lib.telegramauth.service;

import org.lib.telegramauth.entity.TelegramUser;
import org.lib.telegramauth.repository.TelegramUserRepository;
import org.springframework.stereotype.Service;

@Service
public class TelegramUserService implements ITelegramUserService {

    private final TelegramUserRepository telegramUserRepository;

    public TelegramUserService(TelegramUserRepository telegramUserRepository) {
        this.telegramUserRepository = telegramUserRepository;
    }


    public TelegramUser savedTelegramUser(Long id, String username, String firstname, String lastname) {
        TelegramUser user = telegramUserRepository.findById(id).orElse(new TelegramUser());

        user.setIdTelegramUser(id);
        user.setUsername(username);
        user.setFirstName(firstname);
        user.setLastName(lastname);

        return telegramUserRepository.save(user);    }
}
