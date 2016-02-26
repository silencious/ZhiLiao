package com.zhiliao.server.service.recommend;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by riaqn on 15-9-12.
 */
public interface DataModel {
    Iterable<Preference> getPreferencesByUser(long userId);
    Iterable<Long> getUsers();


}
