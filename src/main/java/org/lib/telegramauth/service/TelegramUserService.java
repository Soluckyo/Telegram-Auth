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


    public TelegramUser savedTelegramUser(TelegramUser savedUser) {
        TelegramUser user = telegramUserRepository.findById(savedUser.getIdTelegramUser()).orElse(
                TelegramUser.builder()
                        .idTelegramUser(savedUser.getIdTelegramUser()).build());

                user.setUsername(savedUser.getUsername());
                user.setFirstName(savedUser.getFirstName());
                user.setLastName(savedUser.getLastName());
                user.setLanguageCode(savedUser.getLanguageCode());
                user.setAllowsWriteToPm(savedUser.isAllowsWriteToPm());
                user.setPhotoUrl(savedUser.getPhotoUrl());

        return telegramUserRepository.save(user);    }
}
