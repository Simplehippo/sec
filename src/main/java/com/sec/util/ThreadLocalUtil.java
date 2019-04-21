package com.sec.util;

public class ThreadLocalUtil {

    private static ThreadLocal<String> token = ThreadLocal.withInitial(() -> null);

    public static void setThreadLocalToken(String tokenVal) {
        token.set(tokenVal);
    }

    public static String getThreadLocalToken() {
        return token.get();
    }

    public static void removeThreadLocalToken() {
        token.remove();
    }
}
