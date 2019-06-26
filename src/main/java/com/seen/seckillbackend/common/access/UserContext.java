package com.seen.seckillbackend.common.access;

/**
 * ThreadLocal存储User信息
 */
public class UserContext {
    private static ThreadLocal<Long> userHolder = new ThreadLocal<>();

    public static void setUid(Long uid) {
        userHolder.set(uid);
    }

    public static Long getUid() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }

}