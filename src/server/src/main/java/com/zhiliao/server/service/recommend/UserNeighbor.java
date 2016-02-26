package com.zhiliao.server.service.recommend;


import java.util.*;

/**
 * Created by riaqn on 15-9-12.
 */
public class UserNeighbor {
    private DataModel dataModel;

    private UserSimilarity similarity;

    private int limit;

    public UserNeighbor(DataModel dataModel, UserSimilarity similarity, int limit) {
        this.dataModel = dataModel;
        this.similarity = similarity;
        this.limit = limit;
    }

    public static class NeighborEntity implements  Comparable<NeighborEntity> {
        long userId;

        public NeighborEntity(long userId, float similarity) {
            this.userId = userId;
            this.similarity = similarity;
        }

        float similarity;

        @Override
        public int compareTo(NeighborEntity neighborEntity) {
            float minus = similarity - neighborEntity.similarity;
            if (minus < 0) {
                return -1;
            } else if (minus > 0) {
                return 1;
            } else  {
                return 0;
            }
        }
    }

    // get the neighbor of a user
    public List<NeighborEntity> get(long userId) {
        PriorityQueue<NeighborEntity> neighbor = new PriorityQueue<NeighborEntity>();

        for (Long id : dataModel.getUsers()) {
            neighbor.add(new NeighborEntity(id, similarity.get(userId, id)));
            if (neighbor.size() == limit) {
                neighbor.remove();
            }
        }

        List<NeighborEntity> res = new LinkedList<>();
        while (true) {
            NeighborEntity entity = neighbor.poll();
            if (entity == null)
                break;
            res.add(0, entity);
        }
        return res;
    }
}
