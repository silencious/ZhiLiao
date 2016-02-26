package com.zhiliao.server.service.recommend;


import java.util.Iterator;

/**
 * Created by riaqn on 15-9-12.
 */
public class UserSimilarity {

    public DataModel dataModel;

    public UserSimilarity(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public float get(long id0, long id1) {
        Iterator<Preference> i = dataModel.getPreferencesByUser(id0).iterator();
        Iterator<Preference> j = dataModel.getPreferencesByUser(id1).iterator();

        float sumX2 = 0;
        float sumY2 = 0;
        float sumXY = 0;

        Preference p0 = new Preference(0, Long.MIN_VALUE, 0);
        Preference p1 = new Preference(0, Long.MIN_VALUE, 0);
        while (true) {
            if (p0.itemId < p1.itemId) {
                if (i.hasNext()) {
                    p0 = i.next();
                    sumX2 += p0.value * p0.value;
                } else {
                    p0 = new Preference(0, Long.MAX_VALUE, 0);
                }
            } else {
                if (j.hasNext()) {
                    p1 = j.next();
                    sumY2 += p1.value * p1.value;
                } else {
                    p1 = new Preference(0, Long.MAX_VALUE, 0);
                }
            }
            if (p0.itemId == p1.itemId) {
                if (p0.itemId == Long.MAX_VALUE)
                    break;
                sumXY += p0.value * p1.value;
            }
        }
        return (float) (sumXY / (Math.sqrt(sumX2) * Math.sqrt(sumY2)));
    }
}
