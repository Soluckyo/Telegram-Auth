package org.lib.telegramauth.service;

import lombok.extern.slf4j.Slf4j;
import org.lib.telegramauth.entity.TelegramUser;
import org.lib.telegramauth.exception.InitDataIsEmptyException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuthService implements IAuthService {

    private String tokenBot;

    private final ITelegramUserService userService;

    public AuthService(ITelegramUserService userService) {
        this.userService = userService;
    }

    /**
     * Валидирует значение initData путем сравнения кэша созданным нами и кэша, который дал нам Телеграмм
     * @param initData строка, которую нам присылает Телеграмм для аутентификации
     * @return true если значение правильное, false если неправильное
     */
    public boolean isValid(String initData) {
        Map<String, String> dataMap = parseInitData(initData);

        String hash = dataMap.remove("hash");
        if(hash == null) {
            return false;
        }

        String dataInitCheckString = buildDataInitCheckString(dataMap);
        String calculatedHash = calculateHash(dataInitCheckString);

        log.info("Calculated hash: " + calculatedHash);
        log.info("Hash: " + hash);

        return calculatedHash.equals(hash);
    }

    /**
     * Парсит InitData в коллекцию LinkedHashMap, для дальнейших проверок
     * @param initData строка, которую нам присылает Телеграмм
     * @return Коллекцию с ключ-значениями, которые находились в InitData
     */
    public Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new HashMap<>();
        String[] data = initData.split("&");

        for (String s : data) {
            int idx = s.indexOf('=');
            if (idx > 0) {
                String key = URLDecoder.decode(s.substring(0, idx), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(s.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, value);
            }
        }
        return result;
    }

    @Override
    public TelegramUser extractUser(String initData) {
        log.info("extractUser");
        Map<String, String> data = parseInitData(initData);

        Long id = Long.parseLong(data.get("id"));
        String username = data.getOrDefault("username", null);
        String firstname = data.getOrDefault("firstname", null);
        String lastname = data.getOrDefault("lastname", null);

        return userService.savedTelegramUser(id, username, firstname, lastname);    }

    @Override
    public boolean isExpired(String initData, int i) {
        return false;
    }

    /**
     * Преобразует LinkedHashMap в строку с видом для проверки: 'a=1/nb=2'
     * @param dataMap коллекцию со значениями, которые достали из initData
     * @return строку, с ключ-значениями отсортированными по алфавитному порядку(как требует того Телеграмм)
     */
    private String buildDataInitCheckString(Map<String, String> dataMap) {
        return dataMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));
    }


    private String calculateHash(String data) {
        try {
            byte[] key = MessageDigest.getInstance("SHA-256")
                    .digest(tokenBot.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hmac);

        } catch (Exception e) {
            throw new RuntimeException("Не удалось вычислить хэш", e);
        }
    }

    /**
     * Преобразует в hex-формат (шестнадцатеричный формат)
     * @param bytes
     * @return строку в шестнадцатеричном формате
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes)
            sb.append(String.format("%02x", b & 0xff));
        return sb.toString();
    }
}
