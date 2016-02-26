package com.zhiliao.server.service.recommend;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Created by riaqn on 15-9-12.
 */
public class Utils {
    public static <T> Iterable<T> getTop(Iterator<T> i, int limit) {
        PriorityQueue<T> neighbor = new PriorityQueue<T>();

        while (i.hasNext()) {
            neighbor.add(i.next());
            if (neighbor.size() == limit)
                break;
        }

        while (i.hasNext()) {
            neighbor.add(i.next());
            neighbor.remove();
        }


        List<T> res = new LinkedList<>();
        while (true) {
            T t = neighbor.poll();
            if (t == null)
                break;
            res.add(0, t);
        }
        return res;
    }
}
