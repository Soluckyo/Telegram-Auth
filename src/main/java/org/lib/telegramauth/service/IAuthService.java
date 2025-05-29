package org.lib.telegramauth.service;

import org.lib.telegramauth.entity.TelegramUser;

import java.util.Map;

public interface IAuthService {
    boolean isValid(String initData);
    Map<String, String> parseInitData(String initData);
    TelegramUser extractUser(String initData);

    boolean isExpired(String initData, int i);
}
