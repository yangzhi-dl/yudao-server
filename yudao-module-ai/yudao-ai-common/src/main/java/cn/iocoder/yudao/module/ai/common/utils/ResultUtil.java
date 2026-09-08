package cn.iocoder.yudao.module.ai.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ResultUtil {

    public static Map<String, Object> filterNonNullProperties(Object object) {
        Map<String, Object> nonNullProperties = new HashMap<>();
        if (object != null) {
            Class<?> clazz = object.getClass();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        // 将 Long 类型转换为字符串
                        if (value instanceof Long) {
                            value = String.valueOf(value);
                        }
                        nonNullProperties.put(field.getName(), value);
                    }
                } catch (IllegalAccessException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return nonNullProperties;
    }

    public static String getFirstLineIfH1(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String[] lines = input.split("\n", -1);
        if (lines.length == 0) {
            return "";
        }

        String firstLine = lines[0].trim();
        if (firstLine.startsWith("# ")) {
            return firstLine;
        }

        return "";
    }

    public static String removeFirstLineIfH1(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] lines = input.split("\n", -1);
        if (lines.length == 0) {
            return input;
        }

        String firstLine = lines[0].trim();

        String result;
        if (firstLine.startsWith("# ")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < lines.length; i++) {
                if (i > 1) sb.append('\n');
                sb.append(lines[i]);
            }
            result = sb.toString();
        } else {
            result = input;
        }

        return result.trim();
    }

    /**
     * 判断指定年份是否为闰年。
     *
     * @param year 要判断的年份（正整数，但方法对任意整数均适用）
     * @return 如果是闰年返回 true，否则返回 false
     */
    public static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
