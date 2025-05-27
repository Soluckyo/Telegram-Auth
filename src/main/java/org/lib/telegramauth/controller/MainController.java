package org.lib.telegramauth.controller;

import org.lib.telegramauth.entity.TelegramUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    @GetMapping
    public String loginWithTelegram(@RequestParam String initData, Model model) {
        TelegramUser telegramUser = new TelegramUser();
    }
}
