package com.zhiliao.server.service.recommend;

import com.zhiliao.server.model.Branch;
import com.zhiliao.server.repository.PreferRepository;
import com.zhiliao.server.service.SliceIterator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.*;

/**
 * Created by riaqn on 15-9-12.
 */
public class UserRecommender {
    @Autowired
    private PreferRepository preferRepository;

    private DataModel dataModel;
    private UserNeighbor neighbor;

    public UserRecommender(DataModel dataModel, UserNeighbor neighbor) {
        this.dataModel = dataModel;
        this.neighbor = neighbor;
    }

    public static class RecommendEntity implements Comparable<RecommendEntity> {
        public long itemId;

        public RecommendEntity(long itemId, float estimate) {
            this.itemId = itemId;
            this.estimate = estimate;
        }

        public float estimate;

        @Override
        public int compareTo(RecommendEntity recommendEntity) {
            float minus = estimate - recommendEntity.estimate;
            if (minus < 0)
                return -1;
            else if (minus > 0)
                return 1;
            else
                return 0;
        }
    }

    public Iterable<RecommendEntity> recommend(long userId, int limit) {
        Map<Long, Float> estimate = new HashMap<>();

        for (UserNeighbor.NeighborEntity entity : neighbor.get(userId)) {
            for (Preference preference : dataModel.getPreferencesByUser(entity.userId)) {
                Float e = estimate.get(preference.itemId);
                if (e == null)
                    e = 0f;
                estimate.put(preference.itemId, e + entity.similarity * preference.value);
            }
        }

        for (Preference preference : dataModel.getPreferencesByUser(userId)) {
            estimate.remove(preference.itemId);
        }

        Iterator<Long> iterator = new SliceIterator<Long>() {
            @Override
            protected Slice<Long> load(Pageable pageable) {
                return preferRepository.findByRandom(userId, pageable);
            }
        }.flatten();

        if (estimate.size() < limit && iterator.hasNext()) {
            Long id = iterator.next();
            if (!estimate.containsKey(id))
                estimate.put(iterator.next(), (float) -estimate.size());
        }

        return Utils.<RecommendEntity>getTop(new Iterator<RecommendEntity>() {
            private Iterator<Map.Entry<Long, Float>> iterator = estimate.entrySet().iterator();
            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public RecommendEntity next() {
                Map.Entry<Long, Float> entry = iterator.next();
                return new RecommendEntity(entry.getKey(), entry.getValue());
            }
        }, limit);

    }
}
