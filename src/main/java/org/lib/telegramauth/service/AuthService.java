package org.lib.telegramauth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lib.telegramauth.entity.TelegramUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Service
@Slf4j
public class AuthService implements IAuthService {

    @Value("${telegram.bot.token}")
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
        String hash = dataMap.remove("hash").replaceAll("[\"}]", "").trim();
//        dataMap.remove("signature");

        String dataCheckString = buildDataInitCheckString(dataMap);
        byte[] calculatedKey = calculateKey();
        String calculatedHash = bytesToHex(calculateHash(calculatedKey, dataCheckString));

        log.info("initData: " + initData);
        log.info("dataCheckString: " + dataCheckString);
        log.info("Calculated hash: " + calculatedHash);
        log.info("Telegram Hash: " + hash);

        return calculatedHash.equalsIgnoreCase(hash);
    }

    /**
     * Парсит InitData в коллекцию HashMap, для дальнейших проверок
     * @param initData строка, которую нам присылает Телеграмм
     * @return Коллекцию с ключ-значениями, которые находились в InitData
     */
    public Map<String, String> parseInitData(String initData) {
        Map<String, String> params = new TreeMap<>();

        for (String pair : initData.split("&")) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0];
                String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                if (key.startsWith("{\"initData\":\"user")) {
                    key = "user";
                }
                params.put(key, value);
            }
        }
        return params;
    }

    @Override
    public TelegramUser extractUser(String initData) {
        log.info("extractUser");
        Map<String, String> data = parseInitData(initData);
        String userParse = data.get("user");

        log.info("user: " + userParse);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            TelegramUser user = objectMapper.readValue(userParse, TelegramUser.class);
            log.info("user parsed: " + user);
            return userService.savedTelegramUser(user);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Неудалось распарсить user из JSON: " + userParse, e);
        }
    }

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
        StringBuilder sb = new StringBuilder();
        dataMap.forEach((key, value) ->
                sb.append(key).append("=").append(value).append("\n")
        );
        return sb.toString().trim();
    }


    /**
     * Вычисляет ключ, необходимый для вычисления хэша
     * @return строку содержащую ключ(HmacSha256(tokenBot, WebAppData))
     */
    private byte[] calculateKey() {
        try {
            String keyString = "WebAppData";
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyString.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(tokenBot.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось вычислить ключ", e);
        }
    }

    /**
     * Вычисляет хэш по строке с данными используя ключ вычисляемый в calculateKey
     * @param key ключ, из захэшированного токена бота с помощью HmacSHA256 и ключа в виде строки "WebAppData"
     * @return строку хэш
     */
    private byte[] calculateHash(byte[] key, String data) {
        try{
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Не удалось вычислить хэш" + e);
        }
    }

    /**
     * Преобразует в hex-формат (шестнадцатеричный формат)
     * @param bytes
     * @return строку в шестнадцатеричном формате
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
