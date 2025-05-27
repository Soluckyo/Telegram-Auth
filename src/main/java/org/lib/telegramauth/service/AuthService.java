package org.lib.telegramauth.service;

import org.lib.telegramauth.entity.TelegramUser;
import org.lib.telegramauth.exception.InitDataIsEmptyException;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private String tokenBot;

    public TelegramUser authenticate(String initData) {
        return null;
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

        try{
            byte [] key = MessageDigest.getInstance("SHA-256")
                    .digest(tokenBot.getBytes(StandardCharsets.UTF_8));

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            byte [] calculatedHash = mac.doFinal(dataInitCheckString.getBytes(StandardCharsets.UTF_8));
            String hexHash = bytesToHex(calculatedHash);

            return hexHash.equalsIgnoreCase(hash);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Парсит InitData в коллекцию LinkedHashMap, для дальнейших проверок
     * @param initData строка, которую нам присылает Телеграмм
     * @return Коллекцию с ключ-значениями, которые находились в InitData
     */
    private Map<String, String> parseInitData(String initData) {
        Map<String, String> result = new LinkedHashMap<>();
        if (initData != null) {
            String[] data = initData.split("&");
            for (String s : data) {
                String[] keyValue = s.split("=");
                if(keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    result.put(key, value);
                }
            }
        }else{
            throw new InitDataIsEmptyException("InitData равна null!");
        }
        return result;
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

    /**
     * Преобразует в hex-формат (шестнадцатеричный формат)
     * @param bytes
     * @return строку в шестнадцатеричном формате
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
