package com.zhiliao.server.service.recommend;

/**
 * Created by riaqn on 15-9-12.
 */
public class Preference {
    public Preference(long userId, long itemId, float value) {
        this.userId = userId;
        this.itemId = itemId;
        this.value = value;
    }
    public long userId;
    public long itemId;
    public float value;
}
