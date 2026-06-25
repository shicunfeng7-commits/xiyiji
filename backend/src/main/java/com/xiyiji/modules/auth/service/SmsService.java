package com.xiyiji.modules.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class SmsService {

    private static final String CODE_PREFIX = "sms:code:";
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final String SEND_PREFIX = "sms:send:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final Random random = new Random();

    public String sendCode(String phone) {
        String intervalKey = SEND_PREFIX + phone;
        Boolean exists = stringRedisTemplate.hasKey(intervalKey);
        if (Boolean.TRUE.equals(exists)) {
            throw new RuntimeException("验证码已发送，请60秒后重试");
        }

        String code = generateCode();
        stringRedisTemplate.opsForValue().set(CODE_PREFIX + phone, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(intervalKey, "1", SEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

        System.out.println("========== SMS验证码 ==========");
        System.out.println("手机号: " + phone);
        System.out.println("验证码: " + code);
        System.out.println("有效期: " + CODE_EXPIRE_MINUTES + " 分钟");
        System.out.println("==============================");

        return code;
    }

    public boolean verifyCode(String phone, String code) {
        String key = CODE_PREFIX + phone;
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (cachedCode != null && cachedCode.equals(code)) {
            stringRedisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private String generateCode() {
        int bound = (int) Math.pow(10, CODE_LENGTH);
        return String.format("%0" + CODE_LENGTH + "d", random.nextInt(bound));
    }
}
