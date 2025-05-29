package org.lib.telegramauth.controller;

import org.lib.telegramauth.entity.TelegramUser;
import org.lib.telegramauth.service.AuthService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String index(){
        return "index";
    }

    @PostMapping("/auth/telegram")
    public String loginWithTelegram(@RequestParam("initData") String initData, Model model) {
        if(!authService.isValid(initData)) {
            model.addAttribute("error", "Неправильная подпись Телеграм");
            return "error";
        }
        if(authService.isExpired(initData, 2000)){
            model.addAttribute("error", "Истек срок действия");
            return "error";
        }

        TelegramUser user = authService.extractUser(initData);
        model.addAttribute("user", user);
        return "personal-info";
    }
}
