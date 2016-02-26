package com.zhiliao.server.service.recommend;

import com.zhiliao.server.model.User;
import com.zhiliao.server.model.relationship.Prefer;
import com.zhiliao.server.repository.PreferRepository;
import com.zhiliao.server.repository.UserRepository;
import com.zhiliao.server.service.SliceIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.*;

/**
 * Created by riaqn on 15-9-12.
 */

public class CustomDataModel implements DataModel {
    @Autowired
    private PreferRepository preferRepository;

    @Autowired
    private UserRepository userRepository;

    private float halflife = 14 * 24 * 3600 * 1000;

    public float decay(float prefs, long oldts, long ts) {
        return (float) (prefs * Math.pow(0.5, (ts - oldts) / halflife));
    }

    @Override
    public Iterable<Preference> getPreferencesByUser(long userId) {
        return new Iterable<Preference>() {
            @Override
            public Iterator<Preference> iterator() {
                return new Iterator<Preference>() {
                    private Iterator<Prefer> iterator =
                            new SliceIterator<Prefer>() {
                                @Override
                                protected Slice<Prefer> load(Pageable pageable) {
                                    return preferRepository.findByUseridOrderByItemid(userId, pageable);
                                }
                            }.flatten();
                    private long ts = System.currentTimeMillis();
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Preference next() {
                        Prefer prefer = iterator.next();
                        return new Preference(userId, prefer.itemid, decay(prefer.getPrefs(), prefer.getTs(), ts));
                    }
                };
            }
        };
    }

    @Override
    public Iterable<Long> getUsers() {
        return new Iterable<Long>() {
            @Override
            public Iterator<Long> iterator() {
                return new Iterator<Long>() {
                    private Iterator<User> iterator = new SliceIterator<User>() {
                        @Override
                        protected Slice<User> load(Pageable pageable) {
                            return userRepository.findAll(pageable);
                        }
                    }.flatten();
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public Long next() {
                        return iterator.next().getId();
                    }
                };
            }
        };
    }

    public void add(long userId, long itemId, float value) {
        Prefer prefer = preferRepository.findByUseridAndItemid(userId, itemId);
        if (prefer == null) {
            prefer = new Prefer();
            prefer.setPrefs(0);
            prefer.setTs(0);
            prefer.userid = userId;
            prefer.itemid = itemId;
        }
        long ts = System.currentTimeMillis();
        float pref = decay(prefer.getPrefs(), prefer.getTs(), ts) + value;

        prefer.setPrefs(pref);
        prefer.setTs(ts);

        preferRepository.save(prefer);
    }
}
