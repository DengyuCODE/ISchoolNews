package com.iSchool.utils.common;

import java.util.Random;

/**
 * 盐生成工具
 */
public class RandomSaltUtil {
    public static String generateRandomString(int length) {
        // 可选的字符集
        String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // 创建一个随机数生成器
        Random random = new Random();

        StringBuilder sb = new StringBuilder(length);

        // 生成指定长度的随机字符串
        for (int i = 0; i < length; i++) {
            // 从字符集中随机选择一个字符
            char randomChar = charSet.charAt(random.nextInt(charSet.length()));
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
